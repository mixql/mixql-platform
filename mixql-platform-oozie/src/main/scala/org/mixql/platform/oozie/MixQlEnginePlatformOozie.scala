package org.mixql.platform.oozie

import org.beryx.textio.TextIO
import org.beryx.textio.web.{SparkTextIoApp, WebTextTerminal}
import org.mixql.core.run
import org.rogach.scallop.ScallopConf

import java.io.File
import org.mixql.cluster.{BrokerModule, ClientModule}
import org.mixql.core.engine.Engine
import org.mixql.core.context.{Context, mtype}
import org.mixql.engine.sqlite.local.EngineSqlightLocal

import scala.collection.mutable
import org.mixql.platform.oozie.logger.*
import org.mixql.oozie.OozieParamsReader
import org.mixql.remote.messages.client.ShutDown
import org.mixql.repl.{TerminalApp, WebTextIoExecutor}

import scala.util.Try

object MixQlEnginePlatformOozie:

  def main(args: Array[String]): Unit =
    logDebug("Mixql engine oozie platform: parsing args")
    val oozieId = AppArgs(args).oozieId.toOption.get
    val oozieUrl = config.getString("org.mixql.platform.oozie.url")

    val oozieParams = OozieParamsReader.getAllOozieParams(oozieId, oozieUrl)
    val host = Try {
      Some(oozieParams("org.mixql.cluster.broker.host"))
    }.getOrElse(None)

    val portFrontend = Try {
      Some(oozieParams("org.mixql.cluster.broker.port").toInt)
    }.getOrElse(None)

    logDebug(s"Mixql engine demo platform: initialising engines")
    val engines = mutable.Map[String, Engine](
      "sqlite" -> new ClientModule(
        // Name of client, is used for identification in broker,
        // must be unique
        clientIdentity = "mixql-engine-sqlite-demo-platform",
        // Name of remote engine, is used for identification in broker,
        // must be unique
        moduleIdentity = "mixql-engine-sqlite",
        // will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
        // in base path
        startScriptName = Some("mixql-engine-sqlite"),
        executor = None,
        hostArgs = host,
        portFrontendArgs = portFrontend,
        basePathArgs = Some(new File(".")),
        startEngineTimeOut = 15000 // 15sec
      ),
      "sqlite-local" -> EngineSqlightLocal()
    )

    logDebug(s"Init variables for mixql context")
    val variables: mutable.Map[String, mtype.MType] = mutable.Map[String, mtype.MType]()

    if oozieParams.contains("mixql.org.engine.sqlight.db.path") then
      variables.put("mixql.org.engine.sqlight.db.path", mtype.MString(oozieParams("mixql.org.engine.sqlight.db.path")))

    logDebug(s"Mixql engine oozie platform: init Cluster context")
    val context = Context(
      engines,
      Try({
        oozieParams("org.mixql.platform.oozie.engines.default")
      }).getOrElse("sqlite-local"),
      variablesInit = variables
    )

    logDebug(s"Mixql engine oozie platform: prepare sql files")
    val sqlScriptFiles: List[File] = Try {
      oozieParams("org.mixql.platform.oozie.sql.files").split(";").map { fileName =>
        new File(fileName)
      }.toList
    }.getOrElse(List())

    try {
      logDebug(s"Mixql engine oozie platform: reading and executing sql files if they exist")
      if sqlScriptFiles.nonEmpty then
        sqlScriptFiles.map { (f: File) =>
          (f.getAbsolutePath, utils.FilesOperations.readFileContent(f))
        }.foreach(sql => {
          logDebug("Mixql engine oozie platform: running script: " + sql._1)
          run(sql._2, context)
        })
      else
        logInfo("Launching in WEB REPL mode")
        val webTextTerm = new WebTextTerminal()
        webTextTerm.init();
        val textIO = new TextIO(webTextTerm);
        val app = TerminalApp(context)
        val textIoApp = new SparkTextIoApp(app, textIO.getTextTerminal.asInstanceOf[WebTextTerminal])
        val webTextIoExecutor = new WebTextIoExecutor()
        webTextIoExecutor.withPort(8080)
        webTextIoExecutor.execute(textIoApp)
      end if

      logDebug(context.getParams().toString())
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
      Try({
        if BrokerModule.wasStarted then BrokerModule.close()
      })
    }

case class AppArgs(arguments: Seq[String]) extends ScallopConf(arguments):

  import org.rogach.scallop.stringConverter

  val oozieId = opt[String](descr = "oozie id", required = false)

  verify()
