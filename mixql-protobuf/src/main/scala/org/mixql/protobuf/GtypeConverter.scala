package org.mixql.protobuf

object GtypeConverter {

  import org.mixql.core.context.gtype
  import org.mixql.protobuf.messages.clientMsgs

  def toGtype(remoteMsg: scalapb.GeneratedMessage): gtype.Type = {

    remoteMsg match {
      case _: clientMsgs.NULL => gtype.Null
      case msg: clientMsgs.Bool => gtype.bool(msg.value)
      case msg: clientMsgs.Int => gtype.int(msg.value)
      case msg: clientMsgs.Double => gtype.double(msg.value)
      case msg: clientMsgs.String => gtype.string(msg.value)
      case msg: clientMsgs.Array =>
        gtype.array(
          msg.arr
            .map(f => protobufAnyToGtype(f))
            .toArray
        )
      case clientMsgs.Error(msg, _) => throw new Exception(msg)
      case a: scala.Any =>
        throw new Exception(
          s"RemoteMsgsConverter: toGtype error: " +
            s"got ${a.toString}, when type was expected"
        )
    }
  }

  def protobufAnyToGtype(f: com.google.protobuf.any.Any): gtype.Type = {
    if (f.is[clientMsgs.NULL])
      return gtype.Null

    if (f.is[clientMsgs.Bool]) {
      val msg = f.unpack[clientMsgs.Bool]
      return gtype.bool(msg.value)
    }

    if (f.is[clientMsgs.Int]) {
      val msg = f.unpack[clientMsgs.Int]
      return gtype.int(msg.value)
    }

    if (f.is[clientMsgs.Double]) {
      val msg = f.unpack[clientMsgs.Double]
      return gtype.double(msg.value)
    }

    if (f.is[clientMsgs.String]) {
      val msg = f.unpack[clientMsgs.String]
      return gtype.string(msg.value)
    }


    if (f.is[clientMsgs.Array]) {
      val msg = f.unpack[clientMsgs.Array]
      return toGtype(msg)
    }

    throw new Exception(
      s"protobufAnyToGtype: error:  " +
        s"could not convert com.google.protobuf.any.Any to gtype: got ${f.toString}, when gtype was expected"
    )
  }

  def toGeneratedMsg(gValue: gtype.Type): scalapb.GeneratedMessage = {
    gValue match {
      case gtype.Null =>
        clientMsgs.NULL()
      case gtype.bool(value) =>
        clientMsgs.Bool(value)
      case gtype.int(value) =>
        clientMsgs.Int(value)
      case gtype.double(value) =>
        clientMsgs.Double(value)
      case gtype.string(value, quote) =>
        clientMsgs.String(value, quote)
      case gtype.array(arr) =>
        clientMsgs.Array(
          arr
            .map(gType =>
              com.google.protobuf.any.Any.pack(toGeneratedMsg(gType))
            )
            .toSeq
        )
    }
  }

  def toProtobufAny(remoteMsg: scalapb.GeneratedMessage): com.google.protobuf.any.Any = {
    remoteMsg match {
      case _: clientMsgs.NULL => com.google.protobuf.any.Any.pack(clientMsgs.NULL())
      case msg: clientMsgs.Bool => com.google.protobuf.any.Any.pack(msg)
      case msg: clientMsgs.Int => com.google.protobuf.any.Any.pack(msg)
      case msg: clientMsgs.Double => com.google.protobuf.any.Any.pack(msg)
      case msg: clientMsgs.String => com.google.protobuf.any.Any.pack(msg)
      case msg: clientMsgs.Array => com.google.protobuf.any.Any.pack(msg)
      case clientMsgs.Error(msg, _) => throw new Exception(msg)
      case a: scala.Any =>
        throw new Exception(
          s"RemoteMsgsConverter: toProtobufAny error: " +
            s"got ${a.toString}, when type in scalapb.GeneratedMessage format was expected"
        )
    }
  }

  def toProtobufAny(gValue: gtype.Type): com.google.protobuf.any.Any = {
    gValue match {
      case gtype.Null =>
        com.google.protobuf.any.Any.pack(clientMsgs.NULL())
      case gtype.bool(value) =>
        com.google.protobuf.any.Any.pack(clientMsgs.Bool(value))
      case gtype.int(value) =>
        com.google.protobuf.any.Any.pack(clientMsgs.Int(value))
      case gtype.double(value) =>
        com.google.protobuf.any.Any.pack(clientMsgs.Double(value))
      case gtype.string(value, quote) =>
        com.google.protobuf.any.Any.pack(clientMsgs.String(value, quote))
      case gtype.array(arr) =>
        com.google.protobuf.any.Any.pack(
          clientMsgs.Array(
            arr
              .map(gType =>
                com.google.protobuf.any.Any.pack(toGeneratedMsg(gType))
              )
              .toSeq
          )
        )
    }
  }
}
