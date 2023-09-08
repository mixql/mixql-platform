package org.mixql.cluster

import com.typesafe.config.ConfigFactory
import org.mixql.cluster.logger.{logDebug, logError, logInfo, logWarn}
import org.mixql.core.engine.Engine
import org.mixql.core.context.gtype.{Type, unpack}
import org.mixql.net.PortOperations
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter, messages}
import org.zeromq.{SocketType, ZMQ}

import java.io.File
import java.net.{InetSocketAddress, SocketAddress}
import java.nio.channels.{ServerSocketChannel, SocketChannel}
import scala.concurrent.Future
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Try
import org.mixql.core.context.{EngineContext, gtype}
import org.mixql.remote.messages.`type`.Param
import org.mixql.remote.messages.`type`.gtype.IGtypeMessage
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
import org.mixql.remote.messages.module.IModuleSendToClient
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

case class StashedParam(name: String, value: gtype.Type)

object ClientModule {
  val config = ConfigFactory.load()
}

//if start script name is not none then client must start remote engine by executing script
//which is {basePath}/{startScriptName}. P.S executor will be ignored
//if executor is not none and startScriptName is none then execute it in scala future
//if executor is none and startScript is none then just connect
class ClientModule(clientName: String,
                   moduleName: String,
                   startScriptName: Option[String],
                   executor: Option[IExecutor],
                   hostArgs: Option[String],
                   portFrontendArgs: Option[Int],
                   portBackendArgs: Option[Int],
                   basePathArgs: Option[File],
                   startScriptExtraOpts: Option[String] = None)
    extends Engine
    with java.lang.AutoCloseable {
  var client: ZMQ.Socket = null
  var ctx: ZMQ.Context = null

  var clientRemoteProcess: sys.process.Process = null
  var clientFuture: Future[Unit] = null
  var moduleStarted: Boolean = false

  override def name: String = clientName

  override def executeImpl(stmt: String, ctx: EngineContext): Type = {
    logInfo(s"[ClientModule-$clientName]: module $moduleName was triggered by execute request")

    sendMsg(Execute(moduleName, stmt))
    reactOnRequest(recvMsg(), ctx)
  }

  override def executeFuncImpl(name: String,
                               ctx: EngineContext,
                               kwargs: Map[String, Object],
                               params: Type*): Type = {
    if (kwargs.nonEmpty)
      throw new UnsupportedOperationException(
        "named arguments are not supported in functions in remote engine " + name
      )

    logInfo(s"[ClientModule-$clientName]: module $moduleName was triggered by executeFunc request")
    sendMsg(ExecuteFunction(name, params.map(gParam => GtypeConverter.toGeneratedMsg(gParam)).toArray))
    reactOnRequest(recvMsg(), ctx)
  }

  override def getDefinedFunctions(): List[String] = {
    if (!engineStarted) {
      logInfo(s"[ClientModule-$clientName]: module $moduleName was triggered by getDefinedFunctions request")
    }
    engineStarted = true

    import org.mixql.core.context.gtype
    logInfo(s"Server: ClientModule: $clientName: ask defined functions from remote engine")

    sendMsg(messages.client.GetDefinedFunctions())
    val functionsList =
      recvMsg() match {
        case m: messages.module.DefinedFunctions => m.arr.toList
        case ex: messages.gtype.Error =>
          val errorMessage = s"Server: ClientModule: $clientName: getDefinedFunctions error: \n" + ex.msg
          logError(errorMessage)
          throw new Exception(errorMessage)
        case m: messages.Message =>
          val errorMessage =
            s"Server: ClientModule: $clientName: getDefinedFunctions error: \n" +
              "Unknown message " + m.`type`() + " received"
          logError(errorMessage)
          throw new Exception(errorMessage)
      }

    if functionsList.isEmpty then Nil
    else functionsList
  }

  @tailrec
  private def reactOnRequest(msg: Message, ctx: EngineContext): Type = {
    msg match
      case msg: IGtypeMessage => msg.toGType
      case m: IModuleSendToClient =>
        m match
          case msg: GetPlatformVar =>
            val v = ctx.getVar(msg.name)
            sendMsg(new PlatformVar(msg.workerIdentity(), msg.name, GtypeConverter.toGeneratedMsg(v)))
            reactOnRequest(recvMsg(), ctx)
          case msg: SetPlatformVar =>
            ctx.setVar(msg.name, GtypeConverter.messageToGtype(msg.msg))
            sendMsg(new PlatformVarWasSet(msg.workerIdentity(), msg.name))
            reactOnRequest(recvMsg(), ctx)
          case msg: GetPlatformVars =>
            val valMap = ctx.getVars(msg.names.toList)
            sendMsg(
              new PlatformVars(
                msg.workerIdentity(),
                valMap.map(t => new Param(t._1, GtypeConverter.toGeneratedMsg(t._2))).toArray
              )
            )
            reactOnRequest(recvMsg(), ctx)
          case msg: SetPlatformVars =>
            import collection.JavaConverters._
            ctx.setVars(msg.vars.asScala.map(t => t._1 -> GtypeConverter.messageToGtype(t._2)))
            sendMsg(
              new PlatformVarsWereSet(
                msg.workerIdentity(),
                new java.util.ArrayList[String](msg.vars.keySet())
              )
            )
            reactOnRequest(recvMsg(), ctx)
          case msg: GetPlatformVarsNames =>
            sendMsg(new PlatformVarsNames(msg.workerIdentity(), ctx.getVarsNames().toArray))
            reactOnRequest(recvMsg(), ctx)
          case msg: InvokeFunction =>
//            try {
            val res = ctx.invokeFunction(
              msg.name,
              msg.args.map(arg => unpack(GtypeConverter.messageToGtype(arg))).toList
            )
            sendMsg(
              new InvokedPlatformFunctionResult(
                msg.workerIdentity(),
                msg.name,
                GtypeConverter.toGeneratedMsg(res)
              )
            )
            reactOnRequest(recvMsg(), ctx)
//            } catch {
//              case e: Throwable =>
//                sendMsg(new module.Error(e.getMessage()))
//                reactOnRequest(recvMsg(), ctx)
//            }
      case msg: messages.gtype.Error =>
        logError(
          "Server: ClientModule: $clientName: error while reacting on request" +
            msg.msg
        )
        throw new Exception(msg.msg)
  }

  private def _sendMsg(msg: messages.Message): Unit = {
//    logDebug(
//      "server: Clientmodule " + clientName + " sending identity of remote module " + moduleName + " " +
//        client.send(moduleName.getBytes, ZMQ.SNDMORE)
//    )
//    logDebug(
//      "server: Clientmodule " + clientName + " sending empty frame to remote module " + moduleName + " " +
//        client.send("".getBytes, ZMQ.SNDMORE)
//    )
    logDebug(
      "server: Clientmodule " + clientName + " sending protobuf message to remote module " + moduleName + " " +
        client.send(msg.toByteArray, 0)
    )
  }

  private def sendMsg(msg: IModuleReceiver): Unit = {
    this.synchronized {
      if !moduleStarted then
        if !BrokerModule.wasStarted then startBroker()
        startModuleClient()
        ctx = ZMQ.context(1)
        client = ctx.socket(SocketType.REQ)
        // set id for client
        client.setIdentity(clientName.getBytes)
        logInfo(
          "server: Clientmodule " + clientName + " connected to " +
            s"tcp://${BrokerModule.getHost.get}:${BrokerModule.getPortFrontend.get} " + client
              .connect(s"tcp://${BrokerModule.getHost.get}:${BrokerModule.getPortFrontend.get}")
        )
        moduleStarted = true
        logInfo(s" Clientmodule $clientName: notify broker about started engine " + moduleName)
        _sendMsg(new EngineStarted(moduleName))
      end if
      _sendMsg(msg)
    }
  }

  private def recvMsg(): messages.Message = {
    this.synchronized {
      RemoteMessageConverter.unpackAnyMsgFromArray(client.recv(0))
    }
  }

  private def startBroker() = {
    import ClientModule.config

    val portFrontend: Int = portFrontendArgs.getOrElse(Try({
      config.getInt("org.mixql.cluster.broker.portFrontend")
    }).getOrElse(PortOperations.isPortAvailable(0)))

    val portBackend: Int = portBackendArgs.getOrElse(Try({
      config.getInt("org.mixql.cluster.broker.portBackend")
    }).getOrElse(PortOperations.isPortAvailable(0)))

    val host: String = hostArgs.getOrElse({
      Try(config.getString("org.mixql.cluster.broker.host")).getOrElse("0.0.0.0")
    })

    logInfo(
      s"Mixql engine demo platform: Starting broker messager with" +
        s" frontend port $portFrontend and backend port $portBackend on host $host"
    )
    BrokerModule.start(portFrontend, portBackend, host)
  }

  private def startModuleClient() = {
    val host = BrokerModule.getHost.get
    val portBackend = BrokerModule.getPortBackend.get

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
          s"server: ClientModule: $clientName trying to start remote module $moduleName at " + host +
            " and port at " + portBackend + " in " + basePath.getAbsolutePath + " by executing script " +
            scriptName + " in directory " + basePath.getAbsolutePath
        )
        clientRemoteProcess = CmdOperations.runCmdNoWait(
          Some(
            s"$scriptName.bat --port $portBackend --host $host --identity $moduleName ${startScriptExtraOpts.getOrElse("")}"
          ),
          Some(
            s"$scriptName --port $portBackend --host $host --identity $moduleName ${startScriptExtraOpts.getOrElse("")}"
          ),
          basePath
        )
      case None =>
        executor match
          case Some(engine) =>
            logInfo(
              s"server: ClientModule: $clientName trying to  start module $moduleName at " + host +
                " and port at " + portBackend + " in " + basePath
                  .getAbsolutePath + " by executing in scala future"
            )
            clientFuture = engine.start(moduleName, host, portBackend.toString)
          case None =>
  }

  def ShutDown() = {
    if moduleStarted then
      sendMsg(messages.client.ShutDown())
      moduleStarted = false // not to send occasionally more then once
  }

  override def close() = {
    if (moduleStarted) {
      logInfo(s"Server: ClientModule: sending Shutdown to remote engine " + moduleName)
      ShutDown()
      logDebug("Give time module's socket to shutdown and shutdown message to reach module")
      sleep(2000)
    }
    logDebug(s"Server: ClientModule: $clientName: Executing close")
    Try(if (client != null) {
      logInfo(s"Server: ClientModule: $clientName: close client socket")
      client.close()
    })

    Try(if (ctx != null) {
      logInfo(s"Server: ClientModule: $clientName: close context")
      ctx.close()
    })

    //    if (clientRemoteProcess.isAlive()) clientRemoteProcess.exitValue()
    //    println(s"server: ClientModule: $clientName: Remote client was shutdown")

  }
}
