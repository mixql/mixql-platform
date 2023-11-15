package org.mixql.test.engines.Stub5SecTimeout

import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.engine.core.{IModuleExecutor, PlatformContext}
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.client.{Execute, ExecuteFunction}
import org.mixql.remote.messages.module.DefinedFunctions
import org.mixql.remote.messages.rtype.mtype.MBool
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter, messages}

import scala.collection.mutable
import scala.util.Random

object EngineDemo5SecExecutor extends IModuleExecutor {
  val r: Random.type = scala.util.Random

  override def reactOnExecuteAsync(msg: Execute,
                                   identity: String,
                                   clientAddress: String,
                                   logger: ModuleLogger,
                                   platformContext: PlatformContext): Message = {
    import logger.*
    logDebug(s"Received Execute msg from server statement: ${msg.statement}")
    logInfo(s"Executing command ${msg.statement} for 5000 milliseconds")
    Thread.sleep(5000)
    logInfo(s"Successfully executed command ${msg.statement}")
    logDebug(s"Sending reply on Execute msg")
    messages.rtype.mtype.MNULL()
  }

  def getRandomLongInInterval(start: Long, end: Long): Long = {
    start + r.nextLong((end - start) + 1)
  }

  def functions: Map[String, Object] = Map()

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
