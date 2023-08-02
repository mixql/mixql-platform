package org.mixql.engine.sqlite

import org.mixql.engine.core.logger.ModuleLogger

import scala.collection.mutable
import org.mixql.engine.core.{IModuleExecutor, PlatformContext}
import org.mixql.remote.messages.module.{DefinedFunctions, Execute, ExecuteFunction, ParamChanged}
import org.mixql.remote.messages.Message

class EngineSqlightExecutor extends IModuleExecutor with java.lang.AutoCloseable {

  var context: SQLightJDBC = null

  def functions: Map[String, Any] =
    Map(
      "sqlite_simple_proc" -> SqliteSimpleProc.simple_func,
      "sqlite_simple_proc_params" -> SqliteSimpleProc.simple_func_params,
      "sqlite_simple_proc_context_params" -> SqliteSimpleProc.simple_func_context_params
    )

  override def reactOnExecute(msg: Execute,
                              identity: String,
                              clientAddress: String,
                              logger: ModuleLogger,
                              platformContext: PlatformContext): Message = {
    import logger._
    if (context == null)
      context = new SQLightJDBC(identity, platformContext)
    logDebug(s"Received Execute msg from server statement: ${msg.statement}")
    logInfo(s"Executing command ${msg.statement}")
    //        Thread.sleep(1000)
    val res = context.execute(msg.statement)
    logInfo(s"Successfully executed command ${msg.statement}")
    logDebug(s"Sending reply on Execute msg " + res.getClass.getName)
    res
  }

  override def reactOnParamChanged(msg: ParamChanged,
                                   identity: String,
                                   clientAddress: String,
                                   logger: ModuleLogger,
                                   platformContext: PlatformContext): Unit = {
    import logger._
    logInfo(s"Module $identity :Received notify msg about changed param ${msg.name} from server $clientAddress: ")
  }

  override def reactOnExecuteFunction(msg: ExecuteFunction,
                                      identity: String,
                                      clientAddress: String,
                                      logger: ModuleLogger,
                                      platformContext: PlatformContext): Message = {
    import logger._
    if (context == null)
      context = new SQLightJDBC(identity, platformContext)
    logDebug(s"Started executing function ${msg.name}")
    logInfo(
      s"Executing function ${msg.name} with params " +
        msg.params.mkString("[", ",", "]")
    )
    val res = org.mixql.engine.core.FunctionInvoker.invoke(functions, msg.name, context, msg.params.toList)
    logInfo(s": Successfully executed function ${msg.name} ")
    res
  }

  override def reactOnGetDefinedFunctions(identity: String,
                                          clientAddress: String,
                                          logger: ModuleLogger): DefinedFunctions = {

    logger.logInfo(s"Received request to get defined functions from server")
    new DefinedFunctions(functions.keys.toArray)
  }

  override def reactOnShutDown(identity: String, clientAddress: String, logger: ModuleLogger): Unit = {}

  override def close(): Unit = {
    if (context != null)
      context.close()
  }
}
