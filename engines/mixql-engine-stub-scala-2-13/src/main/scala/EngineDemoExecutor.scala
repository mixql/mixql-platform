package org.mixql.engine.demo.scala.two.thirteen

import scala.collection.mutable
import org.mixql.engine.core.{IModuleExecutor, PlatformContext}
import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.remote.messages.gtype.{Bool, NULL}
import org.mixql.remote.messages.module.{DefinedFunctions, Execute, ExecuteFunction, ParamChanged}
import org.mixql.remote.messages.Message

class EngineDemoExecutor extends IModuleExecutor {

  override def reactOnExecute(msg: Execute,
                              identity: String,
                              clientAddressString: String,
                              logger: ModuleLogger,
                              platformContext: PlatformContext): Message = {
    import logger._
    logInfo(s"Received Execute msg from server statement: ${msg.statement}")
    logDebug(s"Executing command ${msg.statement} for 1sec")
    Thread.sleep(1000)
    logInfo(s"Successfully executed command ${msg.statement}")
    logDebug(s"Sending reply on Execute msg")
    new NULL()
  }

  override def reactOnParamChanged(msg: ParamChanged,
                                   identity: String,
                                   clientAddress: String,
                                   logger: ModuleLogger,
                                   platformContext: PlatformContext): Unit = {
    import logger._
    logInfo(s"Received notify msg about changed param ${msg.name} from server $clientAddress: ")
  }

  override def reactOnExecuteFunction(msg: ExecuteFunction,
                                      identity: String,
                                      clientAddress: String,
                                      logger: ModuleLogger,
                                      platformContext: PlatformContext): Message = {
    import logger._
    logDebug(s"Started executing function ${msg.name}")
    logInfo(
      s"Executing function ${msg.name} with params " +
        msg.params.mkString("[", ",", "]")
    )
    new NULL()
  }

  override def reactOnGetDefinedFunctions(identity: String,
                                          clientAddress: String,
                                          logger: ModuleLogger): DefinedFunctions = {
    logger.logInfo(s"Received request to get defined functions from server")
    new DefinedFunctions(Seq().toArray)
  }

  override def reactOnShutDown(identity: String, clientAddress: String, logger: ModuleLogger): Unit = {}

}
