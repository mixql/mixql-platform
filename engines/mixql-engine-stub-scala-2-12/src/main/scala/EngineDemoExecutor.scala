package org.mixql.engine.demo.scala.two.twelf

import org.mixql.protobuf.{GtypeConverter, ProtoBufConverter}
import org.mixql.protobuf.messages

import scala.collection.mutable
import org.mixql.engine.core.{BrakeException, IModuleExecutor}
import org.mixql.engine.core.Module.{sendMsgToServerBroker, _}
import org.zeromq.ZMQ

object EngineDemoExecutor extends IModuleExecutor {
  val engineParams: mutable.Map[String, messages.Message] =
    mutable.Map()

  def reactOnMessage(msg: Array[Byte])(implicit
                                       server: ZMQ.Socket,
                                       identity: String,
                                       clientAddress: Array[Byte]
  ): Unit = {
    val clientAddressStr = new String(clientAddress)
    ProtoBufConverter.unpackAnyMsg(msg) match {
      case msg: messages.Execute =>
        println(
          s"Module $identity: Received Execute msg from server statement: ${msg.statement}"
        )
        println(s"Module $identity: Executing command ${msg.statement} for 1sec")
        Thread.sleep(1000)
        println(s"Module $identity: Successfully executed command ${msg.statement}")
        println(s"Module $identity: Sending reply on Execute msg")
        sendMsgToServerBroker(clientAddress, new messages.NULL())
      case msg: messages.SetParam =>
        try {
          println(
            s"Module $identity :Received SetParam msg from server $clientAddressStr: " +
              s"must set parameter ${msg.name} "
          )
          engineParams.put(
            msg.name,
            ProtoBufConverter.unpackAnyMsg(msg.json)
          )
          println(s"Module $identity: Sending reply on SetParam  ${msg.name} msg")
          sendMsgToServerBroker(clientAddress, new messages.ParamWasSet())
        } catch {
          case e: Throwable =>
            sendMsgToServerBroker(
              clientAddress,
              new messages.Error(
                s"Module $identity to ${clientAddressStr}: error while executing Set Param command: " +
                  e.getMessage
              )
            )
        }
      case msg: messages.GetParam =>
        println(s"Module $identity: Received GetParam ${msg.name} msg from server")
        println(s"Module $identity:  Sending reply on GetParam ${msg.name} msg")
        try {
          sendMsgToServerBroker(clientAddress, engineParams.get(msg.name).get)
        } catch {
          case e: Throwable =>
            sendMsgToServerBroker(
              clientAddress,
              new messages.Error(
                s"Module $identity to ${clientAddressStr}: error while executing get Param command: " +
                  e.getMessage
              )
            )
        }
      case msg: messages.IsParam =>
        println(s"Module $identity: Received GetParam ${msg.name} msg from server")
        println(s"Module $identity:  Sending reply on GetParam ${msg.name} msg")
        sendMsgToServerBroker(
          clientAddress,
          new messages.Bool(engineParams.keys.toSeq.contains(msg.name))
        )
      case _: messages.ShutDown =>
        println(s"Module $identity: Started shutdown")
        throw new BrakeException()
      case msg: messages.ExecuteFunction =>
        try {
          println(s"Started executing function ${msg.name}")
          import org.mixql.core.context.gtype
          import org.mixql.protobuf.GtypeConverter
          val gParams: Seq[gtype.Type] = if (msg.params.arr.nonEmpty) {
            val p = GtypeConverter.toGtype(msg.params).asInstanceOf[gtype.array].arr
            println(s"[Module-$identity] Params provided for function ${msg.name}: " + p)
            p
          } else Seq()
          println(s"Executing function ${msg.name} with params " + gParams.toString)
          sendMsgToServerBroker(
            clientAddress,
            new messages.NULL()
          )
        }
        catch {
          case e: Throwable =>
            sendMsgToServerBroker(
              clientAddress,
              new messages.Error(
                s"Module $identity to ${clientAddressStr}: error while executing function ${msg.name}: " +
                  e.getMessage
              )
            )
        }
      case _: messages.GetDefinedFunctions =>
        println(s"Module $identity: Received request to get defined functions from server")
        sendMsgToServerBroker(
          clientAddress,
          new messages.DefinedFunctions(Seq().toArray)
        )
    }
  }
}
