package org.mixql.engine.core

import org.mixql.core.context.gtype.Type
import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.remote.messages.module.worker.{GetPlatformVar, GetPlatformVars, GetPlatformVarsNames, PlatformVar, PlatformVarWasSet, PlatformVars, PlatformVarsNames, PlatformVarsWereSet, SetPlatformVar, SetPlatformVars}
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter}
import org.zeromq.ZMQ

import scala.collection.immutable.List
import scala.collection.mutable

class PlatformContext(workerSocket: ZMQ.Socket, workersId: String, clientAddress: Array[Byte])(implicit logger: ModuleLogger) {
  def setVar(key: String, value: Type): Unit = {
    workerSocket.send(RemoteMessageConverter.toArray(new SetPlatformVar(workersId,
      key, GtypeConverter.toGeneratedMsg(value), clientAddress
    )))
    RemoteMessageConverter.unpackAnyMsgFromArray(
      workerSocket.recv()
    ).asInstanceOf[PlatformVarWasSet]
  }

  def getVar(key: String): Type = {
    workerSocket.send(
      RemoteMessageConverter.toArray(
        new GetPlatformVar(workersId, key, clientAddress))
    )
    GtypeConverter.messageToGtype(
      RemoteMessageConverter.unpackAnyMsgFromArray(
        workerSocket.recv()
      ).asInstanceOf[PlatformVar].msg
    )
  }

  def getVars(keys: List[String]): mutable.Map[String, Type] = {
    workerSocket.send(RemoteMessageConverter.toArray(new GetPlatformVars(workersId, keys.toArray, clientAddress)))

    val rep = RemoteMessageConverter.unpackAnyMsgFromArray(
      workerSocket.recv()
    ).asInstanceOf[PlatformVars].vars

    val vars: mutable.Map[String, Type] = mutable.Map()

    rep.foreach(param => vars.put(param.name, GtypeConverter.messageToGtype(param.msg)))

    vars
  }

  def setVars(vars: Map[String, Type]): Unit = {
    import collection.JavaConverters._
    workerSocket.send(RemoteMessageConverter.toArray(new SetPlatformVars(workersId, vars.map(tuple =>
      tuple._1 -> GtypeConverter.toGeneratedMsg(tuple._2)).asJava, clientAddress))
    )
    RemoteMessageConverter.unpackAnyMsgFromArray(
      workerSocket.recv()
    ).asInstanceOf[PlatformVarsWereSet]
  }

  def getVarsNames(): List[String] = {
    workerSocket.send(RemoteMessageConverter.toArray(new GetPlatformVarsNames(workersId, clientAddress)))

    RemoteMessageConverter.unpackAnyMsgFromArray(
      workerSocket.recv()
    ).asInstanceOf[PlatformVarsNames].names.toList
  }
}
