import org.json.{JSONException, JSONObject}
import org.mixql.core.context.gtype
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter}
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.`type`.gtype.{Bool, gArray, gDouble, gString, map}
import org.mixql.remote.messages.client.InvokedPlatformFunctionResult
import org.mixql.remote.messages.module.toBroker.EnginePingHeartBeat

class RemoteMessageConverterToJson extends munit.FunSuite {

  def isJson(Json: String): Boolean = {
    try {
      new JSONObject(Json)
      true
    } catch {
      case _: JSONException => false
    }
  }

  test("convert EnginePingHeartBeat remote message to json and back") {

    val json = RemoteMessageConverter.toJson(new EnginePingHeartBeat("stub"))

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[EnginePingHeartBeat])
    assertEquals(res.asInstanceOf[EnginePingHeartBeat].engineName(), "stub")

  }

  test("convert InvokedPlatformFunctionResult remote message to json") {
    val m = new java.util.HashMap[Message, Message]()
    m.put(new gString("123.9", "'"), new org.mixql.remote.messages.`type`.gtype.Bool(false))
    m.put(new gString("8.8", "\""), new org.mixql.remote.messages.`type`.gtype.gDouble(123.9))

    val json = RemoteMessageConverter.toJson(
      new InvokedPlatformFunctionResult(
        "stub-engine",
        "stub",
        "worker1234566",
        "test_func",
        new gArray(
          Seq(
            {
              new gString("test", "")
            },
            new map(m),
            new gArray({
              Seq(new map(m)).toArray
            })
          ).toArray
        )
      )
    )

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[InvokedPlatformFunctionResult])

    val msg = res.asInstanceOf[InvokedPlatformFunctionResult]

    assertEquals(msg.moduleIdentity(), "stub-engine")
    assertEquals(msg.clientIdentity(), "stub")
    assertEquals(msg.workerIdentity(), "worker1234566")
    assertEquals(msg.name, "test_func")

    /////////////////////////////// Test message//////////////////////////////////////////

    val funcResult = msg.result

    assert(funcResult.isInstanceOf[gArray])
    val funcResultArr = funcResult.asInstanceOf[gArray].arr.toSeq

    val firstElem = funcResultArr.head
    assert(firstElem.isInstanceOf[gString])
    assertEquals(firstElem.asInstanceOf[gString].value, "test")

    {
      assert(funcResultArr(1).isInstanceOf[map])
      val secondElement = GtypeConverter.messageToGtype(funcResultArr(1)).asInstanceOf[gtype.map]
      val val1 = secondElement.getMap.get(new gtype.string("8.8", "\""))
      assert(val1.isInstanceOf[gtype.gDouble])
      assert(val1.asInstanceOf[gtype.gDouble].getValue == 123.9)

      val val2 = secondElement.getMap.get(new gtype.string("123.9", "'"))
      assert(val2.isInstanceOf[gtype.bool])
      assert(!val2.asInstanceOf[gtype.bool].getValue)
    }

    {
      assert(funcResultArr(2).isInstanceOf[gArray])
      val thirdElement = funcResultArr(2).asInstanceOf[gArray]
      val thirdElementArr = thirdElement.arr

      val val1Third = thirdElementArr.head
      assert(val1Third.isInstanceOf[map])
      val mapSecond = GtypeConverter.messageToGtype(val1Third).asInstanceOf[gtype.map]

      val val1 = mapSecond.getMap.get(new gtype.string("8.8", "\""))
      assert(val1.isInstanceOf[gtype.gDouble])
      assert(val1.asInstanceOf[gtype.gDouble].getValue == 123.9)

      val val2 = mapSecond.getMap.get(new gtype.string("123.9", "'"))
      assert(val2.isInstanceOf[gtype.bool])
      assert(!val2.asInstanceOf[gtype.bool].getValue)

    }
    //////////////////////////////////////////////////////////////////////////////////////

  }
}
