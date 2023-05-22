package org.mixql.engine.sqlite

import org.mixql.protobuf.messages

import scala.collection.mutable
import org.mixql.engine.core.{BrakeException, IModuleExecutor}

object EngineSqlightExecutor
  extends IModuleExecutor
    with java.lang.AutoCloseable :
  val engineParams: mutable.Map[String, messages.Message] =
    mutable.Map()

  var context: SQLightJDBC = null

  def functions: Map[String, Any] = Map(
    "sqlite_simple_proc" -> SqliteSimpleProc.simple_func,
    "sqlite_simple_proc_params" -> SqliteSimpleProc.simple_func_params,
    "sqlite_simple_proc_context_params" -> SqliteSimpleProc.simple_func_context_params,
  )

  def reactOnExecute(msg: messages.Execute, identity: String, clientAddress: String): messages.Message = {
    if context == null then context = SQLightJDBC(identity, engineParams)
    println(
      s"[Module-$identity]: Received Execute msg from server statement: ${msg.statement}"
    )
    println(s"[Module-$identity]: Executing command ${msg.statement}")
    //        Thread.sleep(1000)
    val res = context.execute(msg.statement)
    println(s"[Module-$identity]: Successfully executed command ${msg.statement}")
    println(
      s"[Module-$identity]: Sending reply on Execute msg " + res.getClass.getName
    )
    res
  }

  def reactOnSetParam(msg: messages.SetParam, identity: String, clientAddress: String): messages.ParamWasSet = {
    println(
      s"[Module-$identity] :Received SetParam msg from server $clientAddress: " +
        s"must set parameter ${msg.name} with value ${msg.msg}"
    )
    engineParams.put(
      msg.name,
      msg.msg
    )
    println(s"[Module-$identity]: Sending reply on SetParam  ${msg.name} msg")
    messages.ParamWasSet()
  }

  def reactOnGetParam(msg: messages.GetParam, identity: String, clientAddress: String): messages.Message = {
    println(s"[Module-$identity]: Received GetParam ${msg.name} msg from server")
    println(s"[Module-$identity]:  Sending reply on GetParam ${msg.name} msg")
    engineParams(msg.name)
  }

  def reactOnIsParam(msg: messages.IsParam, identity: String, clientAddress: String): messages.Bool = {
    println(s"[Module-$identity]: Received GetParam ${msg.name} msg from server")
    println(s"[Module-$identity]:  Sending reply on GetParam ${msg.name} msg")
    messages.Bool(engineParams.keys.toSeq.contains(msg.name))
  }

  def reactOnExecuteFunction(msg: messages.ExecuteFunction, identity: String,
                             clientAddress: String): messages.Message = {
    if context == null then context = SQLightJDBC(identity, engineParams)
    println(s"[Module-$identity] Started executing function ${msg.name}")
    println(s"[Module-$identity] Executing function ${msg.name} with params " +
      msg.params.mkString("[", ",", "]"))
    val res = org.mixql.engine.core.FunctionInvoker.invoke(functions, msg.name, context, msg.params.toList)
    println(s"[Module-$identity] : Successfully executed function ${msg.name} ")
    res
  }

  def reactOnGetDefinedFunctions(identity: String, clientAddress: String): messages.DefinedFunctions = {
    import collection.JavaConverters._
    println(s"[Module-$identity]: Received request to get defined functions from server")
    messages.DefinedFunctions(functions.keys.toArray)
  }

  def reactOnShutDown(identity: String, clientAddress: String): Unit = {}

  override def close(): Unit =
    if context != null then context.close()
