package org.mixql.protobuf

object GtypeConverter {

  import org.mixql.core.context.gtype

  def toGtype(remoteMsg: messages.Message): gtype.Type = {

    remoteMsg match {
      case _: messages.NULL => gtype.Null
      case msg: messages.Bool => gtype.bool(msg.value)
      case msg: messages.gInt => gtype.int(msg.value)
      case msg: messages.gDouble => gtype.double(msg.value)
      case msg: messages.gString => gtype.string(msg.value)
      case msg: messages.gArray =>
        gtype.array(
          msg.arr
            .map(f => toGtype(ProtoBufConverter.unpackAnyMsg(f)))
        )
      case m: messages.Error => throw new Exception(m.msg)
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
        new messages.NULL()
      case gtype.bool(value) =>
        new messages.Bool(value)
      case gtype.int(value) =>
        new messages.gInt(value)
      case gtype.double(value) =>
        new messages.gDouble(value)
      case gtype.string(value, quote) =>
        new messages.gString(value, quote)
      case gtype.array(arr) =>
        new messages.gArray(
          arr.map(gType => ProtoBufConverter.toJson(toGeneratedMsg(gType)) match {
            case Failure(exception) => throw new Exception(exception)
            case Success(json) => json
          })
        )
    }
  }
}
