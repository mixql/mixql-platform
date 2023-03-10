package org.mixql.cluster

import com.typesafe.config.ConfigFactory
import org.mixql.cluster.ClientModule.broker
import org.mixql.core.engine.Engine
import org.mixql.core.context.gtype.Type
import org.mixql.net.PortOperations
import org.mixql.protobuf.{ProtoBufConverter, GtypeConverter}
import org.mixql.protobuf.messages.clientMsgs
import org.zeromq.{SocketType, ZMQ}

import java.io.File
import java.net.{InetSocketAddress, SocketAddress}
import java.nio.channels.{ServerSocketChannel, SocketChannel}
import scala.concurrent.Future
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Try
import org.mixql.core.context.gtype
import org.mixql.protobuf.messages.clientMsgs.ShutDown

case class StashedParam(name: String, value: gtype.Type)

object ClientModule {
  var broker: BrokerModule = null
  val engineStartedMap: mutable.Map[String, Boolean] = mutable.Map()
  val enginesStashedParams: mutable.Map[String, ListBuffer[StashedParam]] = mutable.Map()
  val config = ConfigFactory.load()

  var haveSentStashedParams: Boolean = false

  def stashMessage(
                    moduleName: String,
                    clientName: String,
                    name: String,
                    value: gtype.Type,
                  ) = {
    println(
      s"[ClientModule-$clientName]: started to stash parameter $name with value $value"
    )
    if enginesStashedParams.get(moduleName).isEmpty then
      enginesStashedParams.put(
        moduleName,
        ListBuffer(StashedParam(name, value))
      )
    else
      enginesStashedParams.get(moduleName) match
        case Some(messages) =>
          messages += StashedParam(name, value)
        case None =>
          println(
            s"[ClientModule-$clientName]: warning! no key $moduleName in enginesStashedParams," +
              s" thow it should be here! Strange!"
          )
          enginesStashedParams.put(
            moduleName,
            ListBuffer(StashedParam(name, value))
          )
      end match
    end if
    println(
      s"[ClientModule-$clientName]: successfully stashed parameter $name with value $value"
    )
  }
}

