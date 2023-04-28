package org.mixql.protobuf

import org.mixql.protobuf.messages
import org.mixql.protobuf.messages.{AnyMsg, Message}
import io.circe.syntax._
import io.circe.parser.decode
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe._
import io.circe.parser._
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
      anyMsg.`type` match {
        case "org.mixql.protobuf.messages.EngineName" =>
          decode[messages.EngineName](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json EngineName: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.ShutDown" =>
          decode[messages.ShutDown](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json ShutDown: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.Execute" =>
          decode[messages.Execute](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json Execute: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.Param" =>
          decode[messages.Param](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json Param: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.Error" =>
          decode[messages.Error](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json Error: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.SetParam" =>
          decode[messages.SetParam](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json SetParam: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.GetParam" =>
          decode[messages.GetParam](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json GetParam: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.IsParam" =>
          decode[messages.IsParam](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json IsParam: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.ParamWasSet" =>
          decode[messages.ParamWasSet](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json ParamWasSet: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.ExecuteFunction" =>
          decode[messages.ExecuteFunction](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json ExecuteFunction: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.GetDefinedFunctions" =>
          decode[messages.GetDefinedFunctions](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json GetDefinedFunctions: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.DefinedFunctions" =>
          decode[messages.DefinedFunctions](anyMsg.json).toTry match {
            case Failure(exception) => return messages.Error("error while unpacking from json DefinedFunctions: " +
              exception.getMessage)
            case Success(v) => v
          }
        case "org.mixql.protobuf.messages.NULL" => decode[messages.NULL](anyMsg.json).toTry match {
          case Failure(exception) => return messages.Error("error while unpacking from json NULL: " +
            exception.getMessage)
          case Success(v) => v
        }
        case "org.mixql.protobuf.messages.Bool" => decode[messages.Bool](anyMsg.json).toTry match {
          case Failure(exception) => return messages.Error("error while unpacking from json Bool: " +
            exception.getMessage)
          case Success(v) => v
        }
        case "org.mixql.protobuf.messages.int" => decode[messages.int](anyMsg.json).toTry match {
          case Failure(exception) => return messages.Error("error while unpacking from json int: " +
            exception.getMessage)
          case Success(v) => v
        }
        case "org.mixql.protobuf.messages.double" => decode[messages.double](anyMsg.json).toTry match {
          case Failure(exception) => return messages.Error("error while unpacking from json double: " +
            exception.getMessage)
          case Success(v) => v
        }
        case "org.mixql.protobuf.messages.gString" => decode[messages.gString](anyMsg.json).toTry match {
          case Failure(exception) => return messages.Error("error while unpacking from json gString: " +
            exception.getMessage)
          case Success(v) => v
        }
        case "org.mixql.protobuf.messages.gArray" => decode[messages.gArray](anyMsg.json).toTry match {
          case Failure(exception) => return messages.Error("error while unpacking from json gArray: " +
            exception.getMessage)
          case Success(v) => v
        }
        case "org.mixql.protobuf.messages.Error" => decode[messages.Error](anyMsg.json).toTry match {
          case Failure(exception) => return messages.Error("error while unpacking from json Error: " +
            exception.getMessage)
          case Success(v) => v
        }
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
