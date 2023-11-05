package org.mixql.cluster

import com.github.nscala_time.time.Imports.{DateTime, richReadableInstant, richReadableInterval}
import com.typesafe.config.{Config, ConfigFactory}
import logger.*
import org.mixql.engine.core.BrakeException
import org.mixql.protobuf.ErrorOps
import org.mixql.remote.RemoteMessageConverter
import org.zeromq.{SocketType, ZMQ}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.broker.{
  CouldNotConvertMsgError,
  EngineStartedTimeOutElapsedError,
  PlatformPongHeartBeat
}
import org.mixql.remote.messages.module.IModuleSendToClient
import org.mixql.remote.messages.module.toBroker.{
  EngineFailed,
  EngineIsReady,
  EnginePingHeartBeat,
  IBrokerReceiverFromModule
}
import org.mixql.remote.messages.client.toBroker.{EngineStarted, IBrokerReceiverFromClient}
import org.mixql.remote.messages.client.{IModuleReceiver, ShutDown}

import scala.language.postfixOps
import scala.util.Try

object BrokerModule extends java.lang.AutoCloseable {
  private var threadBroker: Thread = null

  private var startedBroker: Boolean = false

  def wasStarted: Boolean = startedBroker

  private var _port: Option[Int] = None
  private var _host: Option[String] = None

  def getPort: Option[Int] = _port

  def getHost: Option[String] = _host

  def start(port: Int, host: String): Unit = {
    this.synchronized {
      if !startedBroker then
        if threadBroker == null then
          _port = Some(port)
          _host = Some(host)

          logInfo("Starting broker thread")
          threadBroker = {
            new BrokerMainRunnable("BrokerMainThread", host, _port.get.toString)
          }
          threadBroker.start()
          startedBroker = true
        else new Exception(logError("Broker: threadBroker exists. Call close before start"))
      else new Exception(logError("Broker: was started previously. Call close before start"))
    }
  }

  override def close() = {
    this.synchronized {
      if (threadBroker != null && threadBroker.isAlive() && !threadBroker.isInterrupted)
        _port = None
        _host = None
        logInfo("Broker: Executing close")
        logInfo("Broker: send interrupt to thread")
        threadBroker.interrupt()
        logInfo("Waiting while broker thread is alive")
        try {
          threadBroker.join();
        } catch case _: InterruptedException => System.out.printf("%s has been interrupted", threadBroker.getName())
        logInfo("server: Broker was shutdown")
        threadBroker = null
        startedBroker = false
    }
  }
}

class BrokerMainRunnable(name: String, host: String, port: String) extends Thread(name) {
  var ctx: ZMQ.Context = null
  var frontend: ZMQ.Socket = null
  var poller: ZMQ.Poller = null

  // Key is identity, Value is list of messages
  val enginesStashedMsgs: mutable.Map[String, ListBuffer[StashedClientMessage]] = mutable.Map()
  var engines: mutable.Set[String] = mutable.Set()
  var enginesStartedTimeOut: mutable.Map[String, (Long, DateTime, String)] = mutable.Map()
  var enginesPingHeartBeatTimeout: mutable.Map[String, (Long, DateTime, Long, Int)] = mutable.Map()
  var clientsThatSentToEngine: mutable.Map[String, mutable.Set[String]] = mutable.Map()
  val NOFLAGS = 0
  val config: Config = ConfigFactory.load()

  def init(): Int = {
    logInfo("Initialising broker")
    ctx = ZMQ.context(1)
    frontend = ctx.socket(SocketType.ROUTER)
    logInfo("Broker: starting frontend router socket on " + port)
    frontend.bind(s"tcp://$host:$port")
    logDebug("Initialising poller")
    poller = ctx.poller(1)
    val polFrontendIndex = poller.register(frontend, ZMQ.Poller.POLLIN)
    logDebug("initialised brocker")
    logDebug(
      "broker :\n" +
        " polFrontendIndex: \n" + polFrontendIndex
    )
    polFrontendIndex
  }