//if start script name is not none then client must start remote engine by executing script
//which is {basePath}/{startScriptName}. P.S executor will be ignored
//if executor is not none and startScriptName is none then execute it in scala future
//if executor is none and startScript is none then just connect
class ClientModule(
                    clientName: String,
                    moduleName: String,
                    startScriptName: Option[String],
                    executor: Option[IExecutor],
                    hostArgs: Option[String],
                    portFrontendArgs: Option[Int],
                    portBackendArgs: Option[Int],
                    basePathArgs: Option[File]
                  ) extends Engine
  with java.lang.AutoCloseable {
  var client: ZMQ.Socket = null
  var ctx: ZMQ.Context = null

  var clientRemoteProcess: sys.process.Process = null
  var clientFuture: Future[Unit] = null

  override def name: String = clientName

  private def sendStashedParamsIfTheyAre() = {
    ClientModule.haveSentStashedParams = true
    import ClientModule.enginesStashedParams
    println(s"[ClientModule-$clientName]: Check if there are stashed params")
    enginesStashedParams.get(moduleName) match
      case Some(messages) =>
        if messages.isEmpty then
          println(
            s"[ClientModule-$clientName]: Checked engines map. No stashed messages for $moduleName"
          )
        else
          println(
            s"[ClientModule-$clientName]: Have founded stashed messages (amount: ${messages.length}) " +
              s"for engine $moduleName. Sending them"
          )
          messages.foreach(msg =>
            sendParam(msg.name, msg.value)
          )
          //                  engines.put(moduleName, ListBuffer())
          messages.clear()

      case None =>
        println(
          s"[ClientModule-$clientName]: warning! no key $moduleName, thow it should be here! Strange!"
        )
    end match
  }

  override def execute(stmt: String): Type = {
    sendStashedParamsIfTheyAre()

    import org.mixql.protobuf.messages.clientMsgs
    import org.mixql.protobuf.GtypeConverter
    sendMsg(clientMsgs.Execute(stmt))
    GtypeConverter.toGtype(recvMsg())
  }

  override def executeFunc(name: String, params: Type*): Type = {
    sendStashedParamsIfTheyAre()
    sendMsg(clientMsgs.ExecuteFunction(name, Some(clientMsgs.Array(params.map(
      gParam => GtypeConverter.toProtobufAny(gParam)
    )))))
    GtypeConverter.toGtype(recvMsg())
  }


  override def getDefinedFunctions: List[String] = {
    import org.mixql.core.context.gtype
    sendStashedParamsIfTheyAre()
    println(s"Server: ClientModule: $clientName: ask defined functions from remote engine")
    sendMsg(clientMsgs.GetDefinedFunctions())
    val functionsList = recvMsg().asInstanceOf[clientMsgs.DefinedFunctions].arr.toList
    if functionsList.isEmpty then
      Nil
    else
      functionsList
  }

  private def sendParam(name: String, value: Type): Unit = {
    import org.mixql.protobuf.messages.clientMsgs
    import org.mixql.protobuf.GtypeConverter

    sendMsg(
      clientMsgs.SetParam(
        name,
        Some(
          GtypeConverter.toProtobufAny(value)
        )
      )
    )
    recvMsg() match
      case clientMsgs.ParamWasSet(_) =>
      case clientMsgs.Error(msg, _) => throw Exception(msg)
      case a: scala.Any =>
        throw Exception(
          s"engine-client-module: setParam error:  " +
            s"error while receiving confirmation that param was set: got ${a.toString}," +
            " when ParamWasSet or Error messages were expected"
        )
  }

  override def setParam(name: String, value: Type): Unit = {
    if ClientModule.haveSentStashedParams then
      sendParam(name, value)
    else
      ClientModule.stashMessage(moduleName, clientName, name, value)
  }

  override def getParam(name: String): Type = {
    sendStashedParamsIfTheyAre()
    import org.mixql.protobuf.messages.clientMsgs
    import org.mixql.protobuf.GtypeConverter

    sendMsg(clientMsgs.GetParam(name))
    GtypeConverter.toGtype(recvMsg())
  }

  override def isParam(name: String): Boolean = {
    sendStashedParamsIfTheyAre()
    import org.mixql.core.context.gtype
    import org.mixql.protobuf.messages.clientMsgs
    import org.mixql.protobuf.GtypeConverter

    sendMsg(clientMsgs.IsParam(name))
    GtypeConverter.toGtype(recvMsg()).asInstanceOf[gtype.bool].value
  }

  private def sendMsg(msg: scalapb.GeneratedMessage): Unit = {
    import ClientModule.engineStartedMap
    val started = engineStartedMap.get(moduleName.trim) match
      case Some(value) => value
      case None => false


    import ClientModule.broker
    if !started then
      if broker == null then
        startBroker()
      startModuleClient()
      ctx = ZMQ.context(1)
      client = ctx.socket(SocketType.REQ)
      // set id for client
      client.setIdentity(clientName.getBytes)
      println(
        "server: Clientmodule " + clientName + " connected to " +
          s"tcp://${broker.getHost}:${broker.getPortFrontend} " + client
          .connect(s"tcp://${broker.getHost}:${broker.getPortFrontend}")
      )
      engineStartedMap.put(moduleName.trim, true)
    end if

    println(
      "server: Clientmodule " + clientName + " sending identity of remote module " + moduleName + " " +
        client.send(moduleName.getBytes, ZMQ.SNDMORE)
    )
    println(
      "server: Clientmodule " + clientName + " sending empty frame to remote module " + moduleName + " " +
        client.send("".getBytes, ZMQ.SNDMORE)
    )
    println(
      "server: Clientmodule " + clientName + " sending protobuf message to remote module " + moduleName + " " +
        client.send(ProtoBufConverter.toArray(msg).get, 0)
    )
  }

  private def recvMsg(): scalapb.GeneratedMessage = {
    ProtoBufConverter.unpackAnyMsg(client.recv(0))
  }

  private def startBroker() = {
    import ClientModule.broker
    import ClientModule.config

    val portFrontend: Int = portFrontendArgs.getOrElse(
      Try(
        config.getInt("org.mixql.cluster.broker.portFrontend")
      ).getOrElse(
        PortOperations.isPortAvailable(0)
      )
    )

    val portBackend: Int = portBackendArgs.getOrElse(
      Try(
        config.getInt("org.mixql.cluster.broker.portBackend")
      ).getOrElse(
        PortOperations.isPortAvailable(0)
      )
    )

    val host: String = hostArgs.getOrElse(
      Try(
        config.getString("org.mixql.cluster.broker.host")
      ).getOrElse(
        "0.0.0.0"
      )
    )


    println(s"Mixql engine demo platform: Starting broker messager with" +
      s" frontend port $portFrontend and backend port $portBackend on host $host")
    broker = new BrokerModule(portFrontend, portBackend, host)
    broker.start()
  }

  private def startModuleClient() = {
    val host = broker.getHost
    val portBackend = broker.getPortBackend

    import ClientModule.config
    val basePath: File = basePathArgs.getOrElse(
      Try({
        val file = new File(config.getString("org.mixql.cluster.basePath"))
        if !file.isDirectory then
          println("ERROR: Provided basePath in config in parameter org.mixql.cluster.basePath" +
            " must be directory!!!")
          throw new Exception("")

        if !file.exists() then
          println("ERROR: Provided basePath in config in parameter org.mixql.cluster.basePath" +
            " must exist!!!")
          throw new Exception("")

        file
      }).getOrElse(
        Try({
          val file = new File(sys.env("MIXQL_CLUSTER_BASE_PATH"))
          if !file.isDirectory then
            println("ERROR: Provided basePath in system variable MIXQL_CLUSTER_BASE_PATH must be directory!!!")
            throw new Exception("")

          if !file.exists() then
            println("ERROR: Provided basePath in system variable MIXQL_CLUSTER_BASE_PATH must exist!!!")
            throw new Exception("")
          file
        }).getOrElse(new File("."))
      )
    )

    startScriptName match
      case Some(scriptName) =>
        println(
          s"server: ClientModule: $clientName trying to start remote module $moduleName at " + host +
            " and port at " + portBackend + " in " + basePath.getAbsolutePath + " by executing script " +
            scriptName + " in directory " + basePath.getAbsolutePath
        )
        clientRemoteProcess = CmdOperations.runCmdNoWait(
          Some(
            s"$scriptName.bat --port $portBackend --host $host --identity $moduleName"
          ),
          Some(
            s"$scriptName --port $portBackend --host $host --identity $moduleName"
          ),
          basePath
        )
      case None =>
        executor match
          case Some(engine) =>
            println(
              s"server: ClientModule: $clientName trying to  start module $moduleName at " + host +
                " and port at " + portBackend + " in " + basePath.getAbsolutePath + " by executing in scala future"
            )
            clientFuture = engine.start(moduleName, host, portBackend.toString)
          case None =>
  }

  def ShutDown() = {
    import ClientModule.engineStartedMap
    val started = engineStartedMap.get(moduleName.trim) match
      case Some(value) => value
      case None => false


    import ClientModule.broker
    if started then
      sendMsg(clientMsgs.ShutDown())
      engineStartedMap.put(moduleName.trim, false) //not to send occasionally more then once
  }

  override def close() = {
    println(s"Server: ClientModule: $clientName: Executing close")
    if (client != null) {
      println(s"Server: ClientModule: $clientName: close client socket")
      client.close()
    }
    if (ctx != null) {
      println(s"Server: ClientModule: $clientName: close context")
      ctx.close()
    }

    //    if (clientRemoteProcess.isAlive()) clientRemoteProcess.exitValue()
    //    println(s"server: ClientModule: $clientName: Remote client was shutdown")

  }
}
