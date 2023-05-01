package org.mixql.protobuf

object GtypeConverter {

  import org.mixql.core.context.gtype

  def toGtype(remoteMsgs: Seq[messages.Message]): Seq[gtype.Type] = {
    remoteMsgs.map(
      m => toGtype(m)
    )
  }

  def toGtype(remoteMsg: messages.Message): gtype.Type = {

    remoteMsg match {
      case _: messages.NULL => new gtype.Null()
      case msg: messages.Bool => new gtype.bool(msg.value)
      case msg: messages.gInt => new gtype.gInt(msg.value)
      case msg: messages.gDouble => new gtype.gDouble(msg.value)
      case msg: messages.gString => new gtype.string(msg.value)
      case msg: messages.gArray =>
        new gtype.array(
          msg.arr
            .map(f => toGtype(f))
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
      case _: gtype.Null =>
        new messages.NULL()
      case m: gtype.bool =>
        new messages.Bool(m.getValue)
      case m: gtype.gInt =>
        new messages.gInt(m.getValue)
      case m: gtype.gDouble =>
        new messages.gDouble(m.getValue)
      case m: gtype.string =>
        new messages.gString(m.getValue, m.getQuote)
      case m: gtype.array =>
        new messages.gArray(
          m.getArr.map(gType => toGeneratedMsg(gType))
        )
    }
  }
}
