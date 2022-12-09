package org.mixql.engine.demo

import com.typesafe.config.*
import org.rogach.scallop.ScallopConf

import org.mixql.engine.core

object MixQlEngineSqlight {

  def main(args: Array[String]): Unit = {
    val appArgs: AppArgs = AppArgs(args)
    implicit val indentity = String(appArgs.identity.toOption.get.getBytes)
    import org.rogach.scallop.ScallopConfBase
    val host: String = appArgs.host.toOption.get
    val port = appArgs.port.toOption.get
    println(s"Module $indentity: Starting main client")

    new core.Module(EngineSqlightExecutor, indentity, host, port).startServer()
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
