package org.mixql.engine.demo.scala.two.thirteen

import org.mixql.protobuf.messages

import scala.collection.mutable
import org.mixql.engine.core.IModuleExecutor
import org.mixql.engine.core.logger.ModuleLogger

class EngineDemoExecutor extends IModuleExecutor {
  val engineParams: mutable.Map[String, messages.Message] = mutable.Map()

  def reactOnExecute(msg: messages.Execute,
                     identity: String,
                     clientAddressString: String,
                     logger: ModuleLogger): messages.Message = {
    import logger._
    logInfo(s"Received Execute msg from server statement: ${msg.statement}")
    logDebug(s"Executing command ${msg.statement} for 1sec")
    Thread.sleep(1000)
    logInfo(s"Successfully executed command ${msg.statement}")
    logDebug(s"Sending reply on Execute msg")
    new messages.NULL()
  }

  def reactOnSetParam(msg: messages.SetParam,
                      identity: String,
                      clientAddress: String,
                      logger: ModuleLogger): messages.ParamWasSet = {
    import logger._
    logInfo(
      s"Module $identity :Received SetParam msg from server $clientAddress: " +
        s"must set parameter ${msg.name} with value ${msg.msg} "
    )
    engineParams.put(msg.name, msg.msg)
    logDebug(s"Sending reply on SetParam  ${msg.name} msg")
    new messages.ParamWasSet()
  }

  def reactOnGetParam(msg: messages.GetParam,
                      identity: String,
                      clientAddress: String,
                      logger: ModuleLogger): messages.Message = {
    import logger._
    logInfo(s"Received GetParam ${msg.name} msg from server")
    logDebug(s" Sending reply on GetParam ${msg.name} msg")
    engineParams.get(msg.name).get
  }

  def reactOnIsParam(msg: messages.IsParam,
                     identity: String,
                     clientAddress: String,
                     logger: ModuleLogger): messages.Bool = {
    import logger._
    logInfo(s"Received GetParam ${msg.name} msg from server")
    logDebug(s" Sending reply on GetParam ${msg.name} msg")
    new messages.Bool(engineParams.keys.toSeq.contains(msg.name))
  }

  def reactOnExecuteFunction(msg: messages.ExecuteFunction,
                             identity: String,
                             clientAddress: String,
                             logger: ModuleLogger): messages.Message = {
    import logger._
    logDebug(s"Started executing function ${msg.name}")
    logInfo(
      s"Executing function ${msg.name} with params " +
        msg.params.mkString("[", ",", "]")
    )
    new messages.NULL()
  }

  def reactOnGetDefinedFunctions(identity: String,
                                 clientAddress: String,
                                 logger: ModuleLogger): messages.DefinedFunctions = {
    logger.logInfo(s"Received request to get defined functions from server")
    new messages.DefinedFunctions(Seq().toArray)
  }

  def reactOnShutDown(identity: String, clientAddress: String, logger: ModuleLogger): Unit = {}

}
