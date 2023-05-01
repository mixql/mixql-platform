package org.mixql.protobuf

import org.apache.logging.log4j.core.util
import org.mixql.protobuf.messages
import org.mixql.protobuf.messages.Message

import java.nio.charset.StandardCharsets
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.json.simple.{JSONArray, JSONObject, JSONValue}


import scala.collection.immutable.List


object ProtoBufConverter {
  def unpackAnyMsg(array: Array[Byte]): messages.Message = {
    unpackAnyMsg(new String(array, StandardCharsets.UTF_8))
  }

  private def parseStringsArray(jsonArrObject: JSONArray): Array[String] = {
    var list: List[String] = List();
    for (i <- 0 until jsonArrObject.size()) {
      list = list :+ jsonArrObject.get(i).asInstanceOf[String]
    }
    list.toArray
  }

  private def parseMessagesArray(jsonArrObject: JSONArray): Array[Message] = {
    var list: List[Message] = List();
    for (i <- 0 until jsonArrObject.size()) {
      list = list :+ _unpackAnyMsg(jsonArrObject.get(i).asInstanceOf[JSONObject])
    }
    list.toArray
  }

  private def _unpackAnyMsg(anyMsgJsonObject: JSONObject): messages.Message = {
    anyMsgJsonObject.get("type").asInstanceOf[String] match {
      case "org.mixql.protobuf.messages.EngineName" =>
        new messages.EngineName(
          anyMsgJsonObject.get("name").asInstanceOf[String]
        )
      case "org.mixql.protobuf.messages.ShutDown" =>
        new messages.ShutDown()
      case "org.mixql.protobuf.messages.Execute" =>
        new messages.Execute(
          {
            anyMsgJsonObject.get("statement").asInstanceOf[String]
          }
        )
      case "org.mixql.protobuf.messages.Param" =>
        new messages.Param(
          anyMsgJsonObject.get("name").asInstanceOf[String],
          _unpackAnyMsg(anyMsgJsonObject.get("msg").asInstanceOf[JSONObject])
        )
      case "org.mixql.protobuf.messages.Error" =>
        new messages.Error(
          "error while unpacking from json Error: " + anyMsgJsonObject.get("msg").asInstanceOf[String]
        )
      case "org.mixql.protobuf.messages.SetParam" =>
        new messages.SetParam(
          anyMsgJsonObject.get("name").asInstanceOf[String],
          _unpackAnyMsg(anyMsgJsonObject.get("msg").asInstanceOf[JSONObject])
        )
      case "org.mixql.protobuf.messages.GetParam" =>
        new messages.GetParam(
          anyMsgJsonObject.get("name").asInstanceOf[String]
        )
      case "org.mixql.protobuf.messages.IsParam" =>
        new messages.IsParam(
          anyMsgJsonObject.get("name").asInstanceOf[String]
        )
      case "org.mixql.protobuf.messages.ParamWasSet" =>
        new messages.ParamWasSet()
      case "org.mixql.protobuf.messages.ExecuteFunction" =>
        new messages.ExecuteFunction(
          anyMsgJsonObject.get("name").asInstanceOf[String],
          parseMessagesArray(anyMsgJsonObject
            .get("params").asInstanceOf[JSONArray]
          )
        )
      case "org.mixql.protobuf.messages.GetDefinedFunctions" =>
        new messages.GetDefinedFunctions()
      case "org.mixql.protobuf.messages.DefinedFunctions" =>
        new messages.DefinedFunctions(
          {
            parseStringsArray(anyMsgJsonObject.get("arr").asInstanceOf[JSONArray])
          }
        )
      case "org.mixql.protobuf.messages.NULL" => new messages.NULL()
      case "org.mixql.protobuf.messages.Bool" =>
        new messages.Bool(
          anyMsgJsonObject.get("value").asInstanceOf[String].toBoolean
        )
      case "org.mixql.protobuf.messages.gInt" =>
        new messages.gInt(
          anyMsgJsonObject.get("value").asInstanceOf[String].toInt
        )
      case "org.mixql.protobuf.messages.gDouble" =>
        new messages.gDouble(
          anyMsgJsonObject.get("value").asInstanceOf[String].toDouble
        )
      case "org.mixql.protobuf.messages.gString" =>
        new messages.gString(
          anyMsgJsonObject.get("value").asInstanceOf[String],
          anyMsgJsonObject.get("quote").asInstanceOf[String]
        )
      case "org.mixql.protobuf.messages.gArray" =>
        new messages.gArray(
          parseMessagesArray(anyMsgJsonObject.get("arr").asInstanceOf[JSONArray])
        )
    }
  }

  def unpackAnyMsg(json: String): messages.Message = {

    try {
      import org.json.simple.JSONObject
      import org.json.simple.JSONValue
      val anyMsgJsonObject = JSONValue.parseWithException(json).asInstanceOf[JSONObject]
      _unpackAnyMsg(anyMsgJsonObject)
    }
    catch {
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

  private def _toJsonObject(msg: messages.Message): Try[JSONObject] = {
    Try {
      msg match {
        case m: messages.EngineName => JsonUtils.buildEngineName(m.`type`(), m.name)
        case m: messages.ShutDown =>JsonUtils.buildShutDown(m.`type`())
        case m: messages.Execute => JsonUtils.buildExecute(m.`type`(), m.statement)
        case m: messages.Param => JsonUtils.buildParam(m.`type`(), m.name, _toJsonObject(m.msg).get)
        case m: messages.Error => JsonUtils.buildError(m.`type`(), m.msg)
        case m: messages.SetParam => JsonUtils.buildSetParam(m.`type`(), m.name, _toJsonObject(m.msg).get)
        case m: messages.GetParam => JsonUtils.buildGetParam(m.`type`(), m.name)
        case m: messages.IsParam => JsonUtils.buildIsParam(m.`type`(), m.name)
        case m: messages.ParamWasSet => JsonUtils.buildParamWasSet(m.`type`())
        case m: messages.ExecuteFunction => JsonUtils.buildExecuteFunction(m.`type`(), m.name, m.params.map(
          m => _toJsonObject(m).get
        ))
        case m: messages.GetDefinedFunctions => JsonUtils.buildGetDefinedFunctions(m.`type`())
        case m: messages.DefinedFunctions => JsonUtils.buildDefinedFunction(m.`type`(), m.arr)
        case m: messages.NULL => JsonUtils.buildNULL(m.`type`())
        case m: messages.Bool => JsonUtils.buildBool(m.`type`(), m.value)
        case m: messages.gInt => JsonUtils.buildInt(m.`type`(), m.value)
        case m: messages.gDouble => JsonUtils.buildDouble(m.`type`(), m.value)
        case m: messages.gString => JsonUtils.buildGString(m.`type`(), m.value, m.quote)
        case m: messages.gArray => JsonUtils.buildGArray(m.`type`(), m.arr.map(
          m => _toJsonObject(m).get)
        )
      }
    }
  }

  def toJson(msg: messages.Message): Try[String] = {
    Try {
      _toJsonObject(msg) match {
        case Success(value) => value.toJSONString()
        case Failure(ex) => throw ex
      }
    }
  }
}
