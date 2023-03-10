package org.mixql.engine.sqlite

import org.mixql.protobuf.{GtypeConverter, ProtoBufConverter}
import org.mixql.protobuf.messages.clientMsgs

import scala.collection.mutable
import org.mixql.engine.core.{BrakeException, IModuleExecutor}
import org.mixql.engine.core.Module.{sendMsgToServerBroker, *}
import org.zeromq.ZMQ
import org.mixql.core.function.FunctionInvoker

object EngineSqlightExecutor
    extends IModuleExecutor
    with java.lang.AutoCloseable:
  val engineParams: mutable.Map[String, scalapb.GeneratedMessage] =
    mutable.Map()

  var context: SQLightJDBC = null

  val functions: Map[String, Any] = Map(
    "sqlite_simple_proc" -> SqliteSimpleProc.simple_func,
    "sqlite_simple_proc_params" -> SqliteSimpleProc.simple_func_params,
    "sqlite_simple_proc_context_params" -> SqliteSimpleProc.simple_func_context_params,
  )

  def reactOnMessage(msg: Array[Byte])(implicit
    server: ZMQ.Socket,
    identity: String,
    clientAddress: Array[Byte]
  ): Unit = {
    if context == null then context = SQLightJDBC(identity)
    val clientAddressStr = String(clientAddress)
    ProtoBufConverter.unpackAnyMsg(msg) match {
      case clientMsgs.Execute(statement, _) =>
        println(
          s"[Module-$identity]: Received Execute msg from server statement: ${statement}"
        )
        println(s"[Module-$identity]: Executing command ${statement}")
//        Thread.sleep(1000)
        val res = context.execute(statement)
        println(s"[Module-$identity]: Successfully executed command ${statement}")
        println(
          s"[Module-$identity]: Sending reply on Execute msg " + res.getClass.getName
        )
        sendMsgToServerBroker(clientAddress, res)
      case clientMsgs.SetParam(name, value, _) =>
        try {
          println(
            s"[Module-$identity] :Received SetParam msg from server $clientAddressStr: " +
              s"must set parameter $name "
          )
          engineParams.put(
            name,
            GtypeConverter.toGeneratedMsg(GtypeConverter.protobufAnyToGtype(value.get))
          )
          println(s"[Module-$identity]: Sending reply on SetParam  $name msg")
          sendMsgToServerBroker(clientAddress, clientMsgs.ParamWasSet())
        } catch {
          case e: Throwable =>
            sendMsgToServerBroker(
              clientAddress,
              clientMsgs.Error(
                s"[Module-$identity] to ${clientAddressStr}: error while executing Set Param command: " +
                  e.getMessage
              )
            )
        }
      case clientMsgs.GetParam(name, _) =>
        println(s"[Module-$identity]: Received GetParam $name msg from server")
        println(s"[Module-$identity]:  Sending reply on GetParam $name msg")
        try {
          sendMsgToServerBroker(clientAddress, engineParams.get(name).get)
        } catch {
          case e: Throwable =>
            sendMsgToServerBroker(
              clientAddress,
              clientMsgs.Error(
                s"[Module-$identity] to ${clientAddressStr}: error while executing get Param command: " +
                  e.getMessage
              )
            )
        }
      case clientMsgs.IsParam(name, _) =>
        println(s"[Module-$identity]: Received GetParam $name msg from server")
        println(s"[Module-$identity]:  Sending reply on GetParam $name msg")
        sendMsgToServerBroker(
          clientAddress,
          clientMsgs.Bool(engineParams.keys.toSeq.contains(name))
        )
      case clientMsgs.ShutDown(_) =>
        println(s"[Module-$identity]: Started shutdown")
        throw new BrakeException()
      case clientMsgs.ExecuteFunction(name, params, _) =>
        try
          println(s"[Module-$identity] Started executing function $name")
          import org.mixql.core.context.gtype
          import org.mixql.protobuf.GtypeConverter
          val gParams: Seq[gtype.Type] = params match
            case Some(value) =>
              val p = GtypeConverter.toGtype(value).asInstanceOf[gtype.array].arr
              println(s"[Module-$identity] Params provided for function $name: " + p)
              p
            case None => Seq()
          println(s"[Module-$identity] Executing function $name with params " + gParams.toString)
          val res = FunctionInvoker.invoke(functions, name, context, gParams.map(p => gtype.unpack(p)))
          println(s"[Module-$identity] : Successfully executed function $name with params " + params.toString +
            s"\nResult: $res")
          val gres = gtype.pack(res)
          sendMsgToServerBroker(
            clientAddress,
            GtypeConverter.toGeneratedMsg(gres)
          )
        catch
          case e: Throwable =>
            sendMsgToServerBroker(
              clientAddress,
              clientMsgs.Error(
                s"[Module-$identity] to ${clientAddressStr}: error while executing function $name: " +
                  e.getMessage
              )
            )
      case clientMsgs.GetDefinedFunctions(_) =>
        println(s"[Module-$identity]: Received request to get defined functions from server")
        sendMsgToServerBroker(
          clientAddress,
          clientMsgs.DefinedFunctions(
            List("sqlite_simple_proc", "sqlite_simple_proc_params", "sqlite_simple_proc_context_params"))
        )
    }


  }

  override def close(): Unit =
    if context != null then context.close()
