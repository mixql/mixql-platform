package org.mixql.protobuf

import org.mixql.protobuf.messages
import org.mixql.protobuf.messages.{AnyMsg, Message}

import java.nio.charset.StandardCharsets
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.JSONValue

import scala.collection.immutable.List


object ProtoBufConverter {
  def unpackAnyMsg(array: Array[Byte]): messages.Message = {
    unpackAnyMsg(new String(array, StandardCharsets.UTF_8))
  }

  def unpackAnyMsg(json: String): messages.Message = {

    def parseStringsArray(jsonArrObject: JSONArray): Array[String] = {
      var list: List[String] = List();
      for (i <- 0 until jsonArrObject.size()) {
        list = list :+ jsonArrObject.get(i).asInstanceOf[String]
      }
      list.toArray
    }

    try {
      val anyMsg: messages.AnyMsg = {
        import org.json.simple.JSONObject
        import org.json.simple.JSONValue
        val anyMsgJsonObject = JSONValue.parseWithException(json).asInstanceOf[JSONObject]
        new messages.AnyMsg(anyMsgJsonObject.get("type").asInstanceOf[String],
          anyMsgJsonObject.get("json").asInstanceOf[String])
      }
      anyMsg.`type` match {
        case "org.mixql.protobuf.messages.EngineName" =>
          new messages.EngineName(
            {
              val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
              jsonObject.get("name").asInstanceOf[String]
            }
          )
        case "org.mixql.protobuf.messages.ShutDown" =>
          new messages.ShutDown()
        case "org.mixql.protobuf.messages.Execute" =>
          new messages.Execute(
            {
              val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
              jsonObject.get("statement").asInstanceOf[String]
            }
          )
        case "org.mixql.protobuf.messages.Param" =>
          val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
          new messages.Param(
            jsonObject.get("name").asInstanceOf[String],
            jsonObject.get("json").asInstanceOf[String]
          )
        case "org.mixql.protobuf.messages.Error" =>
          val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
          new messages.Error(
            "error while unpacking from json Error: " + jsonObject.get("msg").asInstanceOf[String]
          )
        case "org.mixql.protobuf.messages.SetParam" =>
          val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
          new messages.SetParam(
            jsonObject.get("name").asInstanceOf[String],
            jsonObject.get("json").asInstanceOf[String]
          )
        case "org.mixql.protobuf.messages.GetParam" =>
          val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
          new messages.GetParam(
            jsonObject.get("name").asInstanceOf[String]
          )
        case "org.mixql.protobuf.messages.IsParam" =>
          val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
          new messages.IsParam(
            jsonObject.get("name").asInstanceOf[String]
          )
        case "org.mixql.protobuf.messages.ParamWasSet" =>
          new messages.ParamWasSet()
        case "org.mixql.protobuf.messages.ExecuteFunction" =>
          val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
          new messages.ExecuteFunction(
            jsonObject.get("name").asInstanceOf[String],
            new messages.gArray(
              parseStringsArray(jsonObject
                .get("params").asInstanceOf[JSONObject]
                .get("arr").asInstanceOf[JSONArray]
              )
            )
          )
        case "org.mixql.protobuf.messages.GetDefinedFunctions" =>
          new messages.GetDefinedFunctions()
        case "org.mixql.protobuf.messages.DefinedFunctions" =>
          new messages.DefinedFunctions(
            {
              val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
              parseStringsArray(jsonObject.get("arr").asInstanceOf[JSONArray])
            }
          )
        case "org.mixql.protobuf.messages.NULL" => new messages.NULL()
        case "org.mixql.protobuf.messages.Bool" =>
          val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
          new messages.Bool(
            jsonObject.get("value").asInstanceOf[String].toBoolean
          )
        case "org.mixql.protobuf.messages.gInt" =>
          val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
          new messages.gInt(
            jsonObject.get("value").asInstanceOf[String].toInt
          )
        case "org.mixql.protobuf.messages.gDouble" =>
          val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
          new messages.gDouble(
            jsonObject.get("value").asInstanceOf[String].toDouble
          )
        case "org.mixql.protobuf.messages.gString" =>
          val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
          new messages.gString(
            jsonObject.get("value").asInstanceOf[String],
            jsonObject.get("quote").asInstanceOf[String]
          )
        case "org.mixql.protobuf.messages.gArray" =>
          val jsonObject = JSONValue.parseWithException(anyMsg.json).asInstanceOf[JSONObject]
          new messages.gArray(
            parseStringsArray(jsonObject.get("arr").asInstanceOf[JSONArray])
          )
      }
    } catch {
      case e: Throwable =>
        new messages.Error(s"Protobuf anymsg converter: Error: " + e.getMessage)
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
    import org.mixql.protobuf.utils.JsonUtils
    Try {
      JsonUtils.buildAnyMsg(
        msg.getClass.getName,
        msg match {
          case m: messages.EngineName => JsonUtils.buildEngineName(m.name)
          case _: messages.ShutDown => "{}"
          case m: messages.Execute => JsonUtils.buildExecute(m.statement)
          case m: messages.Param => JsonUtils.buildParam(m.name, m.json)
          case m: messages.Error => JsonUtils.buildError(m.msg)
          case m: messages.SetParam => JsonUtils.buildSetParam(m.name, m.json)
          case m: messages.GetParam => JsonUtils.buildGetParam(m.name)
          case m: messages.IsParam => JsonUtils.buildIsParam(m.name)
          case _: messages.ParamWasSet => "{}"
          case m: messages.ExecuteFunction => JsonUtils.buildExecuteFunction(m.name, m.params.arr)
          case _: messages.GetDefinedFunctions => "{}"
          case m: messages.DefinedFunctions => JsonUtils.buildDefinedFunction(m.arr)
          case _: messages.NULL => "{}"
          case m: messages.Bool => JsonUtils.buildBool(m.value)
          case m: messages.gInt => JsonUtils.buildInt(m.value)
          case m: messages.gDouble => JsonUtils.buildDouble(m.value)
          case m: messages.gString => JsonUtils.buildGString(m.value, m.quote)
          case m: messages.gArray => JsonUtils.buildGArray(m.arr)
        }
      )
    }
  }
}
