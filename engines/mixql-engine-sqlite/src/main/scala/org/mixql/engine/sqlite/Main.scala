package org.mixql.engine.sqlite

import com.typesafe.config.*
import org.rogach.scallop.ScallopConf

import org.mixql.engine.core
import org.mixql.engine.core.logger.ModuleLogger

object MixQlEngineSqlight {

  def main(args: Array[String]): Unit = {
    val appArgs: AppArgs = AppArgs(args)
    implicit val indentity = String(appArgs.identity.toOption.get.getBytes)
    import org.rogach.scallop.ScallopConfBase
    val host: String = appArgs.host.toOption.get
    val port = appArgs.port.toOption.get
    implicit val logger = new ModuleLogger(indentity, appArgs.logLevel.toOption.get)
    logger.logInfo(s"Starting MixQlEngineSqlite")

    new core.Module(EngineSqlightExecutor, indentity, host, port).startServer()
  }
}

case class AppArgs(arguments: Seq[String]) extends ScallopConf(arguments) {

  import org.rogach.scallop.stringConverter
  import org.rogach.scallop.intConverter

  val port = opt[Int](required = true)
  val host = opt[String](required = true)
  val identity = opt[String](required = true)
  val logLevel = opt[String](required = true)
  verify()
}