  override def run(): Unit = {
    val initRes = init()
    logDebug("Broker thread was started")
    try {
      while (!Thread.currentThread().isInterrupted) {
        try {

          val rc = poller.poll(Try(config.getLong("org.mixql.cluster.broker.pollerTimeout")).getOrElse(100))
          if (rc == -1)
            throw new BrakeException()
          logDebug("ThreadInterrupted: " + Thread.currentThread().isInterrupted)
          // Receive messages from engines
          if (poller.pollin(initRes)) {
            receiveMessageFromFrontend() match {
              case m: IBrokerReceiverFromModule =>
                logDebug("Received message for broker from engine")
                reactOnEngineMsgForBroker(m)
              case m: IModuleSendToClient => sendMessageToFrontend(m.clientIdentity(), m.toByteArray)
              case m: IBrokerReceiverFromClient =>
                logDebug("Received message for broker from client")
                reactOnClientMsgForBroker(m)
              case m: IModuleReceiver =>
                logDebug(s"Received message for engine ${m.moduleIdentity()} from client ${m.clientIdentity()}")
                if (m.isInstanceOf[ShutDown]) {
                  logDebug(s"Delete engine ${m.moduleIdentity()} from engine's list")
                  engines = engines.dropWhile(t => t == m.moduleIdentity().trim)
                  logDebug(s"Delete engine ${m.moduleIdentity()} from enginesPingHeartBeatTimeout's list")
                  enginesPingHeartBeatTimeout = enginesPingHeartBeatTimeout
                    .dropWhile(t => t._1 == m.moduleIdentity().trim)
                  logDebug(s"Delete engine ${m.moduleIdentity()} from enginesStartedTimeOut's list")
                  enginesStartedTimeOut = enginesStartedTimeOut.dropWhile(t => t._1 == m.moduleIdentity().trim)
                  sendMessageToFrontend(m.moduleIdentity(), m.toByteArray)
                } else {
                  if !engines.contains(m.moduleIdentity()) then
                    logDebug(s"Broker frontend: engine was not initialized yet. Stash message in engines map")
                    stashMessage(m.moduleIdentity(), m.clientIdentity(), m.toByteArray)
                  else
                    clientsThatSentToEngine.get(m.moduleIdentity()) match {
                      case Some(value) => value.add(m.clientIdentity())
//                        clientsThatSentToEngine.put(m.moduleIdentity(), value)
                      case None => clientsThatSentToEngine.put(m.moduleIdentity(), mutable.Set(m.clientIdentity()))
                    }
                    sendMessageToFrontend(m.moduleIdentity(), m.toByteArray)
                }
            }
          }
          processTimeOutForStartedEngines()
          processPingsHeartBeatForStartedEngines()
        } catch {
          case e: BrakeException => throw e
          case e: Throwable =>
            logError(
              "Broker main thread:\n Got Exception: \n" + e.getMessage +
                "\n target exception stacktrace: \n" + ErrorOps.stackTraceToString(e)
            )
          // TO-DO Send broker error to clients
        }
      }
    } catch {
      case e: BrakeException =>
    } finally {
      try {
        if frontend != null then
          logInfo("Broker: closing frontend")
          runWithTimeout(5000) {
            frontend.close()
          }
      } catch {
        case e: Throwable => logError("Warning error while closing frontend socket in broker: " + e.getMessage)
      }

      try {
        if poller != null then {
          logInfo("Broker: close poll")
          runWithTimeout(5000) {
            poller.close()
          }
        }
      } catch {
        case e: Throwable => logError("Warning error while closing poller in broker: " + e.getMessage)
      }

      try {
        if ctx != null then {
          logInfo("Broker: terminate context")
          //          ctx.term()
          runWithTimeout(5000) {
            ctx.close()
          }
        }
      } catch {
        case e: Throwable => logError("Warning error while closing broker context: " + e.getMessage)
      }
    }
  }

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent._
  import scala.concurrent.duration._

  def runWithTimeout[T](timeoutMs: Long)(f: => T): Option[T] = {
    Some(Await.result(Future(f), timeoutMs milliseconds))
  }

  def processTimeOutForStartedEngines(): Unit = {
    // Name of engine, clientsIdentity set
    val engineFailedSet = mutable.Map[String, mutable.Set[String]]()

    enginesStartedTimeOut.foreach(tuple => {
      val engineName = tuple._1
      val timeOut = tuple._2._1
      val processStart = tuple._2._2

      val elapsed = (processStart to DateTime.now()).millis
      if (elapsed > timeOut) {
        logError("Broker: elapsed timeout for engine " + engineName)
        // Added here clientIdentity of sender of EngineStarted
        engineFailedSet.put(engineName, mutable.Set[String](tuple._2._3))
      }
    })

    engineFailedSet.keySet.foreach(failedEngineName => {
      enginesStartedTimeOut = enginesStartedTimeOut.dropWhile(p => {
        p._1 == failedEngineName
      })
    })

    // Add clientIdentities which sent messages to engine and they where stashed
    engineFailedSet.foreach(failedEngine => {
      enginesStashedMsgs.get(failedEngine._1) match
        case Some(messages) =>
          if (messages.nonEmpty) {
            messages.foreach(msg => engineFailedSet.apply(failedEngine._1).add(msg.ClientAddr))
            messages.clear() // Do we need to clear messages?
          }
        case None =>
      end match
    })

    engineFailedSet.foreach(engineFailed => {
      val engineFailedName = engineFailed._1
      val clientIdentities = engineFailed._2
      clientIdentities.foreach(clientIdentity =>
        logInfo("Broker: send error message EngineStartedTimeOutElapsedError to client " + clientIdentity)
        sendMessageToFrontend(
          clientIdentity,
          new EngineStartedTimeOutElapsedError(
            engineFailedName,
            "Broker: elapsed timeout for engine " + engineFailedName
          ).toByteArray
        )
      )
    })
  }

