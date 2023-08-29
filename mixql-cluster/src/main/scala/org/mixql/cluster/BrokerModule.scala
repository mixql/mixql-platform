package org.mixql.cluster

import logger.*
import org.zeromq.{SocketType, ZMQ}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object BrokerModule {
  var ctx: ZMQ.Context = null
  var frontend: ZMQ.Socket = null
  var backend: ZMQ.Socket = null
  var poller: ZMQ.Poller = null
  var threadBroker: Thread = null

  // Key is identity, Value is list of messages
  val enginesStashedMsgs: mutable.Map[String, ListBuffer[StashedClientMessage]] = mutable.Map()
  val engines: mutable.Set[String] = mutable.Set()
  val NOFLAGS = 0
}

class BrokerModule(portFrontend: Int, portBackend: Int, host: String) extends java.lang.AutoCloseable {

  import BrokerModule.*

  def getPortFrontend = portFrontend

  def getPortBackend = portBackend

  def getHost = host

  def start() = {
    if threadBroker == null then
      logInfo("Starting broker thread")
      threadBroker = new BrokerMainRunnable("BrokerMainThread", host, portFrontend.toString, portBackend.toString)
      threadBroker.start()
  }

  override def close() = {
    if (threadBroker != null && threadBroker.isAlive() && !threadBroker.isInterrupted)
      logDebug("Broker: Executing close")
      logDebug("Broker: send interrupt to thread")
      threadBroker.interrupt()
    //      println("Waiting while broker thread is alive")
    //      try {
    //        threadBroker.join();
    //      }
    //      catch
    //        case _: InterruptedException => System.out.printf("%s has been interrupted", threadBroker.getName())
    //      println("server: Broker was shutdown")
  }
}

class BrokerMainRunnable(name: String, host: String, portFrontend: String, portBackend: String) extends Thread(name) {

  import BrokerModule.*

  def init(): (Int, Int) = {
    logInfo("Initialising broker")
    ctx = ZMQ.context(1)
    frontend = ctx.socket(SocketType.ROUTER)
    backend = ctx.socket(SocketType.ROUTER)
    logInfo("Broker: starting frontend router socket on " + portFrontend.toString)
    frontend.bind(s"tcp://$host:${portFrontend.toString}")
    logInfo("Broker: starting backend router socket on " + portBackend.toString)
    backend.bind(s"tcp://$host:${portBackend.toString}")
    logDebug("Initialising poller")
    poller = ctx.poller(2)
    val polBackendIndex = poller.register(backend, ZMQ.Poller.POLLIN)
    val polFrontendIndex = poller.register(frontend, ZMQ.Poller.POLLIN)
    logDebug("initialised brocker")
    logDebug(
      "broker : polBackendIndex: " + polBackendIndex +
        " polFrontendIndex: " + polFrontendIndex
    )
    (polBackendIndex, polFrontendIndex)
  }

  override def run(): Unit = {
    val initRes = init()
    logDebug("Broker thread was started")
    try {
      while (!Thread.currentThread().isInterrupted()) {
        val rc = poller.poll(1000)
        if (rc == -1)
          throw Exception("brake")
        logDebug("ThreadInterrupted: " + Thread.currentThread().isInterrupted())
        // Receive messages from engines
        if (poller.pollin(initRes._1)) {
          val (workerAddrStr, ready, clientIDStr, msg, pingHeartBeatMsg) = receiveMessageFromBackend()
          ready match {
            case Some(_) => // Its READY message from engine
              if !engines.contains(workerAddrStr) then
                logDebug(s"Broker: Add $workerAddrStr as key in engines set")
                engines.add(workerAddrStr) // only this thread will write, so there will be no race condition
                sendStashedMessagesToBackendIfTheyAre(workerAddrStr)
            case None => // its message from engine to client or heart beat message from engine
              pingHeartBeatMsg match {
                case Some(_) => // Its heart beat message from engine
                  sendMessageToBackend(
                    s"Broker backend heart beat pong:",
                    workerAddrStr, {
                      "PONG-HEARTBEAT".getBytes
                    }
                  )
                case None => // its message from engine to client
                  sendMessageToFrontend(clientIDStr.get, msg.get)
              }
          }
        }
        if (poller.pollin(initRes._2)) {
          val (clientAddrStr, engineIdentityStr, request) = receiveMessageFromFrontend()
          if !engines.contains(engineIdentityStr) then
            logDebug(s"Broker frontend: engine was not initialized yet. Stash message in engines map")
            stashMessage(engineIdentityStr, clientAddrStr, request)
          else sendMessageToBackend("Broker frontend", engineIdentityStr, clientAddrStr, request)
        }
      }
    } catch {
      case e: Throwable => logError("Broker main thread: Got Exception: " + e.getMessage)
    } finally {
      try {
        if (backend != null) {
          logDebug("Broker: closing backend")
          backend.close()
        }
      } catch {
        case e: Throwable => logError("Warning error while closing backend socket in broker: " + e.getMessage)
      }

      try {
        if frontend != null then
          logDebug("Broker: closing frontend")
          frontend.close()
      } catch {
        case e: Throwable => logError("Warning error while closing frontend socket in broker: " + e.getMessage)
      }

      try {
        if poller != null then {
          logDebug("Broker: close poll")
          poller.close()
        }
      } catch {
        case e: Throwable => logError("Warning error while closing poller in broker: " + e.getMessage)
      }

      try {
        if ctx != null then {
          logDebug("Broker: terminate context")
          //          ctx.term()
          ctx.close()
        }
      } catch {
        case e: Throwable => logError("Warning error while closing broker context: " + e.getMessage)
      }
    }
    logDebug("Broker thread finished...")
  }

