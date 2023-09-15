import org.json.{JSONException, JSONObject}
import org.mixql.core.context.gtype
import org.mixql.core.context.gtype.string
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter}
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.`type`.gtype.{Bool, gArray, gDouble, gString, map}
import org.mixql.remote.messages.broker.{
  CouldNotConvertMsgError,
  EngineStartedTimeOutElapsedError,
  PlatformPongHeartBeat
}
import org.mixql.remote.messages.client.toBroker.EngineStarted
import org.mixql.remote.messages.client.{
  Execute,
  ExecuteFunction,
  GetDefinedFunctions,
  InvokedPlatformFunctionResult,
  PlatformVar,
  PlatformVarWasSet,
  PlatformVars,
  PlatformVarsNames,
  PlatformVarsWereSet,
  ShutDown
}
import org.mixql.remote.messages.module.ExecuteResult
import org.mixql.remote.messages.module.toBroker.EnginePingHeartBeat
import org.mixql.remote.messages.`type`.{Error, Param}

import java.util

class RemoteMessageConverterTest extends munit.FunSuite {

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

  test("convert Execute remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new Execute("stub", "client-stub", "test-stmt")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[Execute])
    val execute = res.asInstanceOf[Execute]
    assertEquals(execute.moduleIdentity(), "stub")
    assertEquals(execute.clientIdentity(), "client-stub")
    assertEquals(execute.statement, "test-stmt")
  }

  test("convert ExecuteResult remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new ExecuteResult("test-stmt", new gString("stub", "").asInstanceOf[Message], "client-stub")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[ExecuteResult])
    val msg = res.asInstanceOf[ExecuteResult]
    assertEquals(msg.clientIdentity(), "client-stub")
    assert(msg.result.isInstanceOf[gString])
    assertEquals(msg.result.asInstanceOf[gString].value, "stub")
    assertEquals(msg.stmt, "test-stmt")
  }