  def processPingsHeartBeatForStartedEngines(): Unit = {
    // Name of engine, clientsIdentity set
    val engineFailedSet = mutable.Set[String]()

    enginesPingHeartBeatTimeout.foreach(tuple => {
      val engineName = tuple._1
      val heartBeatInterval = tuple._2._1
      val processStart = tuple._2._2
      val pollerTimeout = tuple._2._3
      var liveness = tuple._2._4
      val bias = (heartBeatInterval + pollerTimeout) * 0.05 // More if network is worse
      val timeOut = heartBeatInterval + pollerTimeout + bias

      val elapsed = (processStart to DateTime.now()).millis
      if (elapsed > timeOut) {
        liveness = liveness - 1

        val errorMsg =
          "Broker: elapsed ping timeout for engine " + engineName +
            " Expected to receive ping heartbeat less then " + timeOut +
            "\nLiveness: " + liveness
        if (liveness < 0) {
          logError(errorMsg)
          // Added here clientIdentity of sender of EngineStarted
          engineFailedSet.add(engineName)
        } else {
          logWarn(errorMsg)
          // Set updated liveness
          enginesPingHeartBeatTimeout.put(engineName, (heartBeatInterval, DateTime.now(), pollerTimeout, liveness))
        }

      }
    })

    engineFailedSet.foreach(failedEngineName => {
      enginesPingHeartBeatTimeout = enginesPingHeartBeatTimeout.dropWhile(p => {
        p._1 == failedEngineName
      })
      logDebug("enginesPingHeartBeatTimeout: " + enginesPingHeartBeatTimeout.mkString(","))
      logDebug(s"Delete engine ${failedEngineName} from engine's list")
      engines = engines.dropWhile(t => t == failedEngineName.trim)
      logDebug("engines: " + engines.mkString(","))

      clientsThatSentToEngine.get(failedEngineName) match {
        case Some(clientIdentities) =>
          clientIdentities.foreach(clientIdentity =>
            logDebug("Broker: send error message EnginePingTimeOutElapsedError to client " + clientIdentity)
            sendMessageToFrontend(
              clientIdentity,
//              new EnginePingTimeOutElapsedError( //TO-DO
              new EngineStartedTimeOutElapsedError(
                failedEngineName,
                "Broker: elapsed timeout for engine " + failedEngineName
              ).toByteArray
            )
          )
        case None =>
      }
      clientsThatSentToEngine = clientsThatSentToEngine.dropWhile(t => t._1 == failedEngineName.trim)
      logDebug("clientsThatSentToEngine: " + clientsThatSentToEngine.mkString(","))
    })
  }

  private def reactOnClientMsgForBroker(m: IBrokerReceiverFromClient): Unit = {
    m match
      case msg: EngineStarted =>
        logInfo(
          s"Received notification about started engine ${msg.engineName} from client " +
            msg.clientIdentity()
        )
        if (!engines.contains(msg.engineName)) {
          val t = (msg.getTimeout, DateTime.now(), msg.clientIdentity())
          enginesStartedTimeOut.put(msg.engineName, t)
        }
  }

  private def reactOnEngineMsgForBroker(m: IBrokerReceiverFromModule): Unit = {
    m match
      case msg: EngineIsReady =>
        // Its READY message from engine
        logInfo("Received EngineIsReady from engine " + msg.engineName())

        enginesStartedTimeOut = enginesStartedTimeOut.dropWhile(t => t._1 == msg.engineName())

        val t = (msg.getHeartBeatInterval.toString.toLong, DateTime.now(), msg.getPollerTimeout.toString.toLong, 3)
        enginesPingHeartBeatTimeout.put(msg.engineName, t)

        if !engines.contains(msg.engineName()) then
          logDebug(s"Broker: Add ${msg.engineName()} as key in engines set")
          engines.add(msg.engineName()) // only this thread will write, so there will be no race condition
          sendStashedMessagesToBackendIfTheyAre(msg.engineName())

      case msg: EngineFailed =>
        logError("Received EngineFailed error from engine " + msg.engineName())
        // TO-DO Properly react on EngineFailed
        throw new UnsupportedOperationException(msg.getErrorMessage)
      case msg: EnginePingHeartBeat => // Its heart beat message from engine
        logDebug(s"Broker: received EnginePingHeartBeat message from engine " + msg.engineName())
        if engines.contains(msg.engineName()) then
          val t = enginesPingHeartBeatTimeout(msg.engineName())
          enginesPingHeartBeatTimeout.put(msg.engineName(), (t._1, DateTime.now(), t._3, 3))
          sendMessageToFrontend(msg.engineName(), new PlatformPongHeartBeat().toByteArray)
        else
          logWarn(
            s"Broker: ignored EnginePingHeartBeat message from engine " + msg
              .engineName() + " as it was not in list of registered engines: " + engines.mkString(",")
          )
  }

