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
        case "org.mixql.protobuf.messages.clientMsgs.ParamWasSet" =>
          anyMsg.getMsg.unpack[clientMsgs.ParamWasSet]
        case "org.mixql.protobuf.messages.clientMsgs.ExecuteFunction" =>
          anyMsg.getMsg.unpack[clientMsgs.ExecuteFunction]
        case "org.mixql.protobuf.messages.clientMsgs.GetDefinedFunctions" =>
          anyMsg.getMsg.unpack[clientMsgs.GetDefinedFunctions]
        case "org.mixql.protobuf.messages.clientMsgs.DefinedFunctions" =>
          anyMsg.getMsg.unpack[clientMsgs.DefinedFunctions]
        case typeMsg: String => GtypeConverter.toGeneratedMsg(GtypeConverter.protobufAnyToGtype(anyMsg.getMsg))
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
  def toArray(msg: scalapb.GeneratedMessage): Try[scala.Array[Byte]] = {
    Try {
      AnyMsg(
        msg.getClass.getName,
        Some(com.google.protobuf.any.Any.pack(msg))
      ).toByteArray
    }
  }
}
