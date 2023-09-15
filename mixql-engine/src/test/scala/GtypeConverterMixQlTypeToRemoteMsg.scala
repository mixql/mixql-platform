import org.mixql.core.context.gtype
import org.mixql.core.context.gtype.{bool, none, string}
import org.mixql.remote.GtypeConverter
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.`type`.Error
import org.mixql.remote.messages.`type`.gtype._
// For more information on writing tests, see

// https://scalameta.org/munit/docs/getting-started.html
class GtypeConverterMixQlTypeToRemoteMsg extends munit.FunSuite {

  test("convert gtype null to NULL remote message") {
    val res = GtypeConverter.toGeneratedMsg(new gtype.Null())
    assert(res.isInstanceOf[NULL])
  }

  test("convert gtype none to NONE remote message") {
    val res = GtypeConverter.toGeneratedMsg(new none())
    assert(res.isInstanceOf[NONE])
  }

  test("convert gtype bool to Bool remote message ") {
    val res = GtypeConverter.toGeneratedMsg(new bool(false))
    assert(res.isInstanceOf[Bool])
    assert(!res.asInstanceOf[Bool].value)
  }

  test("convert gtype gInt to org.mixql.remote.messages.type.gtype.gInt remote message ") {
    val res = GtypeConverter.toGeneratedMsg(new gtype.gInt(123))
    assert(res.isInstanceOf[gInt])
    assert(res.asInstanceOf[gInt].value == 123)
  }

  test("convert gtype gDouble to org.mixql.remote.messages.type.gtype.gDouble remote message ") {
    val res = GtypeConverter.toGeneratedMsg(new gtype.gDouble(123.9))
    assert(res.isInstanceOf[gDouble])
    assert(res.asInstanceOf[gDouble].value == 123.9)
  }

  test("convert gtype string to org.mixql.remote.messages.type.gtype.gString remote message ") {
    val res = GtypeConverter.toGeneratedMsg(new string("123.9", "'"))
    assert(res.isInstanceOf[gString])
    assert(res.asInstanceOf[gString].value == "123.9")
    assert(res.asInstanceOf[gString].quoted() == "'123.9'")
  }

  test("convert gtype map to org.mixql.remote.messages.type.gtype.map remote message") {
    val m = new java.util.HashMap[gtype.Type, gtype.Type]()
    m.put(new gtype.string("123.9", "'"), new gtype.bool(false))
    m.put(new gtype.string("8.8", "\""), new gtype.gDouble(123.9))

    val res = GtypeConverter.toGeneratedMsg(new gtype.map(m))
    assert(res.isInstanceOf[map])
    val gMap = res.asInstanceOf[map].getMap

    val val1: Message = gMap.get(new gString("123.9", "'"))
    assert(val1.isInstanceOf[Bool])
    assert(!val1.asInstanceOf[Bool].value)

    val val2: Message = gMap.get(new gString("8.8", "\""))
    assert(val2.isInstanceOf[gDouble])
    assert(val2.asInstanceOf[gDouble].value == 123.9)

  }

  test("convert org.mixql.remote.messages.type.gtype.gArray remote message to gtype array") {

    val res = GtypeConverter.toGeneratedMsg({
      new gtype.array(Seq[gtype.Type](new gtype.string("123.9", "'"), new gtype.string("8.8", "\"")).toArray)
    })
    assert(res.isInstanceOf[gArray])
    val arr = res.asInstanceOf[gArray].arr

    assertEquals(arr.length, 2)

    val val1: Message = arr(0)
    assert(val1.isInstanceOf[gString])
    assertEquals(val1.asInstanceOf[gString].quoted(), "'123.9'")

    val val2: Message = arr(1)
    assert(val2.isInstanceOf[gString])
    assertEquals(val2.asInstanceOf[gString].quoted(), "\"8.8\"")
  }

  // TO-DO Test nested array in array in array, nested map in map, array of maps, etc
}
