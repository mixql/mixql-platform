package org.mixql.engine.sqlite.local

import org.mixql.cluster.internal.engine.InternalEngine
import org.mixql.core.context.gtype
import org.mixql.core.context.gtype.Type

import scala.collection.mutable

class EngineSqlightLocal( dbPathParameter: Option[String] = None)
    extends InternalEngine
    with java.lang.AutoCloseable:

  val engineParams: mutable.Map[String, gtype.Type] =
    mutable.Map()

  var context: SQLightJDBC = null

  override def name: String = "mixql-engine-sqlite-local"

  override def executeStmt(statement: String): gtype.Type = {
    logInfo(
      s"Received statement to execute: ${statement}"
    )
    logDebug(s"Executing command ${statement}")

    initContextIfEmpty()

    val res = context.execute(statement)
    logInfo(s"Successfully executed command ${statement}")
    logDebug(s"Returning result of  executed command '${statement}': $res")
    res
  }

  def initContextIfEmpty() = if context == null then
    logDebug(
      s"Init SQlightJDBC context"
    )
    context = SQLightJDBC(name, dbPathParameter)

  override def execFunc(name: String, params: Type*): Type = {
    try
      logInfo(s"Started executing function $name")
      logDebug(s"Params provided for function $name : " + params.toString())
      logDebug(s"Executing function $name with params " + params.toString)
      initContextIfEmpty()
      Thread.sleep(1000)
      logInfo(s"Successfully executed function $name with params " + params.toString)
      gtype.Null
    catch
      case e: Throwable =>
        throw new Exception(
          s"[ENGINE ${this.name}]: error while executing function $name: " +
            e.getMessage
        )
  }

  override def execSetParam(name: String, value: Type): Unit = {
    try {
      logDebug(
        s"Received request to set parameter $name with value $value"
      )
      engineParams.put(name, value)
      logDebug(s"Successfully have set parameter $name with value $value")
    } catch {
      case e: Throwable =>
        throw new Exception(s"[ENGINE ${this.name}] error while setting parameter: " + e.getMessage)
    }
  }

  override def execGetParam(name: String): Type = {
    logDebug(s"Received command to get parameter $name")
    logDebug(s"Trying to get parameter $name")
    try {
      val res = engineParams.get(name).get
      logDebug(s"Successfully returned parameter $name with value $res")
      res
    } catch {
      case e: Throwable =>
        throw new Exception(s"[ENGINE ${this.name}]: error while executing get Param command: " + e.getMessage)
    }
  }

  override def execIsParam(name: String): Boolean = {
    logDebug(s"Received GetParam $name msg from server")
    logDebug(s"Sending reply on GetParam $name msg")
    engineParams.keys.toSeq.contains(name)
  }

  override def close(): Unit =
    if context != null then context.close()
