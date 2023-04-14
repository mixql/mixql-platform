package org.mixql.engine.demo.scala.two.thirteen

import org.mixql.protobuf.{GtypeConverter, ProtoBufConverter}
import org.mixql.protobuf.generated.messages

import scala.collection.{Seq, mutable}
import org.mixql.engine.core.{BrakeException, IModuleExecutor}
import org.mixql.engine.core.Module.{sendMsgToServerBroker, _}
import org.zeromq.ZMQ

object EngineDemoExecutor extends IModuleExecutor {
  val engineParams: mutable.Map[String, com.google.protobuf.GeneratedMessageV3] =
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
          s"Module $identity: Received Execute msg from server statement: ${msg.getStatement}"
        )
        println(s"Module $identity: Executing command ${msg.getStatement} for 1sec")
        Thread.sleep(1000)
        println(s"Module $identity: Successfully executed command ${msg.getStatement}")
        println(s"Module $identity: Sending reply on Execute msg")
        sendMsgToServerBroker(clientAddress, messages.NULL.getDefaultInstance)
      case msg: messages.SetParam =>
        try {
          println(
            s"Module $identity :Received SetParam msg from server $clientAddressStr: " +
              s"must set parameter ${msg.getName} "
          )
          engineParams.put(
            msg.getName,
            GtypeConverter.toGeneratedMsg(GtypeConverter.protobufAnyToGtype(msg.getValue))
          )
          println(s"Module $identity: Sending reply on SetParam  ${msg.getName} msg")
          sendMsgToServerBroker(clientAddress, messages.ParamWasSet.getDefaultInstance)
        } catch {
          case e: Throwable =>
            sendMsgToServerBroker(
              clientAddress,
              messages.Error.newBuilder().setMsg(
                s"Module $identity to ${clientAddressStr}: error while executing Set Param command: " +
                  e.getMessage
              ).build()
            )
        }
      case msg: messages.GetParam =>
        println(s"Module $identity: Received GetParam ${msg.getName} msg from server")
        println(s"Module $identity:  Sending reply on GetParam ${msg.getName} msg")
        try {
          sendMsgToServerBroker(clientAddress, engineParams(msg.getName))
        } catch {
          case e: Throwable =>
            sendMsgToServerBroker(
              clientAddress,
              messages.Error.newBuilder().setMsg(
                s"Module $identity to ${clientAddressStr}: error while executing get Param command: " +
                  e.getMessage
              ).build()
            )
        }
      case msg: messages.IsParam =>
        println(s"Module $identity: Received GetParam ${msg.getName} msg from server")
        println(s"Module $identity:  Sending reply on GetParam ${msg.getName} msg")
        sendMsgToServerBroker(
          clientAddress,
          messages.Bool.newBuilder()
            .setValue(engineParams.keys.toSeq.contains(msg.getName))
            .build()
        )
      case _: messages.ShutDown =>
        println(s"Module $identity: Started shutdown")
        throw new BrakeException()
      case msg: messages.ExecuteFunction =>
        try {
          println(s"Started executing function ${msg.getName}")
          import org.mixql.core.context.gtype
          import org.mixql.protobuf.GtypeConverter
          val gParams: Seq[gtype.Type] = if (msg.hasParams) {
            val p = GtypeConverter.toGtype(msg.getParams).asInstanceOf[gtype.array].getArr
            println(s"[Module-$identity] Params provided for function ${msg.getName}: " + p)
            p
          } else Seq()
          println(s"Executing function ${msg.getName} with params " + gParams.toString)
          sendMsgToServerBroker(
            clientAddress,
            messages.NULL.getDefaultInstance
          )
        }
        catch {
          case e: Throwable =>
            sendMsgToServerBroker(
              clientAddress,
              messages.Error.newBuilder().setMsg(
                s"Module $identity to ${clientAddressStr}: error while executing function ${msg.getName}: " +
                  e.getMessage
              ).build()
            )
        }
      case msg: messages.GetDefinedFunctions =>
        println(s"Module $identity: Received request to get defined functions from server")
        sendMsgToServerBroker(
          clientAddress,
          messages.DefinedFunctions.getDefaultInstance
        )
    }
  }
}
