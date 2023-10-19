import org.mixql.core.context.mtype
import org.mixql.core.context.mtype.{MBool, MNone, MNull, MString}
import org.mixql.remote.GtypeConverter
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.rtype.Error
import org.mixql.remote.messages.rtype
// For more information on writing tests, see

// https://scalameta.org/munit/docs/getting-started.html
class GtypeConverterMixQlTypeToRemoteMsg extends munit.FunSuite {

  test("convert gtype null to NULL remote message") {
    val res = GtypeConverter.toGeneratedMsg(MNull.get())
    assert(res.isInstanceOf[rtype.mtype.MNULL])
  }

  test("convert gtype none to NONE remote message") {
    val res = GtypeConverter.toGeneratedMsg(MNone.get())
    assert(res.isInstanceOf[rtype.mtype.MNONE])
  }

  test("convert gtype bool to Bool remote message ") {
    val res = GtypeConverter.toGeneratedMsg(new MBool(false))
    assert(res.isInstanceOf[rtype.mtype.MBool])
    assert(!res.asInstanceOf[rtype.mtype.MBool].value)
  }

  test("convert gtype gInt to org.mixql.remote.messages.rtype.mtype.MInt remote message ") {
    val res = GtypeConverter.toGeneratedMsg(new mtype.MInt(123))
    assert(res.isInstanceOf[rtype.mtype.MInt])
    assert(res.asInstanceOf[rtype.mtype.MInt].value == 123)
  }

  test("convert gtype gDouble to org.mixql.remote.messages.rtype.mtype.gDouble remote message ") {
    val res = GtypeConverter.toGeneratedMsg(new mtype.MDouble(123.9))
    assert(res.isInstanceOf[rtype.mtype.MDouble])
    assert(res.asInstanceOf[rtype.mtype.MDouble].value == 123.9)
  }

  test("convert gtype string to org.mixql.remote.messages.rtype.mtype.gString remote message ") {
    val res = GtypeConverter.toGeneratedMsg(new MString("123.9", "'"))
    assert(res.isInstanceOf[rtype.mtype.MString])
    assert(res.asInstanceOf[rtype.mtype.MString].value == "123.9")
    assert(res.asInstanceOf[rtype.mtype.MString].quoted() == "'123.9'")
  }

  test("convert gtype map to org.mixql.remote.messages.rtype.mtype.map remote message") {
    val m = new java.util.HashMap[mtype.MType, mtype.MType]()
    m.put(new mtype.MString("123.9", "'"), new mtype.MBool(false))
    m.put(new mtype.MString("8.8", "\""), new mtype.MDouble(123.9))

    val res = GtypeConverter.toGeneratedMsg(new mtype.MMap(m))
    assert(res.isInstanceOf[rtype.mtype.MMap])
    val rMMap = res.asInstanceOf[rtype.mtype.MMap].getMap

    val val1: Message = rMMap.get(new rtype.mtype.MString("123.9", "'"))
    assert(val1.isInstanceOf[rtype.mtype.MBool])
    assert(!val1.asInstanceOf[rtype.mtype.MBool].value)

    val val2: Message = rMMap.get(new rtype.mtype.MString("8.8", "\""))
    assert(val2.isInstanceOf[rtype.mtype.MDouble])
    assert(val2.asInstanceOf[rtype.mtype.MDouble].value == 123.9)

  }

  test("convert org.mixql.remote.messages.rtype.mtype.gArray remote message to gtype array") {

    val res = GtypeConverter.toGeneratedMsg({
      new mtype.MArray(Seq[mtype.MType](new mtype.MString("123.9", "'"), new mtype.MString("8.8", "\"")).toArray)
    })
    assert(res.isInstanceOf[rtype.mtype.MArray])
    val arr = res.asInstanceOf[rtype.mtype.MArray].arr

    assertEquals(arr.length, 2)

    val val1: Message = arr(0)
    assert(val1.isInstanceOf[rtype.mtype.MString])
    assertEquals(val1.asInstanceOf[rtype.mtype.MString].quoted(), "'123.9'")

    val val2: Message = arr(1)
    assert(val2.isInstanceOf[rtype.mtype.MString])
    assertEquals(val2.asInstanceOf[rtype.mtype.MString].quoted(), "\"8.8\"")
  }

  // TO-DO Test nested array in array in array, nested map in map, array of maps, etc
}
