package org.mixql.protobuf

import com.google.protobuf.GeneratedMessageV3

import org.mixql.protobuf.generated.messages
import org.mixql.protobuf.generated.messages.AnyMsg

import scala.util.Try

object ProtoBufConverter {
  def unpackAnyMsg(array: Array[Byte]): GeneratedMessageV3 = {
    try {
      val anyMsg = AnyMsg.parseFrom(array)
      anyMsg.getType match {
        case "org.mixql.protobuf.generated.messages.EngineName" =>
          anyMsg.getMsg.unpack(messages.EngineName.getDefaultInstance.getClass)
        case "org.mixql.protobuf.generated.messages.ShutDown" =>
          anyMsg.getMsg.unpack(messages.ShutDown.getDefaultInstance.getClass)
        case "org.mixql.protobuf.generated.messages.Execute" =>
          anyMsg.getMsg.unpack(messages.Execute.getDefaultInstance.getClass)
        case "org.mixql.protobuf.generated.messages.Param" =>
          anyMsg.getMsg.unpack(messages.Param.getDefaultInstance.getClass)
        case "org.mixql.protobuf.generated.messages.Error" =>
          anyMsg.getMsg.unpack(messages.Error.getDefaultInstance.getClass)
        case "org.mixql.protobuf.generated.messages.SetParam" =>
          anyMsg.getMsg.unpack(messages.SetParam.getDefaultInstance.getClass)
        case "org.mixql.protobuf.generated.messages.GetParam" =>
          anyMsg.getMsg.unpack(messages.GetParam.getDefaultInstance.getClass)
        case "org.mixql.protobuf.generated.messages.IsParam" =>
          anyMsg.getMsg.unpack(messages.IsParam.getDefaultInstance.getClass)
        case "org.mixql.protobuf.generated.messages.ParamWasSet" =>
          anyMsg.getMsg.unpack(messages.ParamWasSet.getDefaultInstance.getClass)
        case "org.mixql.protobuf.generated.messages.ExecuteFunction" =>
          anyMsg.getMsg.unpack(messages.ExecuteFunction.getDefaultInstance.getClass)
        case "org.mixql.protobuf.generated.messages.GetDefinedFunctions" =>
          anyMsg.getMsg.unpack(messages.GetDefinedFunctions.getDefaultInstance.getClass)
        case "org.mixql.protobuf.generated.messages.DefinedFunctions" =>
          anyMsg.getMsg.unpack(messages.DefinedFunctions.getDefaultInstance.getClass)
        case typeMsg: String => GtypeConverter
          .toGeneratedMsg(GtypeConverter.protobufAnyToGtype(anyMsg.getMsg))
        case _: scala.Any =>
          messages
            .Error
            .newBuilder()
            .setMsg(
              s"Protobuf any msg converter: Error: Got unknown type ${anyMsg.getType} of message"
            )
            .build()
      }
    } catch {
      case e: Throwable =>
        messages.Error
          .newBuilder()
          .setMsg(s"Protobuf anymsg converter: Error: " + e.getMessage)
          .build()
    }
  }

  def toArray(msg: GeneratedMessageV3): Try[scala.Array[Byte]] = {
    Try {
      AnyMsg
        .newBuilder()
        .setType(msg.getClass.getName)
        .setMsg(com.google.protobuf.Any.pack(msg))
        .build()
        .toByteArray
    }
  }
}
