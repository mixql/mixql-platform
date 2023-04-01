package org.mixql.cluster.internal.engine

import com.typesafe.config.ConfigFactory
import org.mixql.core.engine.Engine
import org.mixql.core.context.gtype.Type
import java.io.File
import java.net.{InetSocketAddress, SocketAddress}
import java.nio.channels.{ServerSocketChannel, SocketChannel}
import scala.concurrent.Future
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Try
import org.mixql.core.context.gtype
import logger.ILogger

case class StashedParam(name: String, value: gtype.Type)

object InternalEngine {
  val config = ConfigFactory.load()
}

//if start script name is not none then client must start remote engine by executing script
//which is {basePath}/{startScriptName}. P.S executor will be ignored
//if executor is not none and startScriptName is none then execute it in scala future
//if executor is none and startScript is none then just connect
abstract class InternalEngine extends Engine with ILogger {

  private var engineStarted: Boolean = false
  private val engineStashedParams: ListBuffer[StashedParam] = ListBuffer()

  private var haveSetStashedParams: Boolean = false

  private def stashMessage(
                    name: String,
                    value: gtype.Type,
                  ) = {
    logDebug(
      s"started to stash parameter $name with value $value"
    )
    engineStashedParams += StashedParam(name, value)
    logDebug(
      s"successfully stashed parameter $name with value $value"
    )
  }

  private def setStashedParamsIfTheyAre() = {
    haveSetStashedParams = true
    logDebug(s"Check if there are stashed params")
    if (engineStashedParams.isEmpty)
      logDebug(
        s"Checked: No stashed messages for $name"
      )
    else {
      logDebug(
        s"Have founded stashed messages (amount: ${engineStashedParams.length}). Set them"
      )
      engineStarted = true
      engineStashedParams.foreach(msg =>
        execSetParam(msg.name, msg.value)
      )
      engineStashedParams.clear()
    }
  }

  final override def execute(stmt: String): Type = {
    if (!engineStarted)
      logInfo(s" was triggered by execute request")

    setStashedParamsIfTheyAre()
    engineStarted = true

    executeStmt(stmt)
  }

  def executeStmt(stmt: String): Type

  final override def executeFunc(name: String, params: Type*): Type = {
    if (!engineStarted)
      logInfo(s" was triggered by executeFunc request")
    setStashedParamsIfTheyAre()
    engineStarted = true
    execFunc(name, params: _*)
  }

  def execFunc(name: String, params: Type*): Type


  final override def getDefinedFunctions: List[String] = {
    if (!engineStarted)
      logInfo(s" was triggered by getDefinedFunctions request")
    setStashedParamsIfTheyAre()
    engineStarted = true
    registeredFunctions
  }

  def registeredFunctions: List[String] = Nil

  final override def setParam(name: String, value: Type): Unit = {
    if (haveSetStashedParams) {
      engineStarted = true
      execSetParam(name, value)
    }
    else
      stashMessage(name, value)
  }

  def execSetParam(name: String, value: Type): Unit

  final override def getParam(name: String): Type = {
    if (!engineStarted)
      logInfo(s" was triggered by getParam request")
    setStashedParamsIfTheyAre()
    engineStarted = true
    execGetParam(name)
  }

  def execGetParam(name: String): Type

  final override def isParam(name: String): Boolean = {
    if (!engineStarted)
      logInfo(s" was triggered by isParam request")
    setStashedParamsIfTheyAre()
    engineStarted = true
    execIsParam(name)
  }

  def execIsParam(name: String): Boolean
}
