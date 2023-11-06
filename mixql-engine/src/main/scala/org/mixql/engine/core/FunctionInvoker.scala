package org.mixql.engine.core

import org.mixql.remote.messages.Message

import scala.collection.mutable

object FunctionInvoker {

  def invoke(functions: Map[String, Any],
             name: String,
             contexts: List[Object], // To support not only mixql-core context
             params: List[Message] = Nil,
             _kwargs: mutable.Map[String, Message] = mutable.Map.empty): Message = {
    import org.mixql.core.context.mtype
    import org.mixql.remote.GtypeConverter
    val gParams: Seq[mtype.MType] =
      if (params.nonEmpty) {
        GtypeConverter.messagesToGtypes(params.toArray)
      } else
        Seq()

    val kwargs: Map[String, Object] =
      if (params.nonEmpty) {
        _kwargs.map(t => t._1 -> mtype.unpack(GtypeConverter.messageToGtype(t._2)).asInstanceOf[Object]).toMap
      } else
        Map()
    val res = org.mixql.core.function.FunctionInvoker
      .invoke(functions, name, contexts, gParams.map(p => mtype.unpack(p)).toList, kwargs)
    GtypeConverter.toGeneratedMsg(mtype.pack(res))
  }
}
