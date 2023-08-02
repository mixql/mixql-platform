package org.mixql.engine.core

import com.github.nscala_time.time.Imports.{DateTime, richReadableInstant, richReadableInterval}
import org.mixql.engine.core.logger.ModuleLogger
import org.zeromq.{SocketType, ZMQ}
import org.mixql.remote.messages.gtype.NULL
import org.mixql.remote.messages.module.{Execute, ExecuteFunction, GetDefinedFunctions, ParamChanged, ShutDown}
import org.mixql.remote.messages.module.worker.{IWorkerSendToPlatform, IWorkerSender, PlatformVar, PlatformVarWasSet, PlatformVars, PlatformVarsNames, PlatformVarsWereSet, SendMsgToPlatform, WorkerFinished}
import org.mixql.remote.RemoteMessageConverter
import org.mixql.remote.messages.Message

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.Random
import org.mixql.remote.messages

object Module {
  var ctx: ZMQ.Context = null
  implicit var server: ZMQ.Socket = null
  var poller: ZMQ.Poller = null
  var workerPoller: ZMQ.Poller = null

  def sendMsgToServerBroker(msg: Array[Byte],
                            clientAddress: Array[Byte],
                            logger: ModuleLogger): Boolean = {
    // Sending multipart message
    import logger._
    logDebug(s"sendMsgToServerBroker: sending empty frame")
    server.send("".getBytes(), ZMQ.SNDMORE) // Send empty frame
    logDebug(s"sendMsgToServerBroker: sending clientaddress")
    server.send(clientAddress, ZMQ.SNDMORE) // First send address frame
    logDebug(s"sendMsgToServerBroker: sending empty frame")
    server.send("".getBytes(), ZMQ.SNDMORE) // Send empty frame
    logDebug(s"sendMsgToServerBroker: sending message")
    server.send(msg)
  }

  def sendMsgToServerBroker(
                             msg: String,
                             logger: ModuleLogger
                           ): Boolean = {
    import logger._
    logDebug(s"sendMsgToServerBroker: convert msg of type String to Array of bytes")
    logDebug(s"sending empty frame")
    server.send("".getBytes(), ZMQ.SNDMORE) // Send empty frame
    logDebug(s"Send msg to server ")
    server.send(msg.getBytes())
  }

  def sendMsgToServerBroker(
                             msg: Message,
                             clientAddress: Array[Byte],
                             logger: ModuleLogger
                           ): Boolean = {
    import logger._
    logDebug(
      s"sendMsgToServerBroker: convert msg of type Protobuf to Array of bytes"
    )
    sendMsgToServerBroker(RemoteMessageConverter.toArray(msg), clientAddress, logger)
  }

  def readMsgFromServerBroker(logger: ModuleLogger): (Array[Byte], Option[Array[Byte]], Option[String]) = {
    import logger._
    // FOR PROTOCOL SEE BOOK OReilly ZeroMQ Messaging for any applications 2013 ~page 100
    // From server broker messenger we get msg with such body:
    // identity frame
    // empty frame --> delimiter
    // data ->
    if (server.recv(0) == null)
      throw new BrakeException() // empty frame
    logDebug(s"readMsgFromServerBroker: received empty frame")

    val clientAdrress = server.recv(0) // Identity of client object on server
    // or pong-heartbeat from broker
    if (clientAdrress == null)
      throw new BrakeException()

    var msg: Option[Array[Byte]] = None

    var pongHeartMessage: Option[String] = Some(new String(clientAdrress))
    if (pongHeartMessage.get != "PONG-HEARTBEAT") {
      pongHeartMessage = None

      logDebug(s"readMsgFromServerBroker: got client address: " + new String(clientAdrress))

      if (server.recv(0) == null)
        throw new BrakeException() // empty frame
      logDebug(s"readMsgFromServerBroker: received empty frame")

      logDebug(s"have received message from server ${new String(clientAdrress)}")
      msg = Some(server.recv(0))
    }

    (clientAdrress, msg, pongHeartMessage)
  }

  //key -> workers unique name
  //int -> poll in index of pair to communicate with this worker
  val workersMap: mutable.Map[String, ZMQ.Socket] = mutable.Map()
  val r: Random.type = scala.util.Random

