package org.mixql.platform.demo

import org.beryx.textio.web.{SparkTextIoApp, WebTextTerminal}
import org.beryx.textio.{ReadAbortedException, ReadHandlerData, ReadInterruptionStrategy, TextIO, TextIoFactory}
import org.mixql.core.run
import org.rogach.scallop.ScallopConf

import java.io.File
import org.mixql.cluster.{BrokerModule, ClientModule}
import org.mixql.core.engine.Engine
import org.mixql.net.PortOperations
import org.mixql.core.context.{Context, mtype}
import org.mixql.engine.stub.local.EngineStubLocal
import org.mixql.engine.sqlite.local.EngineSqlightLocal
import org.mixql.platform.demo.engines.executors.{MixQlEngineSqliteExecutor, MixQlEngineStubExecutor}
import org.mixql.platform.demo.procedures.SimpleFuncs

import scala.collection.mutable
import org.mixql.platform.demo.logger.*
import org.mixql.remote.messages.client.ShutDown
import org.mixql.repl.{TerminalApp, TerminalOps, WebTextIoExecutor}

import scala.util.Try

object MixQlEnginePlatformDemo:

  def main(args: Array[String]): Unit =
    logDebug("Mixql engine demo platform: parsing args")
    val (host, portFrontend, homePath, sqlScriptFiles) = parseArgs(args.toList)

    val binPath: Option[File] =
      if homePath.isEmpty then None
      else Some(File(homePath.get.getAbsolutePath + "/bin"))

    logDebug(s"Mixql engine demo platform: initialising engines")
    val engines = mutable.Map[String, Engine](
      "stub" -> new ClientModule(
        // Name of client, is used for identification in broker,
        // must be unique
        clientIdentity = "mixql-engine-stub-demo-platform",
        // Name of remote engine, is used for identification in broker,
        // must be unique
        moduleIdentity = "mixql-engine-stub",
        // will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
        // in base path
        startScriptName = None,
        executor = Some(MixQlEngineStubExecutor),
        hostArgs = host,
        portFrontendArgs = portFrontend,
        basePathArgs = None,
        startEngineTimeOut = 10000 // 10sec
      ),
      "sqlite" -> new ClientModule(
        // Name of client, is used for identification in broker,
        // must be unique
        clientIdentity = "mixql-engine-sqlite-demo-platform",
        // Name of remote engine, is used for identification in broker,
        // must be unique
        moduleIdentity = "mixql-engine-sqlite",
        // will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
        // in base path
        startScriptName = None,
        executor = Some(MixQlEngineSqliteExecutor),
        hostArgs = host,
        portFrontendArgs = portFrontend,
        basePathArgs = None,
        startEngineTimeOut = 10000 // 10sec
      ),
      "stub-local" -> EngineStubLocal,
      "sqlite-local" -> EngineSqlightLocal()
    )

    logDebug(s"Init functions for mixql context")

    val functions: collection.mutable.Map[String, Object] = collection.mutable.Map(
      "simple_func" -> SimpleFuncs.simple_func,
      "print_current_vars" -> SimpleFuncs.print_current_vars,
      "get_engines_list" -> SimpleFuncs.get_engines_list
    )

    logDebug(s"Init variables for mixql context")
    val variables: mutable.Map[String, mtype.MType] = mutable.Map[String, mtype.MType](
      "mixql.org.engine.sqlight.db.path" -> mtype.MString(config.getString("mixql.org.engine.sqlight.db.path"))
    )

    logDebug(s"Mixql engine demo platform: init Cluster context")
    val context = Context(
      engines,
      Try({
        config.getString("org.mixql.platform.demo.engines.default")
      }).getOrElse("stub"),
      functionsInit = functions,
      variablesInit = variables
    )

    try {
      var replMode = false
      logDebug("Mixql engine demo platform: reading and executing sql files if they exist")
      (sqlScriptFiles match {
        case None =>
          replMode = true
          List()
        case Some(value) =>
          value.map { (f: File) =>
            (Some(f.getAbsolutePath), utils.FilesOperations.readFileContent(f))
          }
      }).foreach(sql => {
        logDebug("Mixql engine demo platform: running script: " + sql._1.get)
        run(sql._2, context)
      })

      if (replMode) {
        if (isWebRepl()) {
          val webTextTerm = new WebTextTerminal()
          webTextTerm.init();
          val textIO = new TextIO(webTextTerm);
          val app = TerminalApp(context)
          val textIoApp = new SparkTextIoApp(app, textIO.getTextTerminal.asInstanceOf[WebTextTerminal])
          val webTextIoExecutor = new WebTextIoExecutor(launchDesktopBrowser())
          webTextIoExecutor.withPort(8080)
          webTextIoExecutor.execute(textIoApp)
        } else {
          val terminal = TerminalApp(context)
          terminal.accept(TextIoFactory.getTextIO, null)
        }
      }
    } catch {
      case e: Throwable => logError(e.getMessage)
    } finally {
//      context.engines.values.foreach(e =>
//        if (e.isInstanceOf[ClientModule]) {
//          Try({
//            val cl: ClientModule = e.asInstanceOf[ClientModule]
//            logDebug(s"sending shutdown to remote engine " + cl.name)
//            cl.ShutDown()
//          })
//        }
//      )
      Try(context.close())
      Try({ if BrokerModule.wasStarted then BrokerModule.close() })
    }

  private def isWebRepl(): Boolean = {
    Try(config.getBoolean("org.mixql.platform.demo.repl.launch-web")).getOrElse(false)
  }

  private def launchDesktopBrowser(): Boolean = {
    Try(config.getBoolean("org.mixql.platform.demo.repl.launch-desktop-web-browser")).getOrElse(true)
  }

  def parseArgs(args: List[String]): (Option[String], Option[Int], Option[File], Option[List[File]]) =
    import org.rogach.scallop.ScallopConfBase
    val appArgs = AppArgs(args)
    val host = appArgs.host.toOption
    val portFrontend = // PortOperations.isPortAvailable(
      appArgs.portFrontend.toOption
    // )
    val homePath: Option[File] = Try({
      Some(appArgs.homePath.toOption.get)
    }).getOrElse(Try({
      val file = new File(sys.env("MIXQL_PLATFORM_DEMO_HOME_PATH"))
      if !file.isDirectory then
        logError(
          "Provided platform demo's home path in system variable" +
            " MIXQL_PLATFORM_DEMO_HOME_PATH must be directory!!!"
        )
        throw new Exception("")

      if !file.exists() then
        logError(
          "Provided platform demo's home path in system variable" +
            " MIXQL_PLATFORM_DEMO_HOME_PATH must exist!!!"
        )
        throw new Exception("")
      Some(file)
    }).getOrElse(None))

    val sqlScripts = appArgs.sqlFile.toOption
    (host, portFrontend, homePath, sqlScripts)

case class AppArgs(arguments: Seq[String]) extends ScallopConf(arguments):

  import org.rogach.scallop.stringConverter
  import org.rogach.scallop.intConverter
  import org.rogach.scallop.fileConverter
  import org.rogach.scallop.fileListConverter

  val portFrontend = opt[Int](
    descr = "port of platform's broker, clients and engines will connect to it",
    required = false
  ) // , default = Some(0))

  val host = opt[String](descr = "host of platform's broker", required = false) // , default = Some("0.0.0.0"))
  val homePath = opt[File](descr = "home path of platform demo", required = false) // , default = Some(new File(".")))
  val sqlFile = opt[List[File]](descr = "path to sql script file", required = false)

  validateFilesIsFile(sqlFile)
  validateFilesExist(sqlFile)

  validateFileIsDirectory(homePath)

  verify()