  test("convert EngineStarted remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new EngineStarted("stub", "client-stub", 6000)
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[EngineStarted])
    val msg = res.asInstanceOf[EngineStarted]
    assertEquals(msg.clientIdentity(), "client-stub")
    assertEquals(msg.engineName, "stub")
    assert(msg.getTimeout.isInstanceOf[Long])
    assertEquals(msg.getTimeout.toString, 6000.toString)
  }

  test("convert EngineStartedTimeOutElapsedError remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new EngineStartedTimeOutElapsedError("stub", "error-test")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[EngineStartedTimeOutElapsedError])
    val msg = res.asInstanceOf[EngineStartedTimeOutElapsedError]
    assertEquals(msg.getErrorMessage(), "error-test")
    assertEquals(msg.engineName, "stub")
  }

  test("convert CouldNotConvertMsgError remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new CouldNotConvertMsgError("error-test")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[CouldNotConvertMsgError])
    assert(res.isInstanceOf[Error])
    val msg = res.asInstanceOf[CouldNotConvertMsgError]
    assertEquals(msg.getErrorMessage(), "error-test")
  }

  test("convert PlatformPongHeartBeat remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new PlatformPongHeartBeat()
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[PlatformPongHeartBeat])
  }

  test("convert Param remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new Param("test-param-name", new gString("123.9", "'"))
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[Param])
    val param = res.asInstanceOf[Param]
    assertEquals(param.name, "test-param-name")

    val msgRAW = param.msg
    assert(msgRAW.isInstanceOf[gString])
    val msg = msgRAW.asInstanceOf[gString]
    assertEquals(msg.value, "123.9")
  }

  test("convert ExecuteFunction remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new ExecuteFunction("stub", "stub-client", "testFuncName", Seq[Message](new gString("123.9", "'")).toArray)
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[ExecuteFunction])

    val res = resRAW.asInstanceOf[ExecuteFunction]

    assertEquals(res.moduleIdentity, "stub")
    assertEquals(res.clientIdentity(), "stub-client")
    assertEquals(res.name, "testFuncName")

    assert(res.params.length == 1)

    {
      val value = res.params(0)
      assert(value.isInstanceOf[gString])
      assertEquals(value.asInstanceOf[gString].value, "123.9")
    }
  }

  test("convert GetDefinedFunctions remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new GetDefinedFunctions("stub", "stub-client")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[GetDefinedFunctions])

    val res = resRAW.asInstanceOf[GetDefinedFunctions]

    assertEquals(res.moduleIdentity, "stub")
    assertEquals(res.clientIdentity(), "stub-client")
  }

  test("convert PlatformVar remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new PlatformVar("stub", "stub-client", "worker1233", "test-var-name", new gDouble(123.9))
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[PlatformVar])

    val res = resRAW.asInstanceOf[PlatformVar]

    assertEquals(res.moduleIdentity, "stub")
    assertEquals(res.clientIdentity(), "stub-client")
    assertEquals(res.workerIdentity(), "worker1233")
    assertEquals(res.name, "test-var-name")

    val valueRAW = res.msg
    assert(valueRAW.isInstanceOf[gDouble])
    assert(valueRAW.asInstanceOf[gDouble].value == 123.9)
  }

  test("convert PlatformVars remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new PlatformVars(
        "stub",
        "stub-client",
        "worker1233",
        Seq[Param](new Param("test-param-name", new gString("123.9", "'"))).toArray
      )
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[PlatformVars])

    val res = resRAW.asInstanceOf[PlatformVars]

    assertEquals(res.moduleIdentity, "stub")
    assertEquals(res.clientIdentity(), "stub-client")
    assertEquals(res.workerIdentity(), "worker1233")

    val vars = res.vars
    assert(vars.length == 1)

    {
      val value = vars(0)
      assertEquals(value.name, "test-param-name")

      val msgRAW = value.msg
      assert(msgRAW.isInstanceOf[gString])
      val msg = msgRAW.asInstanceOf[gString]
      assertEquals(msg.value, "123.9")
    }
  }

  test("convert PlatformVarsNames remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new PlatformVarsNames(
        "stub",
        "stub-client",
        "worker1233",
        Seq[String]("test-var-name1", "test-var-name2").toArray
      )
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[PlatformVarsNames])

    val res = resRAW.asInstanceOf[PlatformVarsNames]

    assertEquals(res.moduleIdentity, "stub")
    assertEquals(res.clientIdentity(), "stub-client")
    assertEquals(res.workerIdentity(), "worker1233")

    val vars = res.names
    assert(vars.length == 2)

    assertEquals(vars(0), "test-var-name1")
    assertEquals(vars(1), "test-var-name2")
  }

  test("convert PlatformVarsWereSet remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new PlatformVarsWereSet(
        "stub",
        "stub-client",
        "worker1233", {
          val names: util.ArrayList[String] = new util.ArrayList()
          names.add("test-var-name1")
          names.add("test-var-name2")
          names
        }
      )
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[PlatformVarsWereSet])

    val res = resRAW.asInstanceOf[PlatformVarsWereSet]

    assertEquals(res.moduleIdentity, "stub")
    assertEquals(res.clientIdentity(), "stub-client")
    assertEquals(res.workerIdentity(), "worker1233")

    val vars = res.names
    assert(vars.size() == 2)

    assertEquals(vars.get(0), "test-var-name1")
    assertEquals(vars.get(1), "test-var-name2")
  }

  test("convert PlatformVarWasSet remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new PlatformVarWasSet("stub", "stub-client", "worker1233", "test-var-name1")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[PlatformVarWasSet])

    val res = resRAW.asInstanceOf[PlatformVarWasSet]

    assertEquals(res.moduleIdentity, "stub")
    assertEquals(res.clientIdentity(), "stub-client")
    assertEquals(res.workerIdentity(), "worker1233")
    assertEquals(res.name, "test-var-name1")
  }

  test("convert ShutDown remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new ShutDown("stub", "stub-client")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[ShutDown])

    val res = resRAW.asInstanceOf[ShutDown]

    assertEquals(res.moduleIdentity, "stub")
    assertEquals(res.clientIdentity(), "stub-client")
  }

}