  def generateUnusedWorkersName(): String = {
    val numPattern = "[0-9]+".r
    val ids = workersMap.keys.map(name => numPattern.findFirstIn(name).get.toInt)

    var foundUniqueId = false
    var id = -1;
    while (!foundUniqueId) {
      id = r.nextInt().abs
      ids.find(p => p == id) match {
        case Some(_) =>
        case None => foundUniqueId = true
      }
    }
    s"worker$id"
  }

}

class Module(
              executor: IModuleExecutor,
              identity: String,
              host: String,
              port: Int
            )(implicit logger: ModuleLogger) {


  val heartBeatInterval: Long = 3000
  var processStart: DateTime = null
  var liveness: Int = 3
  var brokerClientAdress: Array[Byte] = Array()

  import logger._
  import Module._

  def startServer(): Unit = {
    logInfo(s"Starting main client")

    logInfo(s"host of server is " + host + " and port is " + port.toString)

    try {
      ctx = ZMQ.context(1)
      server = ctx.socket(SocketType.DEALER)
      // set identity to our socket, if it would not be set,
      // then it would be generated by ROUTER socket in broker object on server

      server.setIdentity(identity.getBytes)
      logInfo(
        s"connected: " + server
          .connect(s"tcp://$host:${port.toString}")
      )
      logInfo(s"Connection established.")

      logDebug(s"Setting processStart for timer")
      // Set timer
      processStart = DateTime.now()

      logInfo(s"Setting poller")
      poller = ctx.poller(1)

      logInfo(s"Setting workers poller")
      workerPoller = ctx.poller(14)

      logInfo(s"Register server's socket pollin in poller")
      val serverPollInIndex = poller.register(server, ZMQ.Poller.POLLIN)


      logInfo(s"Sending READY message to server's broker")
      sendMsgToServerBroker("READY", logger)

      while (true) {
        val rc = poller.poll(heartBeatInterval)
        var rcWorkers = -1;
        if (workerPoller.getSize != 0)
          rcWorkers = workerPoller.poll(heartBeatInterval)
        //        if (rc == 1) throw BrakeException()
        if (poller.pollin(serverPollInIndex)) {
          logDebug("Setting processStart for timer, as message was received")
          val (clientAdrressTmp, msg, pongHeartBeatMsg) = readMsgFromServerBroker(logger)
          pongHeartBeatMsg match {
            case Some(_) => // got pong heart beat message
              logDebug(s"got pong heart beat message from broker server")
            case None => // got protobuf message
              implicit val clientAddress: Array[Byte] = clientAdrressTmp
              brokerClientAdress = clientAddress
              implicit val clientAddressStr: String = new String(clientAddress)
              //              executor.reactOnMessage(msg.get)(server, identity, clientAddress)
              val remoteMessage = RemoteMessageConverter.unpackAnyMsgFromArray(msg.get)
              reactOnReceivedMsgForEngine(
                remoteMessage, msg.get,
                clientAddressStr, clientAddress
              )
          }
          processStart = DateTime.now()
          liveness = 3
        } else {
          val elapsed = (processStart to DateTime.now()).millis
          logDebug(s"elapsed: " + elapsed)
          liveness = liveness - 1
          if (liveness == 0) {
            logError(s"heartbeat failure, can't reach server's broker. Shutting down")
            throw new BrakeException()
          }
          if (elapsed >= heartBeatInterval) {
            processStart = DateTime.now()
            logDebug(
              s"heartbeat work. Sending heart beat. Liveness: " + liveness
            )
            sendMsgToServerBroker("PING-HEARTBEAT", logger)
          }
        }

        if (rcWorkers > 0) {
          for (index <- 0 until workerPoller.getSize) {
            if (workerPoller.pollin(index)) {
              val workerSocket = workerPoller.getSocket(index)
              val msg: Message = RemoteMessageConverter.unpackAnyMsgFromArray(workerSocket.recv(0))
              msg match {
                case m: WorkerFinished =>
                  logInfo("Received message WorkerFinished from worker " + m.sender() +
                    " Remove socket from workersMap")
                  workersMap.remove(m.Id)
                  logInfo("Unregister worker's " + m.sender() + " socket from workerPoller")
                  workerPoller.unregister(workerSocket)
                  logInfo("Closing worker's " + m.sender() + " socket")
                  workerSocket.close()
                case m: SendMsgToPlatform =>
                  logInfo("Received message SendMsgToPlatform from worker " + m.sender() +
                    " and send it to platform")
                  sendMsgToServerBroker(m.msg, m.clientAddress(), logger)
                case m: IWorkerSendToPlatform =>
                  logInfo("Received message of type IWorkerSendToPlatform from worker " + m.sender() +
                    s" and proxy it (type: ${m.`type`()}) to platform")
                  sendMsgToServerBroker(m, m.clientAddress(), logger)
              }
            }
          }
        }
      }
    } catch {
      case _: BrakeException => logDebug(s"BrakeException")
      case ex: Exception =>
        logError(s"Error: " + ex.getMessage)
        sendMsgToServerBroker(
          new messages.module.Error(
            s"Module $identity to broker ${new String(brokerClientAdress)}: fatal error: " +
              ex.getMessage
          ),
          brokerClientAdress, logger)
    }
    finally {
      close()
    }
    logInfo(s"Stopped.")
  }

  private def reactOnReceivedMsgForEngine(message: Message, messageRAW: Array[Byte], clientAddressStr: String,
                                          clientAddress: Array[Byte]): Unit = {
    import scala.util.{Success, Failure}
    message match {
      case msg: Execute =>
        reactOnExecuteMessageAsync(msg, clientAddressStr, clientAddress)
      case msg: ParamChanged =>
        reactOnParamChangedMessageAsync(msg, clientAddressStr, clientAddress)
      case _: ShutDown =>
        logInfo(s"Started shutdown")
        try {
          executor.reactOnShutDown(identity, clientAddressStr, logger)
        } catch {
          case e: Throwable => logWarn("Warning: error while reacting on shutdown: " +
            e.getMessage
          )
        }
        throw new BrakeException()
      case msg: ExecuteFunction =>
        reactOnExecuteFunctionMessageAsync(msg, clientAddressStr, clientAddress)
      case _: GetDefinedFunctions =>
        try {
          sendMsgToServerBroker(
            executor.reactOnGetDefinedFunctions(identity, clientAddressStr, logger),
            clientAddress, logger
          )
        }
        catch {
          case e: Throwable =>
            sendMsgToServerBroker(
              new messages.module.Error(
                s"Module $identity to ${clientAddressStr}: error while reacting on getting" +
                  " functions list" + e.getMessage
              ),
              clientAddress, logger)
        }
      case m: messages.module.Error =>
        sendMsgToServerBroker(m, clientAddress, logger)
      case msg: PlatformVarWasSet => sendMessageToWorker(msg, messageRAW)
      case msg: PlatformVar => sendMessageToWorker(msg, messageRAW)
      case msg: PlatformVars => sendMessageToWorker(msg, messageRAW)
      case msg: PlatformVarsWereSet => sendMessageToWorker(msg, messageRAW)
      case msg: PlatformVarsNames => sendMessageToWorker(msg, messageRAW)
    }
  }

  def sendMessageToWorker(msg: IWorkerSender, messageRAW: Array[Byte]) = {
    val workersName = msg.sender()
    logInfo(s"received message ${msg.`type`()} from platfrom to workers-future-$workersName " +
      "Sending it to worker")

    val workerSocket = workersMap(workersName)
    workerSocket.send(messageRAW)
  }


  def reactOnExecuteMessageAsync(msg: Execute, clientAddressStr: String, clientAddress: Array[Byte]) = {
    reactOnRemoteMessageAsync(clientAddress,
      (workersId, ctxPlatform) => {
        logInfo(s"[workers-future-$workersId]: triggering onExecute")
        executor.reactOnExecute(msg, identity, clientAddressStr, logger, ctxPlatform)
      }, (value, socket, workerID) => {
        socket.send(RemoteMessageConverter.toArray(new SendMsgToPlatform(clientAddress,
          value, workerID
        )))
      }, (ex: Throwable, socket, workerID) => {
        socket.send(RemoteMessageConverter.toArray(new SendMsgToPlatform(clientAddress,
          new messages.module.Error(
            s"Module $identity to ${clientAddressStr}: error while reacting on execute: " +
              ex.getMessage
          ), workerID)))
      })
  }

  def reactOnExecuteFunctionMessageAsync(msg: ExecuteFunction, clientAddressStr: String, clientAddress: Array[Byte]) = {
    reactOnRemoteMessageAsync(clientAddress,
      (workersID, ctxPlatform) => {
        logInfo(s"[workers-future-$workersID]: triggering onExecuteFunction")
        executor.reactOnExecuteFunction(msg, identity, clientAddressStr, logger, ctxPlatform)
      }, (value, socket, workerID) => {
        socket.send(RemoteMessageConverter.toArray(new SendMsgToPlatform(clientAddress,
          value, workerID
        )))
      }, (e: Throwable, socket, workerID) => {
        socket.send(RemoteMessageConverter.toArray(new SendMsgToPlatform(clientAddress,
          new messages.module.Error(
            s"Module $identity to ${clientAddressStr}: error while reacting on execute function" +
              s"${msg.name}: " + e.getMessage
          ), workerID)))
      })
  }

  def reactOnParamChangedMessageAsync(msg: ParamChanged, clientAddressStr: String, clientAddress: Array[Byte]) = {
    reactOnRemoteMessageAsync(clientAddress,
      (workersID, ctxPlatform) => {
        logInfo(s"[workers-future-$workersID]: triggering OnParamChanged")
        executor.reactOnParamChanged(msg, identity, clientAddressStr, logger, ctxPlatform)
        new NULL()
      },
      (_, _, _) => {},
      (e: Throwable, socket, workerID) => {
        socket.send(RemoteMessageConverter.toArray(new SendMsgToPlatform(clientAddress,
          new messages.module.Error(
            s"Module $identity to ${clientAddressStr}: error while reacting on changed param: " +
              e.getMessage
          ), workerID
        )))
      })
  }

  def reactOnRemoteMessageAsync(clientAddress: Array[Byte],
                                executeFunc: (String, PlatformContext) => Message,
                                onSuccess: (Message, ZMQ.Socket, String) => Unit,
                                onFailure: (Throwable, ZMQ.Socket, String) => Unit): Unit = {
    import scala.util.{Success, Failure}
    val workersName = generateUnusedWorkersName()
    logInfo("Creating worker " + workersName)
    logInfo(s"Register module's pair socket pollin in workersPoller for worker " + workersName)
    val workerSocket = ctx.socket(SocketType.PAIR)
    val pairPollInIndex = workerPoller.register(workerSocket, ZMQ.Poller.POLLIN)
    workerSocket.bind(s"inproc://$workersName")
    workersMap.put(workersName, workerSocket)
    var futurePairSocket: ZMQ.Socket = null
    Future {
      logInfo(s"[workers-future-$workersName]: Creating future's pair socket for communicating with module")
      futurePairSocket = ctx.socket(SocketType.PAIR)
      logInfo(s"[workers-future-$workersName]: Bind future's pair socket in inproc://$workersName")
      futurePairSocket.connect(s"inproc://$workersName")
      executeFunc(workersName, new PlatformContext(futurePairSocket, workersName, clientAddress))
    }.onComplete {
      case Success(value) => //sendMsgToServerBroker(value, clientAddress, logger)
        onSuccess(value, futurePairSocket, workersName)
        logInfo(s"[workers-future-$workersName]: Sending WorkerFinished to inproc://$workersName")
        futurePairSocket.send(RemoteMessageConverter.toArray(new WorkerFinished(workersName)))
        logInfo(s"[workers-future-$workersName]: Close future's pair socket inproc://$workersName")
        futurePairSocket.close()
      case Failure(ex) => //sendMsgToServerBroker(errorFunc(ex), clientAddress, logger)
        onFailure(ex, futurePairSocket, workersName)
        logInfo(s"[workers-future-$workersName]: Sending WorkerFinished to inproc://$workersName")
        futurePairSocket.send(RemoteMessageConverter.toArray(new WorkerFinished(workersName)))
        logInfo(s"[workers-future-$workersName]: Close future's pair socket inproc://$workersName")
        futurePairSocket.close()
    }
  }

  def close(): Unit = {
    if (server != null) {
      logInfo(s"finally close server")
      server.close()
    }

    if (workersMap.nonEmpty) {
      workersMap.foreach(
        worker => {
          worker._2.close()
        }
      )
    }

    if (poller != null) {
      logInfo(s"finally close poller")
      poller.close()
    }

    if (workerPoller != null) {
      logInfo(s"finally close workerPoller")
      workerPoller.close()
    }

    try {
      if (ctx != null) {
        logInfo(s"finally close context")
        //        implicit val ec: scala.concurrent.ExecutionContext =
        //          scala.concurrent.ExecutionContext.global
        Await.result(
          Future {
            ctx.term()
          },
          scala.concurrent.duration.Duration(5000, "millis")
        )
      }
    } catch {
      case _: Throwable => logError(s"tiemout of closing context exceeded:(")
    }
  }
}
