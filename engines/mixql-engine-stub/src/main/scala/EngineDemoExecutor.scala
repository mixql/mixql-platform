package org.mixql.engine.demo

import org.mixql.protobuf.ProtoBufConverter
import org.mixql.protobuf.messages.clientMsgs

import scala.collection.mutable
import org.mixql.engine.core.{IModuleExecutor, BrakeException}
import org.mixql.engine.core.Module.{sendMsgToServerBroker, *}
import org.zeromq.ZMQ

object EngineDemoExecutor extends IModuleExecutor {
  val engineParams: mutable.Map[String, scalapb.GeneratedMessage] =
    mutable.Map()

  def reactOnMessage(msg: Array[Byte])(implicit
    server: ZMQ.Socket,
    identity: String,
    clientAddress: Array[Byte]
  ): Unit = {
    val clientAddressStr = String(clientAddress)
    ProtoBufConverter.unpackAnyMsg(msg) match {
      case clientMsgs.Execute(statement, _) =>
        println(
          s"Module $identity: Received Execute msg from server statement: ${statement}"
        )
        println(s"Module $identity: Executing command ${statement} for 1sec")
        Thread.sleep(1000)
        println(s"Module $identity: Successfully executed command ${statement}")
        println(s"Module $identity: Sending reply on Execute msg")
        sendMsgToServerBroker(clientAddress, clientMsgs.NULL())
      case clientMsgs.SetParam(name, value, _) =>
        try {
          println(
            s"Module $identity :Received SetParam msg from server $clientAddressStr: " +
              s"must set parameter $name "
          )
          engineParams.put(
            name,
            ProtoBufConverter.unpackAnyMsg(value.get.toByteArray)
          )
          println(s"Module $identity: Sending reply on SetParam  $name msg")
          sendMsgToServerBroker(clientAddress, clientMsgs.ParamWasSet())
        } catch {
          case e: Throwable =>
            sendMsgToServerBroker(
              clientAddress,
              clientMsgs.Error(
                s"Module $identity to ${clientAddressStr}: error while executing Set Param command: " +
                  e.getMessage
              )
            )
        }
      case clientMsgs.GetParam(name, _) =>
        println(s"Module $identity: Received GetParam $name msg from server")
        println(s"Module $identity:  Sending reply on GetParam $name msg")
        try {
          sendMsgToServerBroker(clientAddress, engineParams.get(name).get)
        } catch {
          case e: Throwable =>
            sendMsgToServerBroker(
              clientAddress,
              clientMsgs.Error(
                s"Module $identity to ${clientAddressStr}: error while executing get Param command: " +
                  e.getMessage
              )
            )
        }
      case clientMsgs.IsParam(name, _) =>
        println(s"Module $identity: Received GetParam $name msg from server")
        println(s"Module $identity:  Sending reply on GetParam $name msg")
        sendMsgToServerBroker(
          clientAddress,
          clientMsgs.Bool(engineParams.keys.toSeq.contains(name))
        )
      case clientMsgs.ShutDown(_) =>
        println(s"Module $identity: Started shutdown")
        throw new BrakeException()
    }
  }
}
