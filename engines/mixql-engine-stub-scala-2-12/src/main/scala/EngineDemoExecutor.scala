package org.mixql.engine.demo.scala.two.twelf

import org.mixql.protobuf.messages

import scala.collection.mutable
import org.mixql.engine.core.IModuleExecutor

import _root_.scala.collection.immutable.Seq

object EngineDemoExecutor extends IModuleExecutor {
  val engineParams: mutable.Map[String, messages.Message] =
    mutable.Map()

  def reactOnExecute(msg: messages.Execute)(implicit
                                            identity: String,
                                            clientAddress: String
  ): messages.Message = {
    println(
      s"Module $identity: Received Execute msg from server statement: ${msg.statement}"
    )
    println(s"Module $identity: Executing command ${msg.statement} for 1sec")
    Thread.sleep(1000)
    println(s"Module $identity: Successfully executed command ${msg.statement}")
    println(s"Module $identity: Sending reply on Execute msg")
    new messages.NULL()
  }

  def reactOnSetParam(msg: messages.SetParam)(implicit
                                              identity: String,
                                              clientAddress: String
  ): messages.ParamWasSet = {
    println(
      s"Module $identity :Received SetParam msg from server $clientAddress: " +
        s"must set parameter ${msg.name} "
    )
    engineParams.put(
      msg.name,
      msg.msg
    )
    println(s"Module $identity: Sending reply on SetParam  ${msg.name} msg")
    new messages.ParamWasSet()
  }

  def reactOnGetParam(msg: messages.GetParam)(implicit
                                              identity: String,
                                              clientAddress: String
  ): messages.Message = {
    println(s"Module $identity: Received GetParam ${msg.name} msg from server")
    println(s"Module $identity:  Sending reply on GetParam ${msg.name} msg")
    engineParams.get(msg.name).get
  }

  def reactOnIsParam(msg: messages.IsParam)(implicit
                                            identity: String,
                                            clientAddress: String
  ): messages.Bool = {
    println(s"Module $identity: Received GetParam ${msg.name} msg from server")
    println(s"Module $identity:  Sending reply on GetParam ${msg.name} msg")
    new messages.Bool(engineParams.keys.toSeq.contains(msg.name))
  }

  def reactOnExecuteFunction(msg: messages.ExecuteFunction)(implicit
                                                            identity: String,
                                                            clientAddress: String
  ): messages.Message = {
    println(s"Started executing function ${msg.name}")
    println(s"Started executing function ${msg.name}")
    println(s"Executing function ${msg.name}")
    new messages.NULL()
  }

  def reactOnGetDefinedFunctions()(implicit
                                   identity: String,
                                   clientAddress: String
  ): messages.DefinedFunctions = {
    println(s"Module $identity: Received request to get defined functions from server")
    new messages.DefinedFunctions(Seq().toArray)
  }

}
