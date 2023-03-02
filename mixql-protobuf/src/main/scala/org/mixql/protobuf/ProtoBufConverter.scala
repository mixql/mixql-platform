package org.mixql.protobuf

import org.mixql.protobuf.messages.clientMsgs
import org.mixql.protobuf.messages.clientMsgs.AnyMsg

import scala.util.Try

object ProtoBufConverter {
  def unpackAnyMsg(array: Array[Byte]): scalapb.GeneratedMessage = {
    try {
      val anyMsg = AnyMsg.parseFrom(array)
      anyMsg.`type` match {
        case "org.mixql.protobuf.messages.clientMsgs.EngineName" =>
          anyMsg.getMsg.unpack[clientMsgs.EngineName]
        case "org.mixql.protobuf.messages.clientMsgs.ShutDown" =>
          anyMsg.getMsg.unpack[clientMsgs.ShutDown]
        case "org.mixql.protobuf.messages.clientMsgs.Execute" =>
          anyMsg.getMsg.unpack[clientMsgs.Execute]
        case "org.mixql.protobuf.messages.clientMsgs.Param" =>
          anyMsg.getMsg.unpack[clientMsgs.Param]
        case "org.mixql.protobuf.messages.clientMsgs.Error" =>
          anyMsg.getMsg.unpack[clientMsgs.Error]
        case "org.mixql.protobuf.messages.clientMsgs.SetParam" =>
          anyMsg.getMsg.unpack[clientMsgs.SetParam]
        case "org.mixql.protobuf.messages.clientMsgs.GetParam" =>
          anyMsg.getMsg.unpack[clientMsgs.GetParam]
        case "org.mixql.protobuf.messages.clientMsgs.IsParam" =>
          anyMsg.getMsg.unpack[clientMsgs.IsParam]
        case typeMsg: String => toType(typeMsg, anyMsg.getMsg)
        case _: scala.Any =>
          clientMsgs.Error(
            s"Protobuf any msg converter: Error: Got unknown type ${anyMsg.`type`} of message"
          )
      }
    } catch {
      case e: Throwable =>
        clientMsgs.Error(s"Protobuf anymsg converter: Error: " + e.getMessage)
    }
  }

  def toType(
    protobufType: String,
    anyMsg: com.google.protobuf.any.Any
  ): scalapb.GeneratedMessage = {
    try {
      protobufType match {
        case "org.mixql.protobuf.messages.clientMsgs.NULL" =>
          anyMsg.unpack[clientMsgs.NULL]
        case "org.mixql.protobuf.messages.clientMsgs.Bool" =>
          anyMsg.unpack[clientMsgs.Bool]
        case "org.mixql.protobuf.messages.clientMsgs.Int" =>
          anyMsg.unpack[clientMsgs.Int]
        case "org.mixql.protobuf.messages.clientMsgs.Double" =>
          anyMsg.unpack[clientMsgs.Double]
        case "org.mixql.protobuf.messages.clientMsgs.String" =>
          anyMsg.unpack[clientMsgs.String]
        case "org.mixql.protobuf.messages.clientMsgs.Array" =>
          anyMsg.unpack[clientMsgs.Array]
        case _: scala.Any =>
          clientMsgs.Error(
            s"Protobuf type converter: Error: Got unknown type ${protobufType}"
          )
      }
    } catch {
      case e: Throwable =>
        clientMsgs.Error(s"Protobuf type converter: Error: " + e.getMessage)
    }
  }

  def toArray(msg: scalapb.GeneratedMessage): Try[scala.Array[Byte]] = {
    Try {
      AnyMsg(
        msg.getClass.getName,
        Some(com.google.protobuf.any.Any.pack(msg))
      ).toByteArray
    }
  }
}
