package org.mixql.cluster

import com.typesafe.config.ConfigFactory
import org.mixql.cluster.logger.{logDebug, logError, logInfo, logWarn}
import org.mixql.core.engine.Engine
import org.mixql.core.context.mtype.{MType, unpack}
import org.mixql.net.PortOperations
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter, messages}
import org.zeromq.{SocketType, ZMQ}

import java.io.File
import java.net.{InetSocketAddress, SocketAddress}
import java.nio.channels.{ServerSocketChannel, SocketChannel}
import scala.concurrent.{Await, Future}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.{Random, Try}
import org.mixql.core.context.{EngineContext, mtype}
import org.mixql.remote.messages.rtype.Param
import org.mixql.remote.messages.rtype.mtype.IGtypeMessage
import org.mixql.remote.messages.client.toBroker.EngineStarted
import org.mixql.remote.messages.client.{
  Execute,
  ExecuteFunction,
  IModuleReceiver,
  InvokedPlatformFunctionResult,
  PlatformVar,
  PlatformVarWasSet,
  PlatformVars,
  PlatformVarsNames,
  PlatformVarsWereSet,
  ShutDown
}
import org.mixql.remote.messages.module.{
  ExecuteResult,
  ExecutedFunctionResult,
  ExecutedFunctionResultFailed,
  IModuleSendToClient
}
import org.mixql.remote.messages.{Message, module}
import org.mixql.remote.messages.module.worker.{
  GetPlatformVar,
  GetPlatformVars,
  GetPlatformVarsNames,
  InvokeFunction,
  SetPlatformVar,
  SetPlatformVars
}
import scalapb.options.ScalaPbOptions.OptionsScope

import java.lang.Thread.sleep
import scala.annotation.tailrec
import scala.language.postfixOps

object ClientModule {
  val config = ConfigFactory.load()
}

