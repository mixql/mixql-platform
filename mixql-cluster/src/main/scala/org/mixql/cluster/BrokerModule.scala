package org.mixql.cluster

import logger.*
import org.mixql.engine.core.BrakeException
import org.mixql.protobuf.ErrorOps
import org.mixql.remote.RemoteMessageConverter
import org.zeromq.{SocketType, ZMQ}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.broker.{CouldNotConvertMsgError, PlatformPongHeartBeat}
import org.mixql.remote.messages.module.IModuleSendToClient
import org.mixql.remote.messages.module.toBroker.{
  EngineFailed,
  EngineIsReady,
  EnginePingHeartBeat,
  IBrokerReceiverFromModule
}
import org.mixql.remote.messages.client.toBroker.{EngineStarted, IBrokerReceiverFromClient}
import org.mixql.remote.messages.client.IModuleReceiver

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
  val engines: mutable.Set[String] = mutable.Set()
  val NOFLAGS = 0

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

          val rc = poller.poll(1000)
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
                if !engines.contains(m.moduleIdentity()) then
                  logDebug(s"Broker frontend: engine was not initialized yet. Stash message in engines map")
                  stashMessage(m.moduleIdentity(), m.clientIdentity(), m.toByteArray)
                else sendMessageToFrontend(m.moduleIdentity(), m.toByteArray)
            }
          }
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
          frontend.close()
      } catch {
        case e: Throwable => logError("Warning error while closing frontend socket in broker: " + e.getMessage)
      }

      try {
        if poller != null then {
          logInfo("Broker: close poll")
          poller.close()
        }
      } catch {
        case e: Throwable => logError("Warning error while closing poller in broker: " + e.getMessage)
      }

      try {
        if ctx != null then {
          logInfo("Broker: terminate context")
          //          ctx.term()
          ctx.close()
        }
      } catch {
        case e: Throwable => logError("Warning error while closing broker context: " + e.getMessage)
      }
    }
  }

  private def reactOnClientMsgForBroker(m: IBrokerReceiverFromClient): Unit = {
    m match
      case msg: EngineStarted =>
        logInfo(
          s"Received notification about started engine ${msg.engineName} from client " +
            msg.clientIdentity()
        )
      // TO-DO Properly react on EngineStarted message
  }

  private def reactOnEngineMsgForBroker(m: IBrokerReceiverFromModule): Unit = {
    m match
      case msg: EngineIsReady =>
        // Its READY message from engine
        logInfo("Received EngineIsReady from engine " + msg.engineName())
        if !engines.contains(msg.engineName()) then
          logDebug(s"Broker: Add ${msg.engineName()} as key in engines set")
          engines.add(msg.engineName()) // only this thread will write, so there will be no race condition
          sendStashedMessagesToBackendIfTheyAre(msg.engineName())
      case msg: EngineFailed =>
        logError("Received EngineFailed error from engine " + msg.engineName())
        // TO-DO Properly react on EngineFailed
        throw new UnsupportedOperationException(msg.getErrorMessage)
      case msg: EnginePingHeartBeat => // Its heart beat message from engine
        sendMessageToFrontend(msg.engineName(), new PlatformPongHeartBeat().toByteArray)
  }

  private def receiveMessageFromFrontend(): Message = {
    ////// Identity frame was added by ROUTER socket////////////////////////
    val clientAddr = frontend.recv()
    val clientAddrStr = String(clientAddr)
    logDebug("Broker frontend: received client's identity " + clientAddrStr)
    ///////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////
    // Main msg, contains engine's address and message, separated by empty frame//
    //////////////////////////////////////////////////////////////////////////////
    val request = frontend.recv()
    val requestStr: String = new String(request)
    logDebug(s"Broker frontend: received request [$requestStr] for engine module from $clientAddrStr")
    try {
      RemoteMessageConverter.unpackAnyMsgFromArray(request)
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
