package org.mixql.engine.stub.local

import org.mixql.cluster.internal.engine.InternalEngine

import scala.collection.mutable
import org.mixql.core.context.gtype
import org.mixql.core.context.gtype.Type
import org.mixql.core.function.FunctionInvoker
import org.mixql.engine.stub.local.EngineStubLocal.name

object EngineStubLocal extends InternalEngine {
  val engineParams: mutable.Map[String, gtype.Type] =
    mutable.Map()

  override def name: String = "mixql-engine-stub-local"

  override def executeStmt(statement: String): gtype.Type = {
    logDebug(
      s"Received statement to execute: ${statement}"
    )
    logDebug(s"Executing command :${statement} for 1sec")
    Thread.sleep(1000)
    logInfo(s"executed: ${statement}")
    gtype.Null
  }

  def functions: Map[String, Any] = Map(
    "stub_simple_proc" -> StubSimpleProc.simple_func,
    "stub_simple_proc_params" -> StubSimpleProc.simple_func_params,
    "stub_simple_proc_context_params" -> StubSimpleProc.simple_func_context_params,
  )

  override def execFunc(name: String, params: Type*): Type = {
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

  override def execSetParam(name: String, value: Type): Unit = {
    try {
      logDebug(
        s"Received request to set parameter $name with value $value"
      )
      engineParams.put(name, value)
      logDebug(s" Successfully have set parameter $name with value $value")
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
      logDebug(s" Successfully returned parameter $name with value $res")
      res
    } catch {
      case e: Throwable =>
        throw new Exception(s"[ENGINE ${this.name}]: error while executing get Param command: " + e.getMessage)
    }
  }

  override def execIsParam(name: String): Boolean = {
    logDebug(s" Received GetParam $name msg from server")
    logDebug(s"  Sending reply on GetParam $name msg")
    engineParams.keys.toSeq.contains(name)
  }

  override def registeredFunctions: List[String] = {
    logInfo(s" Was asked to get defined functions")
    functions.keys.toList
  }
}
