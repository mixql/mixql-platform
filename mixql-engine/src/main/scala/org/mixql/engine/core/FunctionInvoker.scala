package org.mixql.engine.core

import org.mixql.protobuf.{ProtoBufConverter, messages}

object FunctionInvoker {
  def invoke(functions: Map[String, Any],
             name: String,
             context: Object, // To support not only mixql-core context
             params: List[messages.Message] = Nil): messages.Message = {
    import org.mixql.core.context.gtype
    import org.mixql.protobuf.GtypeConverter
    val gParams: Seq[gtype.Type] =
      if (params.nonEmpty) {
        GtypeConverter.messagesToGtypes(params.toArray)
      } else
        Seq()
    val res = org.mixql.core.function.FunctionInvoker
      .invoke(functions, name, context, gParams.map(p => gtype.unpack(p)).toList)
    GtypeConverter.toGeneratedMsg(gtype.pack(res))
  }
}
