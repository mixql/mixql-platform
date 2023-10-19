package org.mixql.engine.stub.local

import scala.collection.mutable
import org.mixql.core.context.{EngineContext, mtype}
import org.mixql.core.context.mtype.MType
import org.mixql.core.engine.Engine
import org.mixql.core.function.FunctionInvoker
import org.mixql.engine.local.logger.IEngineLogger
import org.mixql.engine.stub.local.EngineStubLocal.name

object EngineStubLocal extends Engine with IEngineLogger {

  override def name: String = "mixql-engine-stub-local"

  override def executeImpl(statement: String, ctx: EngineContext): mtype.MType = {
    logDebug(s"Received statement to execute: ${statement}")
    logDebug(s"Executing command :${statement} for 1sec")
    Thread.sleep(1000)
    logInfo(s"executed: ${statement}")
    mtype.MNull.get()
  }

  def functions: Map[String, Any] =
    Map(
      "stub_simple_proc" -> StubSimpleProc.simple_func,
      "stub_simple_proc_params" -> StubSimpleProc.simple_func_params,
      "stub_simple_proc_context_params" -> StubSimpleProc.simple_func_context_params
    )

  override def executeFuncImpl(name: String, ctx: EngineContext, kwargs: Map[String, Object], params: MType*): MType = {
    try
      logInfo(s"Started executing function $name")
      logDebug(s"Params provided for function $name : " + params.toString())
      logDebug(s"Executing function $name with params " + params.toString)
      val res = FunctionInvoker
        .invoke(functions, name, List[Object](StubContext(), ctx), params.map(p => mtype.unpack(p)).toList, kwargs)
      logInfo(
        s" Successfully executed function $name with params " + params.toString +
          s"\nResult: $res"
      )
      mtype.pack(res)
    catch
      case e: Throwable =>
        throw new Exception(
          s"[ENGINE ${this.name}]: error while executing function $name: " +
            e.getMessage
        )
  }

  override def getDefinedFunctions(): List[String] = {
    logInfo(s" Was asked to get defined functions")
    functions.keys.toList
  }
}
