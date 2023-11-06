package org.mixql.test.engines.EngineFail

import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.engine.core.{IModuleExecutor, PlatformContext}
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.client.{Execute, ExecuteFunction}
import org.mixql.remote.messages.module.DefinedFunctions
import org.mixql.remote.messages.rtype.mtype.MBool
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter, messages}

import scala.collection.mutable
import scala.sys.exit

class EngineFailExecutor extends IModuleExecutor {

  override def reactOnExecuteAsync(msg: Execute,
                                   identity: String,
                                   clientAddress: String,
                                   logger: ModuleLogger,
                                   platformContext: PlatformContext): Message = {
    import logger.*
    logInfo(s"Received Execute msg from server statement: ${msg.statement}")
    if (msg.statement.trim == "fail_on_next_cmd_whith_runtime_exception") {
      logInfo(s"Fail as was asked")
      exit(1)
    }
    logInfo(s"Executing command ${msg.statement} for 4sec")
    Thread.sleep(4000)
    logInfo(s"Successfully executed command ${msg.statement}")
    logDebug(s"Sending reply on Execute msg")
    messages.rtype.mtype.MNULL()
  }

  def functions: Map[String, Any] = Map()

  override def reactOnExecuteFunctionAsync(msg: ExecuteFunction,
                                           identity: String,
                                           clientAddress: String,
                                           logger: ModuleLogger,
                                           platformContext: PlatformContext): Message = {
    import logger.*
    import collection.JavaConverters._
    logInfo(s"Started executing function ${msg.name}")
    logDebug(
      s"Executing function ${msg.name} with params " +
        msg.params.mkString("[", ",", "]") + " and kwargs " + msg.getKwargs.asScala.mkString(",")
    )
    val res = org.mixql.engine.core.FunctionInvoker
      .invoke(functions, msg.name, List[Object](platformContext), msg.params.toList, msg.getKwargs.asScala)
    logInfo(s": Successfully executed function ${msg.name} ")
    res
  }

  override def reactOnGetDefinedFunctions(identity: String,
                                          clientAddress: String,
                                          logger: ModuleLogger): DefinedFunctions = {
    import logger.*
    logInfo(s"Received request to get defined functions from server")
    DefinedFunctions(functions.keys.toArray, clientAddress)
  }

  override def reactOnShutDown(identity: String, clientAddress: String, logger: ModuleLogger): Unit = {}

}
