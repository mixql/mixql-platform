package org.mixql.protobuf

import org.mixql.protobuf.messages
import org.mixql.protobuf.messages.AnyMsg
import io.circe.syntax._
import io.circe.parser.decode
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe._, io.circe.parser._
import cats.syntax.either._

import java.nio.charset.StandardCharsets
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object ProtoBufConverter {
  def unpackAnyMsg(array: Array[Byte]): messages.Message = {
    unpackAnyMsg(new String(array, StandardCharsets.UTF_8))
  }

  def unpackAnyMsg(json: String): messages.Message = {
    try {
      val anyMsg: messages.AnyMsg = decode[messages.AnyMsg](json).toTry match {
        case Failure(exception) => return messages.Error("error while unpacking array of bytes to anyMag: " +
          exception.getMessage)
        case Success(v) => v
      }
      {
        anyMsg.`type` match {
          case "org.mixql.protobuf.messages.messages.EngineName" =>
            decode[messages.EngineName](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.ShutDown" =>
            decode[messages.ShutDown](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.Execute" =>
            decode[messages.Execute](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.Param" =>
            decode[messages.Param](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.Error" =>
            decode[messages.Error](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.SetParam" =>
            decode[messages.SetParam](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.GetParam" =>
            decode[messages.GetParam](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.IsParam" =>
            decode[messages.IsParam](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.ParamWasSet" =>
            decode[messages.ParamWasSet](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.ExecuteFunction" =>
            decode[messages.ExecuteFunction](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.GetDefinedFunctions" =>
            decode[messages.GetDefinedFunctions](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.DefinedFunctions" =>
            decode[messages.DefinedFunctions](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.NULL" => decode[messages.NULL](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.Bool" => decode[messages.Bool](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.int" => decode[messages.int](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.double" => decode[messages.double](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.gString" => decode[messages.gString](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.gArray" => decode[messages.gArray](anyMsg.json).toTry
          case "org.mixql.protobuf.messages.messages.Error" => decode[messages.Error](anyMsg.json).toTry
        }
      } match {
        case Failure(ex) => messages.Error("error while unpacking jsonMsg of anyMsg: " +
          ex.getMessage)
        case Success(v) => v.asInstanceOf[messages.Message]
      }
    } catch {
      case e: Throwable =>
        messages.Error(s"Protobuf anymsg converter: Error: " + e.getMessage)
    }
  }

  def toArray(msg: messages.Message): Try[Array[Byte]] = {
    Try {
      toJson(msg) match {
        case Success(v) => v.getBytes(StandardCharsets.UTF_8)
        case Failure(exception) => throw exception
      }
    }
  }

  def toJson(msg: messages.Message): Try[String] = {
    Try {
      AnyMsg(
        msg.getClass.getName,
        msg match {
          case m: messages.EngineName => m.asJson.noSpaces
          case m: messages.ShutDown => m.asJson.noSpaces
          case m: messages.Execute => m.asJson.noSpaces
          case m: messages.Param => m.asJson.noSpaces
          case m: messages.Error => m.asJson.noSpaces
          case m: messages.SetParam => m.asJson.noSpaces
          case m: messages.GetParam => m.asJson.noSpaces
          case m: messages.IsParam => m.asJson.noSpaces
          case m: messages.ParamWasSet => m.asJson.noSpaces
          case m: messages.ExecuteFunction => m.asJson.noSpaces
          case m: messages.GetDefinedFunctions => m.asJson.noSpaces
          case m: messages.DefinedFunctions => m.asJson.noSpaces
          case m: messages.NULL => m.asJson.noSpaces
          case m: messages.Bool => m.asJson.noSpaces
          case m: messages.int => m.asJson.noSpaces
          case m: messages.double => m.asJson.noSpaces
          case m: messages.gString => m.asJson.noSpaces
          case m: messages.gArray => m.asJson.noSpaces
          case m: messages.Error => m.asJson.noSpaces
        }
      ).asJson.noSpaces
    }
  }
}
