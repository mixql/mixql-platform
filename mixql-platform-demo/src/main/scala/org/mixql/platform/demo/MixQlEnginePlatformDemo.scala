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
import org.mixql.platform.demo.procedures.SimpleFuncs

import scala.collection.mutable
import org.mixql.platform.demo.logger.*
import scala.util.Try

object MixQlEnginePlatformDemo:
  def main(args: Array[String]): Unit =
    logDebug("Mixql engine demo platform: parsing args")
    val (host, portFrontend, portBackend, basePath, sqlScriptFiles) = parseArgs(args.toList)

    logDebug(s"Mixql engine demo platform: initialising engines")
    val engines = mutable.Map[String, Engine](
      "stub" -> new ClientModule(
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
      "stub-scala-2-12" -> new ClientModule(
        //Name of client, is used for identification in broker,
        //must be unique
        "mixql-engine-stub-scala-2-12-demo-platform",
        //Name of remote engine, is used for identification in broker,
        //must be unique
        "mixql-engine-stub-scala-2-12",
        //will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
        //in base path
        Some("mixql-engine-stub-scala-2-12"),
        None,
        host, portFrontend, portBackend, basePath
      ),
      "stub-scala-2-13" -> new ClientModule(
        //Name of client, is used for identification in broker,
        //must be unique
        "mixql-engine-stub-scala-2-13-demo-platform",
        //Name of remote engine, is used for identification in broker,
        //must be unique
        "mixql-engine-stub-scala-2-13",
        //will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
        //in base path
        Some("mixql-engine-stub-scala-2-13"),
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
      "sqlite-local" -> EngineSqlightLocal()
    )

    logDebug(s"Init functions for mixql context")

    val functions: collection.mutable.Map[String, Any] = collection.mutable.Map(
      "simple_func" -> SimpleFuncs.simple_func,
      "print_current_vars" -> SimpleFuncs.print_current_vars,
      "get_engines_list" -> SimpleFuncs.get_engines_list,
    )

    logDebug(s"Mixql engine demo platform: init Cluster context")
    val context =
      new Context(engines, Try({
        config.getString("org.mixql.platform.demo.engines.default")
      }).getOrElse("stub"), functionsInit = functions)

    try {
      logDebug(s"Mixql engine demo platform: reading and executing sql files if they exist")
      (sqlScriptFiles match {
        case None => List((None, code))
        case Some(value) => value.map {
          (f: File) => (Some(f.getAbsolutePath), utils.FilesOperations.readFileContent(f))
        }
      }).foreach(sql => {
        if sql._1.nonEmpty then
          logDebug("Mixql engine demo platform: running script: " + sql._1.get)
        else
          logDebug("Mixql engine demo platform: running standard code for testing: " + code)
        run(sql._2, context)
      })

      logDebug(context.getScope().head.toString())
    } catch {
      case e: Throwable => logError(e.getMessage)
    } finally {
      context.engines.values.foreach(
        e => if (e.isInstanceOf[ClientModule]) {
          val cl: ClientModule = e.asInstanceOf[ClientModule]
          logDebug(s"sending shutdwon to remote engine " + cl.name)
          cl.ShutDown()
        }
      )
      context.close()
      if ClientModule.broker != null then ClientModule.broker.close()
    }

  def parseArgs(args: List[String]): (Option[String], Option[Int],
    Option[Int], Option[File], Option[List[File]]) =
    import org.rogach.scallop.ScallopConfBase
    val appArgs = AppArgs(args)
    val host = appArgs.host.toOption
    val portFrontend = //PortOperations.isPortAvailable(
      appArgs.portFrontend.toOption
    //)
    val portBackend = //PortOperations.isPortAvailable(
      appArgs.portBackend.toOption
    //)
    val basePath = appArgs.basePath.toOption
    val sqlScripts = appArgs.sqlFile.toOption
    (host, portFrontend, portBackend, basePath, sqlScripts)

case class AppArgs(arguments: Seq[String]) extends ScallopConf(arguments) :

  import org.rogach.scallop.stringConverter
  import org.rogach.scallop.intConverter
  import org.rogach.scallop.fileConverter
  import org.rogach.scallop.fileListConverter

  val portFrontend = opt[Int](descr = "frontend port of platform's broker, client modules will connect to it",
    required = false) //, default = Some(0))
  val portBackend = opt[Int](descr = "backend port of platform's broker, remote engines will connect to it",
    required = false) //, default = Some(0))
  val host = opt[String](descr = "host of platform's broker",
    required = false) //, default = Some("0.0.0.0"))
  val basePath = opt[File](descr = "path with sripts for launching remote engines",
    required = false) //, default = Some(new File(".")))
  val sqlFile = opt[List[File]](descr = "path to sql script file", required = false)

  validateFilesIsFile(sqlFile)
  validateFilesExist(sqlFile)

  validateFileIsDirectory(basePath)

  verify()


