import org.mixql.core.context.gtype
import org.mixql.remote.GtypeConverter
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.`type`.gtype.{Bool, NONE, NULL, gDouble, gInt, gString, map, gArray}
import org.mixql.remote.messages.`type`.Error
// For more information on writing tests, see

// https://scalameta.org/munit/docs/getting-started.html
class GtypeConverterRemoteMsgToMixQlType extends munit.FunSuite {

  test("convert NULL remote message to gtype null") {
    val res = GtypeConverter.messageToGtype(new NULL())
    assert(res.isInstanceOf[gtype.Null])
  }

  test("convert NONE remote message to gtype none") {
    val res = GtypeConverter.messageToGtype(new NONE())
    assert(res.isInstanceOf[gtype.none])
  }

  test("convert Bool remote message to gtype bool") {
    val res = GtypeConverter.messageToGtype(new Bool(false))
    assert(res.isInstanceOf[gtype.bool])
    assert(!res.asInstanceOf[gtype.bool].getValue)
  }

  test("convert org.mixql.remote.messages.type.gtype.gInt remote message to gtype gInt") {
    val res = GtypeConverter.messageToGtype(new gInt(123))
    assert(res.isInstanceOf[gtype.gInt])
    assert(res.asInstanceOf[gtype.gInt].getValue == 123)
  }

  test("convert org.mixql.remote.messages.type.gtype.gDouble remote message to gtype gDouble") {
    val res = GtypeConverter.messageToGtype(new gDouble(123.9))
    assert(res.isInstanceOf[gtype.gDouble])
    assert(res.asInstanceOf[gtype.gDouble].getValue == 123.9)
  }

  test("convert org.mixql.remote.messages.type.gtype.gString remote message to gtype string") {
    val res = GtypeConverter.messageToGtype(new gString("123.9", "'"))
    assert(res.isInstanceOf[gtype.string])
    assert(res.asInstanceOf[gtype.string].getValue == "123.9")
    assert(res.asInstanceOf[gtype.string].quoted() == "'123.9'")
  }

  test("convert org.mixql.remote.messages.type.gtype.map remote message to gtype map") {
    val m = new java.util.HashMap[Message, Message]()
    m.put(new gString("123.9", "'"), new Bool(false))
    m.put(new gString("8.8", "\""), new gDouble(123.9))

    val res = GtypeConverter.messageToGtype(new map(m))
    assert(res.isInstanceOf[gtype.map])
    val gMap = res.asInstanceOf[gtype.map].getMap

    val val1: gtype.Type = gMap.get(new gtype.string("8.8", "\""))
    assert(val1.isInstanceOf[gtype.gDouble])
    assert(val1.asInstanceOf[gtype.gDouble].getValue == 123.9)

    val val2: gtype.Type = gMap.get(new gtype.string("123.9", "'"))
    assert(val2.isInstanceOf[gtype.bool])
    assert(!val2.asInstanceOf[gtype.bool].getValue)
  }

  test("convert org.mixql.remote.messages.type.gtype.gArray remote message to gtype array") {

    val res = GtypeConverter.messageToGtype({
      new gArray(Seq[Message](new gString("123.9", "'"), new gString("8.8", "\"")).toArray)
    })
    assert(res.isInstanceOf[gtype.array])
    val arr = res.asInstanceOf[gtype.array].getArr

    assertEquals(arr.length, 2)

    val val1: gtype.Type = arr(0)
    assert(val1.isInstanceOf[gtype.string])
    assertEquals(val1.asInstanceOf[gtype.string].quoted(), "'123.9'")

    val val2: gtype.Type = arr(1)
    assert(val2.isInstanceOf[gtype.string])
    assertEquals(val2.asInstanceOf[gtype.string].quoted(), "\"8.8\"")
  }

  test("when converts org.mixql.remote.messages.type.Error remote message throws exception") {
    interceptMessage[java.lang.Exception]("test-exception") {
      GtypeConverter.messageToGtype(new Error("test-exception"))
    }
  }

  // TO-DO Test nested array in array in array, nested map in map, array of maps, etc
}