  private def receiveMessageFromBackend()
    : (String, Option[String], Option[String], Option[Array[Byte]], Option[String]) = {
    // FOR PROTOCOL SEE BOOK OReilly ZeroMQ Messaging for any applications 2013 ~page 100
    val workerAddr = backend.recv(NOFLAGS) // Received engine module identity frame
    val workerAddrStr = String(workerAddr)
    logDebug(s"Broker backend : received identity $workerAddrStr from engine module")
    backend.recv(NOFLAGS) // received empty frame
    logDebug(s"Broker backend : received empty frame  from engine module $workerAddrStr")
    // Third frame is READY message or client identity frame or heart beat message from engine
    val clientID = backend.recv(NOFLAGS)
    var clientIDStr: Option[String] = Some(String(clientID))
    var msg: Option[Array[Byte]] = None
    var ready: Option[String] = None
    var pingHeartBeat: Option[String] = None

    if clientIDStr.get != "READY" then
      if (clientIDStr.get == "PING-HEARTBEAT") then
        logDebug(s"Broker: received PING-HEARTBEAT msg from engine module $workerAddrStr")
        pingHeartBeat = Some(clientIDStr.get)
      else
        // Its client's identity
        logDebug(s"Broker backend : received client's identity $clientIDStr")
        backend.recv(NOFLAGS) // received empty frame
        logDebug(s"Broker backend : received empty frame  from engine module $workerAddrStr")
        msg = Some(backend.recv(NOFLAGS))
        logDebug(s"Broker backend : received protobuf message from engine module $workerAddrStr")
      end if
    else
      logDebug(s"Broker: received READY msg from engine module $workerAddrStr")
      ready = Some(clientIDStr.get)
      clientIDStr = None
    end if

    (workerAddrStr, ready, clientIDStr, msg, pingHeartBeat)
  }

  private def receiveMessageFromFrontend(): (String, String, Array[Byte]) = {
    val clientAddr = frontend.recv()
    val clientAddrStr = String(clientAddr)
    logDebug("Broker frontend: received client's identity " + clientAddrStr)
    frontend.recv()
    logDebug(s"Broker frontend: received empty frame from $clientAddrStr")
    val engineIdentity = frontend.recv()
    val engineIdentityStr = String(engineIdentity)
    logDebug(s"Broker frontend: received engine module identity $engineIdentityStr from $clientAddrStr")
    frontend.recv()
    logDebug(s"Broker frontend: received empty frame from $clientAddrStr")
    val request = frontend.recv()
    logDebug(s"Broker frontend: received request for engine module $engineIdentityStr from $clientAddrStr")
    (clientAddrStr, engineIdentityStr, request)
  }

  private def sendMessageToFrontend(clientIDStr: String, msg: Array[Byte]) = {
    logDebug(s"Broker backend : sending clientId $clientIDStr to frontend")
    frontend.send(clientIDStr.getBytes, ZMQ.SNDMORE)
    logDebug(s"Broker backend : sending empty frame to frontend")
    frontend.send("".getBytes, ZMQ.SNDMORE)
    logDebug(s"Broker backend : sending protobuf message to frontend")
    frontend.send(msg)
  }

  private def sendMessageToBackend(logMessagePrefix: String,
                                   engineIdentityStr: String,
                                   clientAddrStr: String,
                                   request: Array[Byte]) = {
    logDebug(s"$logMessagePrefix: sending $engineIdentityStr from $clientAddrStr to backend")
    backend.send(engineIdentityStr.getBytes, ZMQ.SNDMORE)
    logDebug(s"$logMessagePrefix: sending epmpty frame to $engineIdentityStr from $clientAddrStr to backend")
    backend.send("".getBytes(), ZMQ.SNDMORE)
    logDebug(s"$logMessagePrefix: sending clientAddr to $engineIdentityStr from $clientAddrStr to backend")
    backend.send(clientAddrStr.getBytes, ZMQ.SNDMORE)
    logDebug(s"$logMessagePrefix: sending epmpty frame to $engineIdentityStr from $clientAddrStr to backend")
    backend.send("".getBytes(), ZMQ.SNDMORE)
    logDebug(s"$logMessagePrefix: sending protobuf frame to $engineIdentityStr from $clientAddrStr to backend")
    backend.send(request, NOFLAGS)
  }

  private def sendMessageToBackend(logMessagePrefix: String, engineIdentityStr: String, request: Array[Byte]) = {
    logDebug(s"$logMessagePrefix: sending $engineIdentityStr  to backend")
    backend.send(engineIdentityStr.getBytes, ZMQ.SNDMORE)
    logDebug(s"$logMessagePrefix: sending epmpty frame to $engineIdentityStr to backend")
    backend.send("".getBytes(), ZMQ.SNDMORE)
    logDebug(s"$logMessagePrefix: sending message frame to $engineIdentityStr to backend")
    backend.send(request, NOFLAGS)
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
          messages.foreach(msg => sendMessageToBackend("Broker stashed: ", workerAddrStr, msg.ClientAddr, msg.request))
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
