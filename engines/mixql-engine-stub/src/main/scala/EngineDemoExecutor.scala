package org.mixql.engine.demo

import org.mixql.protobuf.{GtypeConverter, ProtoBufConverter}
import org.mixql.protobuf.messages

import scala.collection.mutable
import org.mixql.engine.core.IModuleExecutor

object EngineDemoExecutor extends IModuleExecutor {
  val engineParams: mutable.Map[String, messages.Message] =
    mutable.Map()

  def reactOnExecute(msg: messages.Execute, identity: String, clientAddress: String): messages.Message = {
    println(
      s"Module $identity: Received Execute msg from server statement: ${msg.statement}"
    )
    println(s"Module $identity: Executing command ${msg.statement} for 1sec")
    Thread.sleep(1000)
    println(s"Module $identity: Successfully executed command ${msg.statement}")
    println(s"Module $identity: Sending reply on Execute msg")
    messages.NULL()
  }

  def reactOnSetParam(msg: messages.SetParam, identity: String, clientAddress: String): messages.ParamWasSet = {
    println(
      s"Module $identity :Received SetParam msg from server $clientAddress: " +
        s"must set parameter ${msg.name} with value ${msg.msg}"
    )
    engineParams.put(
      msg.name,
      msg.msg
    )
    println(s"Module $identity: Sending reply on SetParam  ${msg.name} msg")
    messages.ParamWasSet()
  }

  def reactOnGetParam(msg: messages.GetParam, identity: String, clientAddress: String): messages.Message = {
    println(s"Module $identity: Received GetParam ${msg.name} msg from server")
    println(s"Module $identity:  Sending reply on GetParam ${msg.name} msg")
    engineParams.get(msg.name).get
  }

  def reactOnIsParam(msg: messages.IsParam, identity: String, clientAddress: String): messages.Bool = {
    println(s"Module $identity: Received GetParam ${msg.name} msg from server")
    println(s"Module $identity:  Sending reply on GetParam ${msg.name} msg")
    messages.Bool(engineParams.keys.toSeq.contains(msg.name))
  }

  def functions: Map[String, Any] = Map(
    "stub_simple_proc" -> StubSimpleProc.simple_func,
    "stub_simple_proc_params" -> StubSimpleProc.simple_func_params,
    "stub_simple_proc_context_params" -> StubSimpleProc.simple_func_context_params,
    "stub_simple_func_return_arr" -> StubSimpleProc.simple_func_return_arr,
    "stub_simple_func_return_map" -> StubSimpleProc.simple_func_return_map
  )

  val context = StubContext()

  def reactOnExecuteFunction(msg: messages.ExecuteFunction, identity: String,
                             clientAddress: String): messages.Message = {
    println(s"Started executing function ${msg.name}")
    println(s"[Module-$identity] Executing function ${msg.name} with params " +
      msg.params.mkString("[", ",", "]"))
    val res = org.mixql.engine.core.FunctionInvoker.invoke(functions, msg.name, context, msg.params.toList)
    println(s"[Module-$identity] : Successfully executed function ${msg.name} ")
    res
  }

  def reactOnGetDefinedFunctions(identity: String,
                                 clientAddress: String): messages.DefinedFunctions = {
    println(s"Module $identity: Received request to get defined functions from server")
    messages.DefinedFunctions(functions.keys.toArray)
  }

  def reactOnShutDown(identity: String, clientAddress: String): Unit = {}

}
