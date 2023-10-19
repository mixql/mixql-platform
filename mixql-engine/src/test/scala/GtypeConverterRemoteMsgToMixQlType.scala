import org.mixql.core.context.mtype
import org.mixql.remote.GtypeConverter
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.rtype.mtype.{MBool, MNONE, MNULL, MDouble, MInt, MString, MMap, MArray}
import org.mixql.remote.messages.rtype.Error
// For more information on writing tests, see

// https://scalameta.org/munit/docs/getting-started.html
class GtypeConverterRemoteMsgToMixQlType extends munit.FunSuite {

  test("convert NULL remote message to gtype null") {
    val res = GtypeConverter.messageToGtype(new MNULL())
    assert(res.isInstanceOf[mtype.MNull])
  }

  test("convert NONE remote message to gtype none") {
    val res = GtypeConverter.messageToGtype(new MNONE())
    assert(res.isInstanceOf[mtype.MNone])
  }

  test("convert Bool remote message to gtype bool") {
    val res = GtypeConverter.messageToGtype(new MBool(false))
    assert(res.isInstanceOf[mtype.MBool])
    assert(!res.asInstanceOf[mtype.MBool].getValue)
  }

  test("convert org.mixql.remote.messages.rtype.mtype.MInt remote message to gtype gInt") {
    val res = GtypeConverter.messageToGtype(new MInt(123))
    assert(res.isInstanceOf[mtype.MInt])
    assert(res.asInstanceOf[mtype.MInt].getValue == 123)
  }

  test("convert org.mixql.remote.messages.rtype.mtype.MDouble remote message to gtype gDouble") {
    val res = GtypeConverter.messageToGtype(new MDouble(123.9))
    assert(res.isInstanceOf[mtype.MDouble])
    assert(res.asInstanceOf[mtype.MDouble].getValue == 123.9)
  }

  test("convert org.mixql.remote.messages.rtype.mtype.gString remote message to gtype string") {
    val res = GtypeConverter.messageToGtype(new MString("123.9", "'"))
    assert(res.isInstanceOf[mtype.MString])
    assert(res.asInstanceOf[mtype.MString].getValue == "123.9")
    assert(res.asInstanceOf[mtype.MString].quoted() == "'123.9'")
  }

  test("convert org.mixql.remote.messages.rtype.mtype.MMap remote message to gtype map") {
    val m = new java.util.HashMap[Message, Message]()
    m.put(new MString("123.9", "'"), new MBool(false))
    m.put(new MString("8.8", "\""), new MDouble(123.9))

    val res = GtypeConverter.messageToGtype(new MMap(m))
    assert(res.isInstanceOf[mtype.MMap])
    val gMap = res.asInstanceOf[mtype.MMap].getMap

    val val1: mtype.MType = gMap.get(new mtype.MString("8.8", "\""))
    assert(val1.isInstanceOf[mtype.MDouble])
    assert(val1.asInstanceOf[mtype.MDouble].getValue == 123.9)

    val val2: mtype.MType = gMap.get(new mtype.MString("123.9", "'"))
    assert(val2.isInstanceOf[mtype.MBool])
    assert(!val2.asInstanceOf[mtype.MBool].getValue)
  }

  test("convert org.mixql.remote.messages.rtype.mtype.gArray remote message to gtype array") {

    val res = GtypeConverter.messageToGtype({
      new MArray(Seq[Message](new MString("123.9", "'"), new MString("8.8", "\"")).toArray)
    })
    assert(res.isInstanceOf[mtype.MArray])
    val arr = res.asInstanceOf[mtype.MArray].getArr

    assertEquals(arr.length, 2)

    val val1: mtype.MType = arr(0)
    assert(val1.isInstanceOf[mtype.MString])
    assertEquals(val1.asInstanceOf[mtype.MString].quoted(), "'123.9'")

    val val2: mtype.MType = arr(1)
    assert(val2.isInstanceOf[mtype.MString])
    assertEquals(val2.asInstanceOf[mtype.MString].quoted(), "\"8.8\"")
  }

  test("when converts org.mixql.remote.messages.rtype.Error remote message throws exception") {
    interceptMessage[java.lang.Exception]("test-exception") {
      GtypeConverter.messageToGtype(new Error("test-exception"))
    }
  }

  // TO-DO Test nested array in array in array, nested map in map, array of maps, etc
}
