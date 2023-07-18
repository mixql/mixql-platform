package org.mixql.engine.demo.scala.two.thirteen

import com.typesafe.config._
import org.rogach.scallop.ScallopConf

import org.mixql.engine.core
import org.mixql.engine.core.logger.ModuleLogger

object MixQlEngineDemo {

  def main(args: Array[String]): Unit = {
    val appArgs: AppArgs = AppArgs(args)
    implicit val indentity = new String(appArgs.identity.toOption.get.getBytes)
    import org.rogach.scallop.ScallopConfBase
    val host: String = appArgs.host.toOption.get
    val port = appArgs.port.toOption.get
    implicit val logger = new ModuleLogger(indentity)
    logger.logInfo(s"Starting main client")

    new core.Module(new EngineDemoExecutor(), indentity, host, port).startServer()
  }
}

case class AppArgs(arguments: Seq[String]) extends ScallopConf(arguments) {

  import org.rogach.scallop.stringConverter
  import org.rogach.scallop.intConverter

  val port = opt[Int](required = true)
  val host = opt[String](required = true)
  val identity = opt[String](required = true)
  verify()
}
