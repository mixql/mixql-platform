package org.mixql.engine.stub.local

import org.mixql.cluster.internal.engine.InternalEngine

import scala.collection.mutable
import org.mixql.core.context.{ContextVars, gtype}
import org.mixql.core.context.gtype.Type
import org.mixql.core.function.FunctionInvoker
import org.mixql.engine.stub.local.EngineStubLocal.name

object EngineStubLocal extends InternalEngine {
  val engineParams: mutable.Map[String, gtype.Type] =
    mutable.Map()

  override def name: String = "mixql-engine-stub-local"

  override def executeStmt(statement: String, ctx: ContextVars): gtype.Type = {
    logDebug(
      s"Received statement to execute: ${statement}"
    )
    logDebug(s"Executing command :${statement} for 1sec")
    Thread.sleep(1000)
    logInfo(s"executed: ${statement}")
    new gtype.Null()
  }

  def functions: Map[String, Any] = Map(
    "stub_simple_proc" -> StubSimpleProc.simple_func,
    "stub_simple_proc_params" -> StubSimpleProc.simple_func_params,
    "stub_simple_proc_context_params" -> StubSimpleProc.simple_func_context_params,
  )

  override def execFunc(name: String, ctx: ContextVars, params: Type*): Type = {
    import org.mixql.core.context.gtype
    try
      logInfo(s"Started executing function $name")
      logDebug(s"Params provided for function $name : " + params.toString())
      logDebug(s"Executing function $name with params " + params.toString)
      val res = FunctionInvoker.invoke(functions, name, StubContext(), params.map(p => gtype.unpack(p)).toList)
      logInfo(s" Successfully executed function $name with params " + params.toString +
        s"\nResult: $res")
      gtype.pack(res)
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
        s"Received notification that parameter $name was changed"
      )
    } catch {
      case e: Throwable =>
        throw new Exception(s"[ENGINE ${this.name}] error while setting parameter: " + e.getMessage)
    }
  }

  override def registeredFunctions: List[String] = {
    logInfo(s" Was asked to get defined functions")
    functions.keys.toList
  }
}