//if start script name is not none then client must start remote engine by executing script
//which is {basePath}/{startScriptName}. P.S executor will be ignored
//if executor is not none and startScriptName is none then execute it in scala future
//if executor is none and startScript is none then just connect
class ClientModule(clientIdentity: String,
                   moduleIdentity: String,
                   startScriptName: Option[String],
                   executor: Option[IExecutor],
                   hostArgs: Option[String],
                   portFrontendArgs: Option[Int],
                   basePathArgs: Option[File],
                   startEngineTimeOut: Long,
                   startScriptExtraOpts: Option[String] = None)
    extends Engine
    with java.lang.AutoCloseable {
//  var client: ZMQ.Socket = null
  protected var _ctx: ZMQ.Context = null

  def ctx(): ZMQ.Context =
    if (_ctx == null) {
      _ctx = ZMQ.context(1)
      _ctx
    } else
      _ctx

  var clientRemoteProcess: sys.process.Process = null
  var clientFuture: Future[Unit] = null
  var moduleStarted: Boolean = false

  override def name: String = clientIdentity

  override def executeImpl(stmt: String, ctx: EngineContext): MType = {
    logInfo(s"[ClientModule-$clientIdentity]: module $moduleIdentity was triggered by execute request")

    var client: ZMQ.Socket = null
    try {
      client = initClientSocket()
      sendMsg(Execute(moduleIdentity, stmt), client)
      reactOnRequest(recvMsg(client), ctx, client)
    } finally {
      closeClientSocket(client)
    }
  }

  val clientSocketsIdentitiesSet: mutable.Set[String] = mutable.Set()
  val r: Random.type = scala.util.Random

  def generateUnusedClientSocketsIdentity(): String = {
    val numPattern = "[0-9]+".r
    val ids = clientSocketsIdentitiesSet.map(name => numPattern.findFirstIn(name).get.toInt)

    var foundUniqueId = false
    var id = -1;
    while (!foundUniqueId) {
      id = r.nextInt().abs
      ids.find(p => p == id) match {
        case Some(_) =>
        case None    => foundUniqueId = true
      }
    }
    s"$clientIdentity$id"
  }

  protected def initClientSocket(): ZMQ.Socket = {

    ClientModule.synchronized {
      if (!BrokerModule.wasStarted)
        startBroker()
    }

    val client = this.ctx().socket(SocketType.DEALER)
    this.synchronized {
      val clientIdentity = generateUnusedClientSocketsIdentity()
      client.setIdentity(clientIdentity.getBytes)
      clientSocketsIdentitiesSet.add(clientIdentity)
    }
    logInfo(s"[ClientModule-$clientIdentity]: created client socket with identity " + String(client.getIdentity))
    logInfo(
      s"server: Clientmodule ${String(client.getIdentity)} connected to " +
        s"tcp://${BrokerModule.getHost.get}:${BrokerModule.getPort.get} " + client
          .connect(s"tcp://${BrokerModule.getHost.get}:${BrokerModule.getPort.get}")
    )
    client
  }

  override def executeFuncImpl(name: String, ctx: EngineContext, kwargs: Map[String, Object], params: MType*): MType = {
    if (kwargs.nonEmpty)
      throw new UnsupportedOperationException("named arguments are not supported in functions in remote engine " + name)

    logInfo(s"[ClientModule-$clientIdentity]: module $moduleIdentity was triggered by executeFunc request")
    var client: ZMQ.Socket = null
    try {
      client = initClientSocket()
      sendMsg(
        ExecuteFunction(moduleIdentity, name, params.map(gParam => GtypeConverter.toGeneratedMsg(gParam)).toArray),
        client
      )
      reactOnRequest(recvMsg(client), ctx, client)
    } finally {
      closeClientSocket(client)
    }
  }

  override def getDefinedFunctions(): List[String] = {
    if (!engineStarted) {
      logInfo(s"[ClientModule-$clientIdentity]: module $moduleIdentity was triggered by getDefinedFunctions request")
    }
    engineStarted = true

    logInfo(s"Server: ClientModule: $clientIdentity: ask defined functions from remote engine")
    var client: ZMQ.Socket = null
    val functionsList =
      try {
        client = initClientSocket()

        sendMsg(messages.client.GetDefinedFunctions(moduleIdentity), client)

        recvMsg(client) match {
          case m: messages.module.DefinedFunctions => m.arr.toList
          case ex: org.mixql.remote.messages.rtype.Error =>
            val errorMessage =
              s"Server: ClientModule: ${String(client.getIdentity)}: getDefinedFunctions error: \n" + ex.getErrorMessage
            logError(errorMessage)
            throw new Exception(errorMessage)
          case m: messages.Message =>
            val errorMessage =
              s"Server: ClientModule: ${String(client.getIdentity)}: getDefinedFunctions error: \n" +
                "Unknown message " + m.`type`() + " received"
            logError(errorMessage)
            throw new Exception(errorMessage)
        }
      } finally {
        closeClientSocket(client)
      }

    if functionsList.isEmpty then Nil
    else functionsList
  }

  def closeClientSocket(client: ZMQ.Socket) = {
    if (client != null) {
      val clientSocketIdentity = String(client.getIdentity)
      logInfo(s"Server: ClientModule: close socket with identity " + clientSocketIdentity)
      Try(if (client != null) {
        logInfo(s"Server: ClientModule: ${String(client.getIdentity)}: close client socket ")
        runWithTimeout(5000) {
          client.close()
        }
      })
      this.synchronized {
        clientSocketsIdentitiesSet.remove(clientSocketIdentity)
      }
    }
  }

  @tailrec
  private def reactOnRequest(msg: Message, ctx: EngineContext, client: ZMQ.Socket): MType = {
    msg match
      case msg: IGtypeMessage => msg.toGType
      case m: IModuleSendToClient =>
        m match
          case msg: GetPlatformVar =>
            val v = ctx.getVar(msg.name)
            sendMsg(
              new PlatformVar(moduleIdentity, msg.workerIdentity(), msg.name, GtypeConverter.toGeneratedMsg(v)),
              client
            )
            reactOnRequest(recvMsg(client), ctx, client)
          case msg: SetPlatformVar =>
            ctx.setVar(msg.name, GtypeConverter.messageToGtype(msg.msg))
            sendMsg(new PlatformVarWasSet(moduleIdentity, msg.workerIdentity(), msg.name), client)
            reactOnRequest(recvMsg(client), ctx, client)
          case msg: GetPlatformVars =>
            val valMap = ctx.getVars(msg.names.toList)
            sendMsg(
              new PlatformVars(
                moduleIdentity,
                msg.workerIdentity(),
                valMap.map(t => new Param(t._1, GtypeConverter.toGeneratedMsg(t._2))).toArray
              ),
              client
            )
            reactOnRequest(recvMsg(client), ctx, client)
          case msg: SetPlatformVars =>
            import collection.JavaConverters._
            ctx.setVars(msg.vars.asScala.map(t => t._1 -> GtypeConverter.messageToGtype(t._2)))
            sendMsg(
              new PlatformVarsWereSet(
                moduleIdentity,
                msg.workerIdentity(),
                new java.util.ArrayList[String](msg.vars.keySet())
              ),
              client
            )
            reactOnRequest(recvMsg(client), ctx, client)
          case msg: GetPlatformVarsNames =>
            sendMsg(new PlatformVarsNames(moduleIdentity, msg.workerIdentity(), ctx.getVarsNames().toArray), client)
            reactOnRequest(recvMsg(client), ctx, client)
          case msg: InvokeFunction =>
//            try {
            val res = ctx
              .invokeFunction(msg.name, msg.args.map(arg => unpack(GtypeConverter.messageToGtype(arg))).toList)
            sendMsg(
              new InvokedPlatformFunctionResult(
                moduleIdentity,
                msg.workerIdentity(),
                msg.name,
                GtypeConverter.toGeneratedMsg(res)
              ),
              client
            )
            reactOnRequest(recvMsg(client), ctx, client)
          case msg: ExecutedFunctionResult =>
            if (msg.msg.isInstanceOf[org.mixql.remote.messages.rtype.Error]) {
              logError(
                s"Server: ClientModule: ${String(client.getIdentity)} Error while executing function " + msg
                  .functionName + "error: " +
                  msg.msg.asInstanceOf[org.mixql.remote.messages.rtype.Error].getErrorMessage
              )
              throw new Exception(msg.msg.asInstanceOf[org.mixql.remote.messages.rtype.Error].getErrorMessage)
            }
            GtypeConverter.messageToGtype(msg.msg)
          case msg: org.mixql.remote.messages.rtype.Error =>
            logError(
              s"Server: ClientModule: ${String(client.getIdentity)}:" + msg
                .asInstanceOf[org.mixql.remote.messages.rtype.Error].getErrorMessage
            )
            throw new Exception(msg.asInstanceOf[org.mixql.remote.messages.rtype.Error].getErrorMessage)
          case msg: ExecuteResult =>
            if (msg.result.isInstanceOf[org.mixql.remote.messages.rtype.Error]) {
              logError(
                s"Server: ClientModule: ${String(client.getIdentity)} Error while executing statement " + msg
                  .stmt + "error: " +
                  msg.result.asInstanceOf[org.mixql.remote.messages.rtype.Error].getErrorMessage
              )
              throw new Exception(msg.result.asInstanceOf[org.mixql.remote.messages.rtype.Error].getErrorMessage)
            }
            GtypeConverter.messageToGtype(msg.result)
      case msg: org.mixql.remote.messages.rtype.Error =>
        logError(
          s"Server: ClientModule: ${String(client.getIdentity)}: \n error while reacting on request\n" +
            msg.getErrorMessage
        )
        throw new Exception(msg.getErrorMessage)
  }

  private def _sendMsg(msg: messages.Message, client: ZMQ.Socket): Unit = {
    logDebug(
      "server: Clientmodule " + String(
        client.getIdentity
      ) + " sending protobuf message to remote module " + moduleIdentity + " " +
        client.send(msg.toByteArray, 0)
    )
  }

  private def sendMsg(msg: IModuleReceiver, client: ZMQ.Socket): Unit = {
    this.synchronized {
      if (!moduleStarted) {
        startModuleClient()
        moduleStarted = true
        logInfo(s" Clientmodule ${String(client.getIdentity)}: notify broker about started engine " + moduleIdentity)
        _sendMsg(new EngineStarted(moduleIdentity, startEngineTimeOut), client)
      }
    }
    _sendMsg(msg, client)
  }

  private def recvMsg(client: ZMQ.Socket): messages.Message = {
    val emptyRAW = client.recv(0)
    val emptyRAWStr = new String(emptyRAW)
    logDebug("Received empty msg " + emptyRAWStr)

    val msgRAW = client.recv(0)
    val msgRAWStr = new String(msgRAW)
    logDebug("Received raw msg " + msgRAWStr)
    RemoteMessageConverter.unpackAnyMsgFromArray(msgRAW)
  }

  private def startBroker(): Unit = {
    import ClientModule.config

    val portFrontend: Int = portFrontendArgs.getOrElse(Try({
      config.getInt("org.mixql.cluster.broker.portFrontend")
    }).getOrElse(PortOperations.isPortAvailable(0)))

    val host: String = hostArgs.getOrElse({
      Try(config.getString("org.mixql.cluster.broker.host")).getOrElse("0.0.0.0")
    })

    logInfo(
      s"Mixql engine demo platform: Starting broker messager with" +
        s" frontend port $portFrontend on host $host"
    )
    BrokerModule.start(portFrontend, host)
  }

  private def startModuleClient() = {
    val host = BrokerModule.getHost.get
    val portBackend = BrokerModule.getPort.get

    import ClientModule.config
    val basePath: File = basePathArgs.getOrElse(Try({
      val file = new File(config.getString("org.mixql.cluster.basePath"))
      if !file.isDirectory then
        logError(
          "Provided basePath in config in parameter org.mixql.cluster.basePath" +
            " must be directory!!!"
        )
        throw new Exception("")

      if !file.exists() then
        logError(
          "Provided basePath in config in parameter org.mixql.cluster.basePath" +
            " must exist!!!"
        )
        throw new Exception("")

      file
    }).getOrElse(Try({
      val file = new File(sys.env("MIXQL_CLUSTER_BASE_PATH"))
      if !file.isDirectory then
        logError("Provided basePath in system variable MIXQL_CLUSTER_BASE_PATH must be directory!!!")
        throw new Exception("")

      if !file.exists() then
        logError("Provided basePath in system variable MIXQL_CLUSTER_BASE_PATH must exist!!!")
        throw new Exception("")
      file
    }).getOrElse(new File("."))))

    startScriptName match
      case Some(scriptName) =>
        logInfo(
          s"server: ClientModule: $clientIdentity trying to start remote module $moduleIdentity at " + host +
            " and port at " + portBackend + " in " + basePath.getAbsolutePath + " by executing script " +
            scriptName + " in directory " + basePath.getAbsolutePath
        )
        clientRemoteProcess = CmdOperations.runCmdNoWait(
          Some(
            s"$scriptName.bat --port $portBackend --host $host --identity $moduleIdentity ${startScriptExtraOpts.getOrElse("")}"
          ),
          Some(
            s"$scriptName --port $portBackend --host $host --identity $moduleIdentity ${startScriptExtraOpts.getOrElse("")}"
          ),
          basePath
        )
      case None =>
        executor match
          case Some(engine) =>
            logInfo(
              s"server: ClientModule: $clientIdentity trying to  start module $moduleIdentity at " + host +
                " and port at " + portBackend + " in " + basePath.getAbsolutePath + " by executing in scala future"
            )
            clientFuture = engine.start(moduleIdentity, host, portBackend.toString)
          case None =>
  }

  def ShutDown() = {
    if moduleStarted then
      var client: ZMQ.Socket = null
      try {
        client = initClientSocket()
        sendMsg(messages.client.ShutDown(moduleIdentity), client)
        moduleStarted = false // not to send occasionally more then once
      } finally {
        closeClientSocket(client)
      }
      Try(if (_ctx != null) {
        logInfo(s"Server: ClientModule: $clientIdentity: close context")
        runWithTimeout(5000) {
          _ctx.close()
        }
        _ctx = null
      })
  }

  override def close() = {
    if (moduleStarted) {
      logInfo(s"Server: ClientModule: sending Shutdown to remote engine " + moduleIdentity)
      runWithTimeout(5000) {
        ShutDown()
      }
      logDebug("Give time module's socket to shutdown and shutdown message to reach module")
      sleep(2000)
    }
    logDebug(s"Server: ClientModule: $clientIdentity: Executing close")

    Try(if (_ctx != null) {
      logInfo(s"Server: ClientModule: $clientIdentity: close context")
      runWithTimeout(5000) {
        _ctx.close()
      }
    })

    //    if (clientRemoteProcess.isAlive()) clientRemoteProcess.exitValue()
    //    println(s"server: ClientModule: $clientIdentity: Remote client was shutdown")

  }

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent._
  import scala.concurrent.duration._

  def runWithTimeout[T](timeoutMs: Long)(f: => T): Option[T] = {
    Some(Await.result(Future(f), timeoutMs milliseconds))
  }
}
