package org.mixql.protobuf

object GtypeConverter {

  import org.mixql.core.context.gtype

  def toGtype(remoteMsg: messages.Message): gtype.Type = {

    remoteMsg match {
      case _: messages.NULL => gtype.Null
      case msg: messages.Bool => gtype.bool(msg.value)
      case msg: messages.int => gtype.int(msg.value)
      case msg: messages.double => gtype.double(msg.value)
      case msg: messages.gString => gtype.string(msg.value)
      case msg: messages.gArray =>
        gtype.array(
          msg.arr
            .map(f => toGtype(ProtoBufConverter.unpackAnyMsg(f)))
        )
      case messages.Error(msg) => throw new Exception(msg)
      case a: scala.Any =>
        throw new Exception(
          s"RemoteMsgsConverter: toGtype error: " +
            s"got ${a.toString}, when type was expected"
        )
    }
  }

  def toGeneratedMsg(gValue: gtype.Type): messages.Message = {
    import scala.util.Failure
    import scala.util.Success
    gValue match {
      case gtype.Null =>
        messages.NULL()
      case gtype.bool(value) =>
        messages.Bool(value)
      case gtype.int(value) =>
        messages.int(value)
      case gtype.double(value) =>
        messages.double(value)
      case gtype.string(value, quote) =>
        messages.gString(value, quote)
      case gtype.array(arr) =>
        messages.gArray(
          arr.map(gType => ProtoBufConverter.toJson(toGeneratedMsg(gType)) match {
            case Failure(exception) => throw new Exception(exception)
            case Success(json) => json
          })
        )
    }
  }
}
