package org.mixql.engine.demo

import org.mixql.protobuf.{GtypeConverter, ProtoBufConverter}
import org.mixql.protobuf.messages
import scala.collection.mutable
import org.mixql.engine.core.IModuleExecutor
import org.mixql.engine.core.logger.ModuleLogger

object EngineDemoExecutor extends IModuleExecutor {
  val engineParams: mutable.Map[String, messages.Message] =
    mutable.Map()

  def reactOnExecute(msg: messages.Execute, identity: String,
                     clientAddress: String, logger: ModuleLogger): messages.Message = {
    import logger._
    logDebug(
      s"Received Execute msg from server statement: ${msg.statement}"
    )
    logInfo(s"Executing command ${msg.statement} for 1sec")
    Thread.sleep(1000)
    logInfo(s"Successfully executed command ${msg.statement}")
    logDebug(s"Sending reply on Execute msg")
    messages.NULL()
  }

  def reactOnSetParam(msg: messages.SetParam, identity: String,
                      clientAddress: String, logger: ModuleLogger): messages.ParamWasSet = {
    import logger._
    logInfo(
      s"Received SetParam msg from server $clientAddress: " +
        s"must set parameter ${msg.name} with value ${msg.msg}"
    )
    engineParams.put(
      msg.name,
      msg.msg
    )
    logDebug(s"Sending reply on SetParam  ${msg.name} msg")
    messages.ParamWasSet()
  }

  def reactOnGetParam(msg: messages.GetParam, identity: String,
                      clientAddress: String, logger: ModuleLogger): messages.Message = {
    import logger._
    logInfo(s"Received GetParam ${msg.name} msg from server")
    logDebug(s" Sending reply on GetParam ${msg.name} msg")
    engineParams.get(msg.name).get
  }

  def reactOnIsParam(msg: messages.IsParam, identity: String,
                     clientAddress: String, logger: ModuleLogger): messages.Bool = {
    import logger._
    logInfo(s"Received GetParam ${msg.name} msg from server")
    logDebug(s" Sending reply on GetParam ${msg.name} msg")
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
                             clientAddress: String, logger: ModuleLogger): messages.Message = {
    import logger._
    logInfo(s"Started executing function ${msg.name}")
    logDebug(s"Executing function ${msg.name} with params " +
      msg.params.mkString("[", ",", "]"))
    val res = org.mixql.engine.core.FunctionInvoker.invoke(functions, msg.name, context, msg.params.toList)
    logInfo(s": Successfully executed function ${msg.name} ")
    res
  }

  def reactOnGetDefinedFunctions(identity: String,
                                 clientAddress: String, logger: ModuleLogger): messages.DefinedFunctions = {
    import logger._
    logInfo(s"Received request to get defined functions from server")
    messages.DefinedFunctions(functions.keys.toArray)
  }

  def reactOnShutDown(identity: String, clientAddress: String, logger: ModuleLogger): Unit = {}

}
