package org.mixql.platform.oozie

import org.mixql.core.run
import org.rogach.scallop.ScallopConf

import java.io.File
import org.mixql.cluster.ClientModule
import org.mixql.core.engine.Engine
import org.mixql.core.context.{Context, gtype}
import org.mixql.protobuf.messages.ShutDown
import org.mixql.engine.sqlite.local.EngineSqlightLocal

import scala.collection.mutable
import org.mixql.platform.oozie.logger.*
import org.mixql.oozie.OozieParamsReader

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
      Some(oozieParams("org.mixql.cluster.broker.portFrontend").toInt)
    }.getOrElse(None)

    val portBackend = Try {
      Some(oozieParams("org.mixql.cluster.broker.portBackend").toInt)
    }.getOrElse(None)

    logDebug(s"Mixql engine demo platform: initialising engines")
    val engines = mutable.Map[String, Engine](
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
        host, portFrontend, portBackend, Some(new File("."))
      ),
      "sqlite-local" -> EngineSqlightLocal()
    )

    logDebug(s"Init variables for mixql context")
    val variables: mutable.Map[String, gtype.Type] = mutable.Map[String, gtype.Type]()

    if oozieParams.contains("mixql.org.engine.sqlight.db.path") then
      variables.put("mixql.org.engine.sqlight.db.path", gtype.string(
        oozieParams("mixql.org.engine.sqlight.db.path")
      ))


    logDebug(s"Mixql engine oozie platform: init Cluster context")
    val context =
      new Context(engines, Try({
        oozieParams("org.mixql.platform.oozie.engines.default")
      }).getOrElse("sqlite-local"), variables = variables)

    logDebug(s"Mixql engine oozie platform: prepare sql files")
    val sqlScriptFiles: List[File] = Try {
      oozieParams("org.mixql.platform.oozie.sql.files").split(";").map {
        fileName => new File(fileName)
      }.toList
    }.getOrElse(List())

    try {
      logDebug(s"Mixql engine oozie platform: reading and executing sql files if they exist")
      sqlScriptFiles.map {
        (f: File) => (f.getAbsolutePath, utils.FilesOperations.readFileContent(f))
      }.foreach(sql => {
        logDebug("Mixql engine oozie platform: running script: " + sql._1)
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

case class AppArgs(arguments: Seq[String]) extends ScallopConf(arguments) :

  import org.rogach.scallop.stringConverter

  val oozieId = opt[String](descr = "oozie id", required = false)

  verify()

