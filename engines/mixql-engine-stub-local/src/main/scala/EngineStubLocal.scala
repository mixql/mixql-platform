package org.mixql.engine.stub.local

import scala.collection.mutable
import org.mixql.core.engine.Engine
import org.mixql.core.context.gtype
import org.mixql.core.context.gtype.Type
import org.mixql.core.function.FunctionInvoker
import org.mixql.engine.stub.local.EngineStubLocal.name

object EngineStubLocal extends Engine {
  val engineParams: mutable.Map[String, gtype.Type] =
    mutable.Map()

  override def name: String = "mixql-engine-stub-local"

  override def execute(statement: String): gtype.Type = {
    println(
      s"[ENGINE $name] : Received statement to execute: ${statement}"
    )
    println(s"[ENGINE $name] : Executing command ${statement} for 1sec")
    Thread.sleep(1000)
    println(s"[ENGINE $name] : Successfully executed command ${statement}")
    gtype.Null
  }

  val functions: Map[String, Any] = Map(
    "stub_simple_proc" -> StubSimpleProc.simple_func,
    "stub_simple_proc_params" -> StubSimpleProc.simple_func_params,
    "stub_simple_proc_context_params" -> StubSimpleProc.simple_func_context_params,
  )

  override def executeFunc(name: String, params: Type*): Type = {
    import org.mixql.core.context.gtype
    try
      println(s"[ENGINE ${this.name}] :Started executing function $name")
      println(s"[ENGINE ${this.name}] :Params provided for function $name : " + params.toString())
      println(s"[ENGINE ${this.name}] :Executing function $name with params " + params.toString)
      val res = FunctionInvoker.invoke(functions, name, StubContext(), params.map(p => gtype.unpack(p)))
      println(s"[ENGINE ${this.name}] : Successfully executed function $name with params " + params.toString +
        s"\nResult: $res")
      gtype.pack(res)
    catch
      case e: Throwable =>
        throw new Exception(
          s"[ENGINE ${this.name}]: error while executing function $name: " +
            e.getMessage
        )
  }

  override def setParam(name: String, value: Type): Unit = {
    try {
      println(
        s"[ENGINE ${this.name}]  :Received request to set parameter $name with value $value"
      )
      engineParams.put(name, value)
      println(s"[ENGINE ${this.name}] : Successfully have set parameter $name with value $value")
    } catch {
      case e: Throwable =>
        throw new Exception(s"[ENGINE ${this.name}] error while setting parameter: " + e.getMessage)
    }
  }

  override def getParam(name: String): Type = {
    println(s"[ENGINE ${this.name}] : Received command to get parameter $name")
    println(s"[ENGINE ${this.name}] : Trying to get parameter $name")
    try {
      val res = engineParams.get(name).get
      println(s"[ENGINE ${this.name}] : Successfully returned parameter $name with value $res")
      res
    } catch {
      case e: Throwable =>
        throw new Exception(s"[ENGINE ${this.name}]: error while executing get Param command: " + e.getMessage)
    }
  }

  override def isParam(name: String): Boolean = {
    println(s"[ENGINE ${this.name}] : Received GetParam $name msg from server")
    println(s"[ENGINE ${this.name}] :  Sending reply on GetParam $name msg")
    engineParams.keys.toSeq.contains(name)
  }

  override def getDefinedFunctions: List[String] = {
    println(s"[ENGINE ${this.name}] : Was asked to get defined functions")
    List("stub_simple_proc", "stub_simple_proc_params", "stub_simple_proc_context_params")
  }
}
