package org.mixql.platform.demo

import org.mixql.core.run
import org.rogach.scallop.ScallopConf

import java.io.File
import org.mixql.cluster.{BrokerModule, ClientModule}
import org.mixql.core.Main.code
import org.mixql.core.engine.Engine
import org.mixql.net.PortOperations
import org.mixql.core.context.Context
import org.mixql.protobuf.messages.clientMsgs.ShutDown
import org.mixql.engine.stub.local.EngineStubLocal
import org.mixql.engine.sqlite.local.EngineSqlightLocal

import scala.collection.mutable

object MixQlEnginePlatformDemo:
  def main(args: Array[String]): Unit =
    println("Mixql engine demo platform: parsing args")
    val (host, portFrontend, portBackend, basePath, sqlScriptFiles) = parseArgs(args.toList)

    println(s"Mixql engine demo platform: Starting broker messager with" +
      s" frontend port $portFrontend and backend port $portBackend on host $host")
    val broker = new BrokerModule(portFrontend, portBackend, host)
    broker.start()

    println(s"Mixql engine demo platform: initialising engines")
    val engines = mutable.Map[String, Engine]("stub" -> new ClientModule(
      //Name of client, is used for identification in broker,
      //must be unique
      "mixql-engine-stub-demo-platform",
      //Name of remote engine, is used for identification in broker,
      //must be unique
      "mixql-engine-stub",
      //will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
      //in base path
      Some("mixql-engine-stub"),
      None,
      host, portFrontend, portBackend, basePath
    ),
      "sqlite" -> new ClientModule(
        //Name of client, is used for identification in broker,
        //must be unique
        "mixql-engine-sqlite-demo-platform",
        //Name of remote engine, is used for identification in broker,
        //must be unique
        "mixql-engine-sqlite",
        //will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
        //in base path
        Some("mixql-engine-sqlite"),
        None,
        host, portFrontend, portBackend, basePath
      ),
      "stub-local" -> EngineStubLocal,
      "sqlite-local" -> EngineSqlightLocal
    )

    println(s"Mixql engine demo platform: init Cluster context")
    val context =
      new Context(engines, "stub")

    try {
      println(s"Mixql engine demo platform: reading and executing sql files if they exist")
      (sqlScriptFiles match {
        case None => List((None, code))
        case Some(value) => value.map {
          (f: File) => (Some(f.getAbsolutePath), utils.FilesOperations.readFileContent(f))
        }
      }).foreach(sql => {
        if sql._1.nonEmpty then
          println("Mixql engine demo platform: running script: " + sql._1.get)
        else
          println("Mixql engine demo platform: running standard code for testing: " + code)
        run(sql._2, context)
      })

      println(context.scope.head)
    } catch {
      case e: Throwable => println(s"Error: Mixql engine demo platform: " + e.getMessage)
    } finally {
      context.engines.values.foreach(
        e => if (e.isInstanceOf[ClientModule]) {
          val cl: ClientModule = e.asInstanceOf[ClientModule]
          println(s"mixql core context: sending shutdwon to remote engine " + cl.name)
          cl.sendMsg(ShutDown())
        }
      )
      context.close()
      broker.close()
    }

  def parseArgs(args: List[String]): (String, Int, Int, File, Option[List[File]]) =
    import org.rogach.scallop.ScallopConfBase
    val appArgs = AppArgs(args)
    val host: String = appArgs.host.toOption.get
    val portFrontend = PortOperations.isPortAvailable(
      appArgs.portFrontend.toOption.get
    )
    val portBackend = PortOperations.isPortAvailable(
      appArgs.portBackend.toOption.get
    )
    val basePath = appArgs.basePath.toOption.get
    val sqlScripts = appArgs.sqlFile.toOption
    (host, portFrontend, portBackend, basePath, sqlScripts)

case class AppArgs(arguments: Seq[String]) extends ScallopConf(arguments):

  import org.rogach.scallop.stringConverter
  import org.rogach.scallop.intConverter
  import org.rogach.scallop.fileConverter
  import org.rogach.scallop.fileListConverter

  val portFrontend = opt[Int](required = false, default = Some(0))
  val portBackend = opt[Int](required = false, default = Some(0))
  val host = opt[String](required = false, default = Some("0.0.0.0"))
  val basePath = opt[File](required = false, default = Some(new File(".")))
  val sqlFile = opt[List[File]](descr = "path to sql script file", required = false)

  validateFilesIsFile(sqlFile)
  validateFilesExist(sqlFile)

  validateFileIsDirectory(basePath)

  verify()


