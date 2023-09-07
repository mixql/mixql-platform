package org.mixql.engine.core

import org.mixql.core.context.gtype.Type
import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.remote.messages.client.{InvokedFunctionResult, PlatformVar, PlatformVarWasSet, PlatformVars, PlatformVarsNames, PlatformVarsWereSet}
import org.mixql.remote.messages.gtype
import org.mixql.remote.messages.module.worker.{GetPlatformVar, GetPlatformVars, GetPlatformVarsNames, InvokeFunction, SetPlatformVar, SetPlatformVars}
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter, messages}
import org.zeromq.ZMQ

import scala.collection.immutable.List
import scala.collection.mutable

class PlatformContext(workerSocket: ZMQ.Socket, workersId: String, clientAddress: Array[Byte])(implicit
  logger: ModuleLogger) {

  import logger._

  def setVar(key: String, value: Type): Unit = {
    this.synchronized {
      logInfo(s"[PlatformContext]: was asked to set variable $key in platform context")
      logInfo(s"[PlatformContext]: sending request SetPlatformVar to platform")
      workerSocket.send(
        RemoteMessageConverter
          .toArray(new SetPlatformVar(workersId, key, GtypeConverter.toGeneratedMsg(value), clientAddress))
      )

      RemoteMessageConverter.unpackAnyMsgFromArray(workerSocket.recv()) match {
        case _: PlatformVarWasSet => logInfo(s"[PlatformContext]: received answer PlatformVarWasSet from platform")
        case m: gtype.Error =>
          val errorMsg = "[PlatformContext]: Received error while settingVar: " + m.msg
          logError(errorMsg)
          throw new Exception(errorMsg)
        case m: messages.Message =>
          val errorMsg = "[PlatformContext]: Received unexpected remote message on setVar request: " + m.`type`()
          logError(errorMsg)
          throw new Exception(errorMsg)
      }
    }
  }

  def getVar(key: String): Type = {
    this.synchronized {
      logInfo(s"[PlatformContext]: was asked to get variable $key in platform context")
      logInfo(s"[PlatformContext]: sending request GetPlatformVar to platform")
      workerSocket.send(RemoteMessageConverter.toArray(new GetPlatformVar(workersId, key, clientAddress)))
      RemoteMessageConverter.unpackAnyMsgFromArray(workerSocket.recv()) match {
        case m: PlatformVar =>
          logInfo(s"[PlatformContext]: received answer PlatformVar for variable ${m.name} from platform")
          GtypeConverter.messageToGtype(m.msg)
        case m: gtype.Error =>
          val errorMsg = "[PlatformContext]: Received error while gettingVar: " + m.msg
          logError(errorMsg)
          throw new Exception(errorMsg)
        case m: messages.Message =>
          val errorMsg = "[PlatformContext]: Received unexpected remote message on getVar request: " + m.`type`()
          logError(errorMsg)
          throw new Exception(errorMsg)

      }
    }
  }

  def getVars(keys: List[String]): mutable.Map[String, Type] = {
    this.synchronized {
      logInfo(s"[PlatformContext]: was asked to get variables ${keys.mkString(",")} in platform context")
      logInfo(s"[PlatformContext]: sending request GetPlatformVars to platform")
      workerSocket.send(RemoteMessageConverter.toArray(new GetPlatformVars(workersId, keys.toArray, clientAddress)))

      RemoteMessageConverter.unpackAnyMsgFromArray(workerSocket.recv()) match {
        case m: PlatformVars =>
          logInfo(
            s"[PlatformContext]: received answer PlatformVars with variables ${m.vars.map(p => p.name).mkString(",")} from platform"
          )

          val vars: mutable.Map[String, Type] = mutable.Map()

          m.vars.foreach(param => vars.put(param.name, GtypeConverter.messageToGtype(param.msg)))
          vars
        case m: gtype.Error =>
          val errorMsg = "[PlatformContext]: Received error while gettingVars: " + m.msg
          logError(errorMsg)
          throw new Exception(errorMsg)
        case m: messages.Message =>
          val errorMsg =
            "[PlatformContext]: Received unexpected remote message on GetPlatformVars request: " +
              m.`type`()
          logError(errorMsg)
          throw new Exception(errorMsg)
      }
    }
  }

  def setVars(vars: mutable.Map[String, Type]): Unit = {
    setVars(collection.immutable.Map(vars.toSeq: _*))
  }

  def setVars(vars: Map[String, Type]): Unit = {
    this.synchronized {
      import collection.JavaConverters._
      logInfo(s"[PlatformContext]: was asked to set variables ${vars.keys.mkString(",")} in platform context")
      logInfo(s"[PlatformContext]: sending request SetPlatformVars to platform")
      workerSocket.send(
        RemoteMessageConverter.toArray(
          new SetPlatformVars(
            workersId,
            vars.map(tuple => tuple._1 -> GtypeConverter.toGeneratedMsg(tuple._2)).asJava,
            clientAddress
          )
        )
      )

      RemoteMessageConverter.unpackAnyMsgFromArray(workerSocket.recv()) match {
        case m: PlatformVarsWereSet =>
          logInfo(
            s"[PlatformContext]: received answer PlatformVarsWereSet with variables ${m.names.toArray().mkString(",")} from platform"
          )
        case m: gtype.Error =>
          val errorMsg = "[PlatformContext]: Received error while settingVars: " + m.msg
          logError(errorMsg)
          throw new Exception(errorMsg)
        case m: messages.Message =>
          val errorMsg =
            "[PlatformContext]: Received unexpected remote message on SetPlatformVars request: " +
              m.`type`()
          logError(errorMsg)
          throw new Exception(errorMsg)
      }
    }
  }

  def getVarsNames(): List[String] = {
    this.synchronized {
      logInfo(s"[PlatformContext]: was asked to get vars names in platform context")
      logInfo(s"[PlatformContext]: sending request GetPlatformVarsNames to platform")
      workerSocket.send(RemoteMessageConverter.toArray(new GetPlatformVarsNames(workersId, clientAddress)))

      RemoteMessageConverter.unpackAnyMsgFromArray(workerSocket.recv()) match {
        case m: PlatformVarsNames =>
          val res = m.names.toList
          logInfo(s"[PlatformContext]: received answer PlatformVarsNames with names ${res.mkString(",")} from platform")
          res
        case m: gtype.Error =>
          val errorMsg = "[PlatformContext]: Received error while settingVars: " + m.msg
          logError(errorMsg)
          throw new Exception(errorMsg)
        case m: messages.Message =>
          val errorMsg =
            "[PlatformContext]: Received unexpected remote message on SetPlatformVars request: " +
              m.`type`()
          logError(errorMsg)
          throw new Exception(errorMsg)
      }
    }
  }

  /** invoke function using context, can call also functions from other engines
    * and default context functions
    *
    * @param funcName
    *   name of function
    * @param args
    *   arguments for function
    */
  def invokeFunction(funcName: String, args: List[Type] = Nil): Type = {
    this.synchronized {
      logInfo(s"[PlatformContext]: was asked to invoke function $funcName using platform context")
      logInfo(s"[PlatformContext]: sending request InvokeFunction to platform")
      workerSocket.send(
        RemoteMessageConverter.toArray(
          new InvokeFunction(
            workersId,
            funcName,
            args.map(arg => GtypeConverter.toGeneratedMsg(arg)).toArray,
            clientAddress
          )
        )
      )

      RemoteMessageConverter.unpackAnyMsgFromArray(workerSocket.recv()) match {
        case m: InvokedFunctionResult =>
          logInfo(s"[PlatformContext]: received answer InvokedFunctionResult of function ${m.name} from platform")
          GtypeConverter.messageToGtype(m.result)
        case m: gtype.Error =>
          val errorMsg = s"[PlatformContext]: Received error while invoking function ${funcName}: " + m.msg
          logError(errorMsg)
          throw new Exception(errorMsg)
        case m: messages.Message =>
          val errorMsg =
            s"[PlatformContext]: Received unexpected remote message on invoking function ${funcName}: " +
              m.`type`()
          logError(errorMsg)
          throw new Exception(errorMsg)
      }
    }
  }
}
