package org.mixql.cluster.internal.engine

import org.mixql.core.engine.Engine
import org.mixql.core.context.gtype.Type

import java.io.File
import java.net.{InetSocketAddress, SocketAddress}
import java.nio.channels.{ServerSocketChannel, SocketChannel}
import scala.concurrent.Future
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Try
import org.mixql.core.context.{ContextVars, gtype}
import logger.ILogger

case class StashedParam(name: String, value: gtype.Type)

object InternalEngine {
}

//if start script name is not none then client must start remote engine by executing script
//which is {basePath}/{startScriptName}. P.S executor will be ignored
//if executor is not none and startScriptName is none then execute it in scala future
//if executor is none and startScript is none then just connect
abstract class InternalEngine extends Engine with ILogger {

  private var engineStarted: Boolean = false

  final override def execute(stmt: String, ctx: ContextVars): Type = {
    if (!engineStarted)
      logInfo(s" was triggered by execute request")

    engineStarted = true

    executeStmt(stmt, ctx)
  }

  def executeStmt(stmt: String, ctx: ContextVars): Type

  final override def executeFunc(name: String, ctx: ContextVars, params: Type*): Type = {
    if (!engineStarted)
      logInfo(s" was triggered by executeFunc request")
    engineStarted = true
    execFunc(name, ctx, params: _*)
  }

  def execFunc(name: String, ctx: ContextVars, params: Type*): Type


  final override def getDefinedFunctions(ctx: ContextVars): List[String] = {
    if (!engineStarted)
      logInfo(s" was triggered by getDefinedFunctions request")
    engineStarted = true
    registeredFunctions
  }

  def registeredFunctions: List[String] = Nil

  final override def paramChanged(name: String, ctx: ContextVars): Unit = {
    engineStarted = true
    execParamChanged(name, ctx)
  }

  def execParamChanged(name: String, ctx: ContextVars): Unit

}
