package org.mixql.engine.sqlite

import scala.collection.mutable
import org.mixql.engine.core.{BrakeException, IModuleExecutor, PlatformContext}
import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.remote.messages
import org.mixql.remote.messages.client.{Execute, ExecuteFunction}
import org.mixql.remote.messages.module.DefinedFunctions
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.rtype.mtype.MBool

object EngineSqlightExecutor extends IModuleExecutor:

//  var context: SQLightJDBC = null

  def functions: Map[String, Any] =
    Map(
      "sqlite_simple_proc" -> SqliteSimpleProc.simple_func,
      "sqlite_simple_proc_params" -> SqliteSimpleProc.simple_func_params,
      "sqlite_simple_proc_context_params" -> SqliteSimpleProc.simple_func_context_params
    )

  override def reactOnExecuteAsync(msg: Execute,
                                   identity: String,
                                   clientAddress: String,
                                   logger: ModuleLogger,
                                   platformContext: PlatformContext): Message = {
    import logger._
    val context = new SQLightJDBC(identity, platformContext, logger)
    try {
      logInfo(s"Received Execute msg from server statement: ${msg.statement}")
      logDebug(s"Executing command ${msg.statement}")
      //        Thread.sleep(1000)
      val res = context.execute(msg.statement)
      logInfo(s"Successfully executed command ${msg.statement}")
      logDebug(s"Sending reply on Execute msg " + res.getClass.getName)
      res
    } finally {
      context.close()
    }
  }

  override def reactOnExecuteFunctionAsync(msg: ExecuteFunction,
                                           identity: String,
                                           clientAddress: String,
                                           logger: ModuleLogger,
                                           platformContext: PlatformContext): Message = {
    import collection.JavaConverters._
    val context = new SQLightJDBC(identity, platformContext, logger)
    try {
      import logger._
      logDebug(s"Started executing function ${msg.name}")
      logInfo(
        s"Executing function ${msg.name} with params " +
          msg.params.mkString("[", ",", "]" + " and kwargs " + msg.getKwargs.asScala.mkString(","))
      )
      val res = org.mixql.engine.core.FunctionInvoker
        .invoke(functions, msg.name, List[Object](context, platformContext), msg.params.toList, msg.getKwargs.asScala)
      logInfo(s": Successfully executed function ${msg.name} ")
      res
    } finally {
      context.close()
    }
  }

  override def reactOnGetDefinedFunctions(identity: String,
                                          clientAddress: String,
                                          logger: ModuleLogger): DefinedFunctions = {

    import logger._
    import collection.JavaConverters._
    logInfo(s"Received request to get defined functions from server")
    DefinedFunctions(functions.keys.toArray, clientAddress)
  }

  override def reactOnShutDown(identity: String, clientAddress: String, logger: ModuleLogger): Unit = {}

//  override def close(): Unit = if context != null then context.close()
