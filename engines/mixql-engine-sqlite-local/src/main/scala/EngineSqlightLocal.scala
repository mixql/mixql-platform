package org.mixql.engine.sqlite.local

import org.mixql.cluster.internal.engine.InternalEngine
import org.mixql.core.context.{ContextVars, gtype}
import org.mixql.core.context.gtype.Type

import scala.collection.mutable

class EngineSqlightLocal(dbPathParameter: Option[String] = None)
  extends InternalEngine
    with java.lang.AutoCloseable:

  //  val engineParams: mutable.Map[String, gtype.Type] =
  //    mutable.Map()

  var context: SQLightJDBC = null

  override def name: String = "mixql-engine-sqlite-local"

  override def executeStmt(statement: String, ctx: ContextVars): gtype.Type = {
    logInfo(
      s"Received statement to execute: ${statement}"
    )
    logDebug(s"Executing command ${statement}")

    initContextIfEmpty(ctx)

    val res = context.execute(statement)
    logInfo(s"Successfully executed command ${statement}")
    logDebug(s"Returning result of  executed command '${statement}': $res")
    res
  }

  private def initContextIfEmpty(ctx: ContextVars): Unit = if context == null then
    logDebug(
      s"Init SQlightJDBC context"
    )
    context = SQLightJDBC(name, ctx, dbPathParameter)

  override def execFunc(name: String, ctx: ContextVars, params: Type*): Type = {
    try
      logInfo(s"Started executing function $name")
      logDebug(s"Params provided for function $name : " + params.toString())
      logDebug(s"Executing function $name with params " + params.toString)
      initContextIfEmpty(ctx)
      Thread.sleep(1000)
      logInfo(s"Successfully executed function $name with params " + params.toString)
      new gtype.Null()
    catch
      case e: Throwable =>
        throw new Exception(
          s"[ENGINE ${this.name}]: error while executing function $name: " +
            e.getMessage
        )
  }

  override def execParamChanged(name: String, ctx: ContextVars): Unit = {
    try {
      logDebug(
        s"Received notification that param $name was changed"
      )
    } catch {
      case e: Throwable =>
        throw new Exception(s"[ENGINE ${this.name}] error while setting parameter: " + e.getMessage)
    }
  }

  override def close(): Unit =
    if context != null then context.close()
