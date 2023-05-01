package org.mixql.engine.core

import org.mixql.protobuf.messages

trait IModuleExecutor {

  def reactOnExecute(msg: messages.Execute)(implicit
                                            identity: String,
                                            clientAddress: String
  ): messages.Message

  def reactOnSetParam(msg: messages.SetParam)(implicit
                                              identity: String,
                                              clientAddress: String
  ): messages.ParamWasSet

  def reactOnGetParam(msg: messages.GetParam)(implicit
                                              identity: String,
                                              clientAddress: String
  ): messages.Message

  def reactOnIsParam(msg: messages.IsParam)(implicit
                                            identity: String,
                                            clientAddress: String
  ): messages.Bool

  def reactOnShutDown()(implicit
                        identity: String,
                        clientAddress: String
  ): Unit = {}

  def reactOnExecuteFunction(msg: messages.ExecuteFunction)(implicit
                                                            identity: String,
                                                            clientAddress: String
  ): messages.Message

  def reactOnGetDefinedFunctions()(implicit
                                   identity: String,
                                   clientAddress: String
  ): messages.DefinedFunctions
}