  private def receiveMessageFromFrontend(): Message = {
    ////// Identity frame was added by ROUTER socket////////////////////////
    val clientAddr = frontend.recv()
    val clientAddrStr = String(clientAddr)
    ///////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////
    // Main msg, contains engine's address and message, separated by empty frame//
    //////////////////////////////////////////////////////////////////////////////
    val request = frontend.recv()
    val requestStr: String = new String(request)
    logDebug(s"Broker frontend: received request [$requestStr] for engine module from $clientAddrStr")
    try {
      RemoteMessageConverter.unpackAnyMsgFromArray(request) match {
        case m: IBrokerReceiverFromModule => m.setEngineName(clientAddrStr)
        case m: IModuleSendToClient       => m
        case m: IBrokerReceiverFromClient =>
          logInfo("Broker frontend: received client's identity " + clientAddrStr)
          m.SetClientIdentity(clientAddrStr)
        case m: IModuleReceiver =>
          logInfo("Broker frontend: received client's identity " + clientAddrStr)
          m.SetClientIdentity(clientAddrStr)
      }
    } catch
      case e: Throwable =>
        val msg = logError(
          s"Broker:\n receiveMessageFromFrontend: \n" +
            "Could not convert received message from byte array to Message class: \n" +
            e.getMessage + "\n target exception stacktrace: \n" + ErrorOps.stackTraceToString(e)
        )
        sendMessageToFrontend(clientAddrStr, new CouldNotConvertMsgError(msg).toByteArray)
        throw e
    //////////////////////////////////////////////////////////////////////////////
  }

  private def sendMessageToFrontend(clientIDStr: String, msg: Array[Byte]) = {
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // About ROUTER socket:                                                                                   //
    // The peer application sets the ZMQ_IDENTITY option of its peer socket(DEALER or REQ) before             //
    // binding or connecting. Usually the peer then connects to the already -bound ROUTER socket.            //
    // But the ROUTER can also connect to the peer. At connection time, the peer socket tells                //
    // the router socket, “please use this identity for this connection”.                                    //
    //                                                                                                       //
    // If the peer socket doesn’t say that, the router generates its usual arbitrary random identity          //
    // for the connection.                                                                                    //
    //                                                                                                       //
    // The ROUTER socket now provides this logical address to the application as a prefix identity frame     //
    // for any messages coming in from that peer.                                                            //
    // The ROUTER also expects the logical address as the prefix identity frame for any outgoing messages.   //
    //                                                                                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    logDebug(s"Broker backend : sending clientId $clientIDStr to frontend")
    frontend.send(clientIDStr.getBytes, ZMQ.SNDMORE)
    logDebug(s"sending epmpty frame to $clientIDStr to backend")
    frontend.send("".getBytes(), ZMQ.SNDMORE)
    logDebug(s"Broker backend : sending protobuf message to frontend")
    frontend.send(msg)
  }

  private def sendStashedMessagesToBackendIfTheyAre(workerAddrStr: String): Unit = {
    logDebug(s"Broker: Check if there are stashed messages for our engine")
    enginesStashedMsgs.get(workerAddrStr) match
      case Some(messages) =>
        if messages.isEmpty then logDebug(s"Broker: Checked engines map. No stashed messages for $workerAddrStr")
        else
          logDebug(
            s"Broker: Have founded stashed messages (amount: ${messages.length}) " +
              s"for engine $workerAddrStr. Sending them"
          )
          messages.foreach(msg => sendMessageToFrontend(workerAddrStr, msg.request))
          //                  engines.put(workerAddrStr, ListBuffer())
          messages.clear()

      case None => logWarn(s"Broker: warning! no key $workerAddrStr, thow it should be here! Strange!")
    end match
  }

  private def stashMessage(engineIdentityStr: String, clientAddrStr: String, request: Array[Byte]) = {
    if !enginesStashedMsgs.contains(engineIdentityStr) then
      enginesStashedMsgs.put(engineIdentityStr, ListBuffer(StashedClientMessage(clientAddrStr, request)))
    else
      enginesStashedMsgs.get(engineIdentityStr) match
        case Some(messages) => messages += StashedClientMessage(clientAddrStr, request)
        case None =>
          logWarn(
            s"Broker frontend: warning! no key $engineIdentityStr in enginesStashedMsgs," +
              s" thow it should be here! Strange!"
          )
          enginesStashedMsgs.put(engineIdentityStr, ListBuffer(StashedClientMessage(clientAddrStr, request)))
      end match
    end if
  }
}
