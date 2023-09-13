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

case class StashedParam(name: String, value: gtype.Type)

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

  override def name: String = clientIdentity

  override def executeImpl(stmt: String, ctx: EngineContext): Type = {
    logInfo(s"[ClientModule-$clientIdentity]: module $moduleIdentity was triggered by execute request")

    sendMsg(Execute(moduleIdentity, clientIdentity, stmt))
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

    logInfo(s"[ClientModule-$clientIdentity]: module $moduleIdentity was triggered by executeFunc request")
    sendMsg(
      ExecuteFunction(
        moduleIdentity,
        clientIdentity,
        name,
        params.map(gParam => GtypeConverter.toGeneratedMsg(gParam)).toArray
      )
    )
    reactOnRequest(recvMsg(), ctx)
  }

  override def getDefinedFunctions(): List[String] = {
    if (!engineStarted) {
      logInfo(
        s"[ClientModule-$clientIdentity]: module $moduleIdentity was triggered by getDefinedFunctions request"
      )
    }
    engineStarted = true

    import org.mixql.core.context.gtype
    logInfo(s"Server: ClientModule: $clientIdentity: ask defined functions from remote engine")

    sendMsg(messages.client.GetDefinedFunctions(moduleIdentity, clientIdentity))
    val functionsList =
      recvMsg() match {
        case m: messages.module.DefinedFunctions => m.arr.toList
        case ex: org.mixql.remote.messages.`type`.Error =>
          val errorMessage =
            s"Server: ClientModule: $clientIdentity: getDefinedFunctions error: \n" + ex.getErrorMessage
          logError(errorMessage)
          throw new Exception(errorMessage)
        case m: messages.Message =>
          val errorMessage =
            s"Server: ClientModule: $clientIdentity: getDefinedFunctions error: \n" +
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
            sendMsg(
              new PlatformVar(
                moduleIdentity,
                clientIdentity,
                msg.workerIdentity(),
                msg.name,
                GtypeConverter.toGeneratedMsg(v)
              )
            )
            reactOnRequest(recvMsg(), ctx)
          case msg: SetPlatformVar =>
            ctx.setVar(msg.name, GtypeConverter.messageToGtype(msg.msg))
            sendMsg(new PlatformVarWasSet(moduleIdentity, clientIdentity, msg.workerIdentity(), msg.name))
            reactOnRequest(recvMsg(), ctx)
          case msg: GetPlatformVars =>
            val valMap = ctx.getVars(msg.names.toList)
            sendMsg(
              new PlatformVars(
                moduleIdentity,
                clientIdentity,
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
                moduleIdentity,
                clientIdentity,
                msg.workerIdentity(),
                new java.util.ArrayList[String](msg.vars.keySet())
              )
            )
            reactOnRequest(recvMsg(), ctx)
          case msg: GetPlatformVarsNames =>
            sendMsg(
              new PlatformVarsNames(
                moduleIdentity,
                clientIdentity,
                msg.workerIdentity(),
                ctx.getVarsNames().toArray
              )
            )
            reactOnRequest(recvMsg(), ctx)
          case msg: InvokeFunction =>
//            try {
            val res = ctx.invokeFunction(
              msg.name,
              msg.args.map(arg => unpack(GtypeConverter.messageToGtype(arg))).toList
            )
            sendMsg(
              new InvokedPlatformFunctionResult(
                moduleIdentity,
                clientIdentity,
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
          case msg: ExecutedFunctionResult =>
            if (msg.msg.isInstanceOf[org.mixql.remote.messages.`type`.Error]) {
              logError(
                "Server: ClientModule: Error while executing function " + msg.functionName + "error: " +
                  msg.msg.asInstanceOf[org.mixql.remote.messages.`type`.Error].getErrorMessage
              )
              throw new Exception(
                msg.msg.asInstanceOf[org.mixql.remote.messages.`type`.Error].getErrorMessage
              )
            }
            GtypeConverter.messageToGtype(msg.msg)
          case msg: org.mixql.remote.messages.`type`.Error =>
            logError(
              "Server: ClientModule: $clientIdentity:" + msg
                .asInstanceOf[org.mixql.remote.messages.`type`.Error].getErrorMessage
            )
            throw new Exception(msg.asInstanceOf[org.mixql.remote.messages.`type`.Error].getErrorMessage)
          case msg: ExecuteResult =>
            if (msg.result.isInstanceOf[org.mixql.remote.messages.`type`.Error]) {
              logError(
                "Server: ClientModule: Error while executing statement " + msg.stmt + "error: " +
                  msg.result.asInstanceOf[org.mixql.remote.messages.`type`.Error].getErrorMessage
              )
              throw new Exception(
                msg.result.asInstanceOf[org.mixql.remote.messages.`type`.Error].getErrorMessage
              )
            }
            GtypeConverter.messageToGtype(msg.result)
      case msg: org.mixql.remote.messages.`type`.Error =>
        logError(
          "Server: ClientModule: $clientIdentity: error while reacting on request" +
            msg.getErrorMessage
        )
        throw new Exception(msg.getErrorMessage)
  }

  private def _sendMsg(msg: messages.Message): Unit = {
    logDebug(
      "server: Clientmodule " + clientIdentity + " sending protobuf message to remote module " + moduleIdentity + " " +
        client.send(msg.toByteArray, 0)
    )
  }

  private def sendMsg(msg: IModuleReceiver): Unit = {
    this.synchronized {
      if !moduleStarted then
        if !BrokerModule.wasStarted then startBroker()
        startModuleClient()
        ctx = ZMQ.context(1)
        client = ctx.socket(SocketType.DEALER)
        // set id for client
        client.setIdentity(clientIdentity.getBytes)
        logInfo(
          "server: Clientmodule " + clientIdentity + " connected to " +
            s"tcp://${BrokerModule.getHost.get}:${BrokerModule.getPortFrontend.get} " + client
              .connect(s"tcp://${BrokerModule.getHost.get}:${BrokerModule.getPortFrontend.get}")
        )
        moduleStarted = true
        logInfo(s" Clientmodule $clientIdentity: notify broker about started engine " + moduleIdentity)
        _sendMsg(new EngineStarted(moduleIdentity, clientIdentity))
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
          s"server: ClientModule: $clientIdentity trying to start remote module $moduleIdentity at " + host +
            " and port at " + portBackend + " in " + basePath.getAbsolutePath + " by executing script " +
            scriptName + " in directory " + basePath.getAbsolutePath
        )
        clientRemoteProcess = CmdOperations.runCmdNoWait(
          Some(
            s"$scriptName.bat --port $portBackend --host $host --identity $moduleIdentity ${startScriptExtraOpts
                .getOrElse("")}"
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
                " and port at " + portBackend + " in " + basePath
                  .getAbsolutePath + " by executing in scala future"
            )
            clientFuture = engine.start(moduleIdentity, host, portBackend.toString)
          case None =>
  }

  def ShutDown() = {
    if moduleStarted then
      sendMsg(messages.client.ShutDown(moduleIdentity, clientIdentity))
      moduleStarted = false // not to send occasionally more then once
  }

  override def close() = {
    if (moduleStarted) {
      logInfo(s"Server: ClientModule: sending Shutdown to remote engine " + moduleIdentity)
      ShutDown()
      logDebug("Give time module's socket to shutdown and shutdown message to reach module")
      sleep(2000)
    }
    logDebug(s"Server: ClientModule: $clientIdentity: Executing close")
    Try(if (client != null) {
      logInfo(s"Server: ClientModule: $clientIdentity: close client socket")
      client.close()
    })

    Try(if (ctx != null) {
      logInfo(s"Server: ClientModule: $clientIdentity: close context")
      ctx.close()
    })

    //    if (clientRemoteProcess.isAlive()) clientRemoteProcess.exitValue()
    //    println(s"server: ClientModule: $clientIdentity: Remote client was shutdown")

  }
}
