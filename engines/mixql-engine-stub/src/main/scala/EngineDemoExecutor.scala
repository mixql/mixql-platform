package org.mixql.engine.demo

import scala.collection.mutable
import org.mixql.engine.core.{IModuleExecutor, PlatformContext}
import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.remote.messages.client.{Execute, ExecuteFunction}
import org.mixql.remote.messages.module.DefinedFunctions
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter, messages}
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.rtype.mtype.MBool

import scala.util.Random

object EngineDemoExecutor extends IModuleExecutor {
  val r: Random.type = scala.util.Random

  override def reactOnExecuteAsync(msg: Execute,
                                   identity: String,
                                   clientAddress: String,
                                   logger: ModuleLogger,
                                   platformContext: PlatformContext): Message = {
    import logger._
    logDebug(s"Received Execute msg from server statement: ${msg.statement}")
    val timeout = getRandomLongInInterval(500, 7000)
    logInfo(s"Executing command ${msg.statement} for $timeout milliseconds")
    Thread.sleep(timeout)
    logInfo(s"Successfully executed command ${msg.statement}")
    logDebug(s"Sending reply on Execute msg")
    messages.rtype.mtype.MNULL()
  }

  def getRandomLongInInterval(start: Long, end: Long): Long = {
    start + r.nextLong((end - start) + 1)
  }

  def functions: Map[String, Object] =
    Map(
      "stub_simple_proc" -> StubSimpleProc.simple_func,
      "stub_simple_proc_params" -> StubSimpleProc.simple_func_params,
      "stub_simple_proc_context_params" -> StubSimpleProc.simple_func_context_params,
      "stub_simple_func_return_arr" -> StubSimpleProc.simple_func_return_arr,
      "stub_simple_func_return_map" -> StubSimpleProc.simple_func_return_map,
      "execute_platform_func_in_stub_func" -> StubSimpleProc.execute_platform_func_in_stub_func,
      "execute_stub_func_using_platform_in_stub_func" -> StubSimpleProc.execute_stub_func_using_platform_in_stub_func,
      "stub_simple_proc_context" -> StubSimpleProc.stub_simple_proc_context,
      "execute_stub_func_long_sleep" -> StubSimpleProc.execute_stub_func_long_sleep,
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
    import collection.JavaConverters._
    logInfo(s"Started executing function ${msg.name}")
    logDebug(
      s"Executing function ${msg.name} with params " +
        msg.params.mkString("[", ",", "]") + " and kwargs " + msg.getKwargs.asScala.mkString(",")
    )
    val res = org.mixql.engine.core.FunctionInvoker
      .invoke(functions, msg.name, List[Object](platformContext, context), msg.params.toList, msg.getKwargs.asScala)
    logInfo(s": Successfully executed function ${msg.name} ")
    res
  }

  override def reactOnGetDefinedFunctions(identity: String,
                                          clientAddress: String,
                                          logger: ModuleLogger): DefinedFunctions = {
    import logger._
    logInfo(s"Received request to get defined functions from server")
    DefinedFunctions(functions.keys.toArray, clientAddress)
  }

  override def reactOnShutDown(identity: String, clientAddress: String, logger: ModuleLogger): Unit = {}

}
