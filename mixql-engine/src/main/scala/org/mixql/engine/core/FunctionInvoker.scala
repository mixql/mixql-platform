package org.mixql.engine.core

import org.mixql.remote.messages
import org.mixql.remote.RemoteMessageConverter
import org.mixql.remote.messages.Message

object FunctionInvoker {
  def invoke(functions: Map[String, Any],
             name: String,
             contexts: List[Object], // To support not only mixql-core context
             params: List[Message] = Nil): Message = {
    import org.mixql.core.context.gtype
    import org.mixql.remote.GtypeConverter
    val gParams: Seq[gtype.Type] =
      if (params.nonEmpty) {
        GtypeConverter.messagesToGtypes(params.toArray)
      } else
        Seq()
    val res = org.mixql.core.function.FunctionInvoker
      .invoke(functions, name, contexts, gParams.map(p => gtype.unpack(p)).toList)
    GtypeConverter.toGeneratedMsg(gtype.pack(res))
  }
}
