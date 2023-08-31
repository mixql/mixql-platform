package org.mixql.engine.demo

import scala.collection.mutable
import org.mixql.engine.core.{IModuleExecutor, PlatformContext}
import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.remote.messages.gtype.Bool
import org.mixql.remote.messages.module.{DefinedFunctions, Execute, ExecuteFunction, ParamChanged}
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter, messages}
import org.mixql.remote.messages.{Message, gtype}

object EngineDemoExecutor extends IModuleExecutor {

  override def reactOnExecuteAsync(msg: Execute,
                                   identity: String,
                                   clientAddress: String,
                                   logger: ModuleLogger,
                                   platformContext: PlatformContext): Message = {
    import logger._
    logDebug(s"Received Execute msg from server statement: ${msg.statement}")
    logInfo(s"Executing command ${msg.statement} for 1sec")
    Thread.sleep(1000)
    logInfo(s"Successfully executed command ${msg.statement}")
    logDebug(s"Sending reply on Execute msg")
    messages.gtype.NULL()
  }

  override def reactOnParamChangedAsync(msg: ParamChanged,
                                        identity: String,
                                        clientAddress: String,
                                        logger: ModuleLogger,
                                        platformContext: PlatformContext): Unit = {
    import logger._
    logInfo(s"Module $identity :Received notify msg about changed param ${msg.name} from server $clientAddress: ")
  }

  def functions: Map[String, Any] =
    Map(
      "stub_simple_proc" -> StubSimpleProc.simple_func,
      "stub_simple_proc_params" -> StubSimpleProc.simple_func_params,
      "stub_simple_proc_context_params" -> StubSimpleProc.simple_func_context_params,
      "stub_simple_func_return_arr" -> StubSimpleProc.simple_func_return_arr,
      "stub_simple_func_return_map" -> StubSimpleProc.simple_func_return_map,
      "execute_platform_func_in_stub_func" -> StubSimpleProc.execute_platform_func_in_stub_func,
      "execute_stub_func_using_platform_in_stub_func" -> StubSimpleProc.execute_stub_func_using_platform_in_stub_func,
      "stub_simple_proc_context" -> StubSimpleProc.stub_simple_proc_context,
      "stub_simple_proc_context_test_setting_getting_vars" -> StubSimpleProc
        .stub_simple_proc_context_test_setting_getting_vars
    )

  val context = StubContext()

  override def reactOnExecuteFunctionAsync(msg: ExecuteFunction,
                                           identity: String,
                                           clientAddress: String,
                                           logger: ModuleLogger,
                                           platformContext: PlatformContext): Message = {
    import logger._
    logInfo(s"Started executing function ${msg.name}")
    logDebug(
      s"Executing function ${msg.name} with params " +
        msg.params.mkString("[", ",", "]")
    )
    val res = org.mixql.engine.core.FunctionInvoker
      .invoke(functions, msg.name, List[Object](platformContext, context), msg.params.toList)
    logInfo(s": Successfully executed function ${msg.name} ")
    res
  }

  override def reactOnGetDefinedFunctions(identity: String,
                                          clientAddress: String,
                                          logger: ModuleLogger): DefinedFunctions = {
    import logger._
    logInfo(s"Received request to get defined functions from server")
    DefinedFunctions(functions.keys.toArray)
  }

  override def reactOnShutDown(identity: String, clientAddress: String, logger: ModuleLogger): Unit = {}

}
