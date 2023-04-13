package org.mixql.protobuf

object GtypeConverter {

  import org.mixql.core.context.gtype
  import org.mixql.protobuf.generated.messages

  def toGtype(remoteMsg: com.google.protobuf.GeneratedMessageV3): gtype.Type = {
    import collection.JavaConverters._

    remoteMsg match {
      case _: messages.NULL => gtype.Null
      case msg: messages.Bool => gtype.bool(msg.getValue)
      case msg: messages.Int => gtype.int(msg.getValue)
      case msg: messages.Double => gtype.double(msg.getValue)
      case msg: messages.String => gtype.string(msg.getValue)
      case msg: messages.Array =>
        gtype.array(
          msg.getArrList.asScala
            .map(f => protobufAnyToGtype(f))
            .toArray
        )
      case msg: messages.Error => throw new Exception(msg.getMsg)
      case a: scala.Any =>
        throw new Exception(
          s"RemoteMsgsConverter: toGtype error: " +
            s"got ${a.toString}, when type was expected"
        )
    }
  }

  def protobufAnyToGtype(f: com.google.protobuf.Any): gtype.Type = {
    if (f.is(messages.NULL.getDefaultInstance.getClass))
      return gtype.Null

    if (f.is(messages.Bool.getDefaultInstance.getClass)) {
      val msg = f.unpack(messages.Bool.getDefaultInstance.getClass)
      return gtype.bool(msg.getValue)
    }

    if (f.is(messages.Int.getDefaultInstance.getClass)) {
      val msg = f.unpack(messages.Int.getDefaultInstance.getClass)
      return gtype.int(msg.getValue)
    }

    if (f.is(messages.Double.getDefaultInstance.getClass)) {
      val msg = f.unpack(messages.Double.getDefaultInstance.getClass)
      return gtype.double(msg.getValue)
    }

    if (f.is(messages.String.getDefaultInstance.getClass)) {
      val msg = f.unpack(messages.String.getDefaultInstance.getClass)
      return gtype.string(msg.getValue)
    }


    if (f.is(messages.Array.getDefaultInstance.getClass)) {
      val msg = f.unpack(messages.Array.getDefaultInstance.getClass)
      return toGtype(msg)
    }

    throw new Exception(
      s"protobufAnyToGtype: error:  " +
        s"could not convert com.google.protobuf.any.Any to gtype: got ${f.toString}, when gtype was expected"
    )
  }

  def toGeneratedMsg(gValue: gtype.Type): com.google.protobuf.GeneratedMessageV3 = {
    gValue match {
      case gtype.Null =>
        messages.NULL.newBuilder().build()
      case gtype.bool(value) =>
        messages.Bool.newBuilder().setValue(value).build()
      case gtype.int(value) =>
        messages.Int.newBuilder().setValue(value).build()
      case gtype.double(value) =>
        messages.Double.newBuilder().setValue(value).build()
      case gtype.string(value, quote) =>
        messages.String.newBuilder()
          .setValue(value)
          .setQuote(quote)
          .build()
      case gtype.array(arr) =>
        var arrayBuilder = messages.Array.newBuilder

        arr.map(gType =>
          com.google.protobuf.Any.pack(toGeneratedMsg(gType))
        ) foreach (
          v => arrayBuilder = arrayBuilder.addArr(v)
          )
        arrayBuilder.build()
    }
  }

  def toProtobufAny(remoteMsg: com.google.protobuf.GeneratedMessageV3): com.google.protobuf.Any = {
    remoteMsg match {
      case _: messages.NULL => com.google.protobuf.Any.pack(messages.NULL.getDefaultInstance)
      case msg: messages.Bool => com.google.protobuf.Any.pack(msg)
      case msg: messages.Int => com.google.protobuf.Any.pack(msg)
      case msg: messages.Double => com.google.protobuf.Any.pack(msg)
      case msg: messages.String => com.google.protobuf.Any.pack(msg)
      case msg: messages.Array => com.google.protobuf.Any.pack(msg)
      case msg: messages.Error => throw new Exception(msg.getMsg)
      case a: scala.Any =>
        throw new Exception(
          s"RemoteMsgsConverter: toProtobufAny error: " +
            s"got ${a.toString}, when type in scalapb.GeneratedMessage format was expected"
        )
    }
  }

  def toProtobufAny(gValue: gtype.Type): com.google.protobuf.Any = {
    gValue match {
      case gtype.Null =>
        com.google.protobuf.Any.pack(messages.NULL.newBuilder().build())
      case gtype.bool(value) =>
        com.google.protobuf.Any.pack(messages.Bool.newBuilder().setValue(value).build())
      case gtype.int(value) =>
        com.google.protobuf.Any.pack(messages.Int.newBuilder().setValue(value).build())
      case gtype.double(value) =>
        com.google.protobuf.Any.pack(messages.Double.newBuilder().setValue(value).build())
      case gtype.string(value, quote) =>
        com.google.protobuf.Any.pack(messages.String.newBuilder()
          .setValue(value).setQuote(quote).build()
        )
      case gtype.array(arr) =>
        var arrayBuilder = messages.Array.newBuilder

        arr.map(gType =>
          com.google.protobuf.Any.pack(toGeneratedMsg(gType))
        ) foreach (
          v => arrayBuilder = arrayBuilder.addArr(v)
          )

        com.google.protobuf.Any.pack(
          arrayBuilder.build()
        )
    }
  }
}
