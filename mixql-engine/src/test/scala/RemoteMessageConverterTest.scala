import org.json.{JSONException, JSONObject}
import org.mixql.core.context.mtype
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter}
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.rtype.mtype.{MBool, MNONE, MNULL, MArray, MDouble, MInt, MString, MMap}
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
import org.mixql.remote.messages.module.{
  DefinedFunctions,
  ExecuteResult,
  ExecuteResultFailed,
  ExecutedFunctionResult,
  ExecutedFunctionResultFailed,
  GetDefinedFunctionsError
}
import org.mixql.remote.messages.module.toBroker.{EngineFailed, EngineIsReady, EnginePingHeartBeat}
import org.mixql.remote.messages.rtype.{Error, Param}
import org.mixql.remote.messages.module.worker.{
  GetPlatformVar,
  GetPlatformVars,
  GetPlatformVarsNames,
  InvokeFunction,
  SendMsgToPlatform,
  SetPlatformVar,
  SetPlatformVars,
  WorkerFinished
}

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

  test("convert Bool remote message to json and back") {

    val json = RemoteMessageConverter.toJson(new MBool(false))

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[MBool])
    assert(!res.asInstanceOf[MBool].value)
  }

  test("convert gArray remote message to json and back") {

    val json = RemoteMessageConverter
      .toJson(new MArray(Seq[Message](new MString("123.9", "'"), new MString("8.8", "\"")).toArray))

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[MArray])
    val arr = res.asInstanceOf[MArray].arr

    assertEquals(arr.length, 2)

    {
      val value: Message = arr(0)
      assert(value.isInstanceOf[MString])
      assertEquals(value.asInstanceOf[MString].quoted(), "'123.9'")
    }

    {
      val val2: Message = arr(1)
      assert(val2.isInstanceOf[MString])
      assertEquals(val2.asInstanceOf[MString].quoted(), "\"8.8\"")
    }
  }

  test("convert gDouble remote message to json and back") {

    val json = RemoteMessageConverter.toJson(new MDouble(123.9))

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[MDouble])
    assertEqualsDouble(Double.box(res.asInstanceOf[MDouble].value), Double.box(123.9), Double.box(0.0001))
  }

  test("convert gInt remote message to json and back") {

    val json = RemoteMessageConverter.toJson(new MInt(123))

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[MInt])
    assertEquals(res.asInstanceOf[MInt].value.toString, 123.toString)
  }

  test("convert gString remote message to json and back") {

    val json = RemoteMessageConverter.toJson(new MString("123.9", "'"))

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[MString])
    assertEquals(res.asInstanceOf[MString].value, "123.9")
    assertEquals(res.asInstanceOf[MString].quoted(), "'123.9'")
  }

  test("convert org.mixql.remote.messages.type.mtype.MMap to json and back") {
    val m = new java.util.HashMap[Message, Message]()
    m.put(new MString("123.9", "'"), new MBool(false))
    m.put(new MString("8.8", "\""), new MDouble(123.9))

    val json = RemoteMessageConverter.toJson(new MMap(m))
    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[MMap])
    val gMap = res.asInstanceOf[MMap].getMap

    val val1: Message = gMap.get(new MString("8.8", "\""))
    assert(val1.isInstanceOf[MDouble])
    assertEqualsDouble(Double.box(val1.asInstanceOf[MDouble].value), Double.box(123.9), Double.box(0.0001))

    val val2: Message = gMap.get(new MString("123.9", "'"))
    assert(val2.isInstanceOf[MBool])
    assert(!val2.asInstanceOf[MBool].value)
  }

  test("convert NONE remote message to json and back") {

    val json = RemoteMessageConverter.toJson(new MNONE())

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[MNONE])
  }

  test("convert NULL remote message to json and back") {

    val json = RemoteMessageConverter.toJson(new MNULL())

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[MNULL])
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
    m.put(new MString("123.9", "'"), new MBool(false))
    m.put(new MString("8.8", "\""), new MDouble(123.9))

    val json = RemoteMessageConverter.toJson(
      new InvokedPlatformFunctionResult(
        "stub-engine",
        "stub",
        "worker1234566",
        "test_func",
        new MArray(
          Seq(
            {
              new MString("test", "")
            },
            new MMap(m),
            new MArray({
              Seq(new MMap(m)).toArray
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

    assert(funcResult.isInstanceOf[MArray])
    val funcResultArr = funcResult.asInstanceOf[MArray].arr.toSeq

    val firstElem = funcResultArr.head
    assert(firstElem.isInstanceOf[MString])
    assertEquals(firstElem.asInstanceOf[MString].value, "test")

    {
      assert(funcResultArr(1).isInstanceOf[MMap])
      val secondElement = GtypeConverter.messageToGtype(funcResultArr(1)).asInstanceOf[mtype.MMap]
      val val1 = secondElement.getMap.get(new mtype.MString("8.8", "\""))
      assert(val1.isInstanceOf[mtype.MDouble])
      assert(val1.asInstanceOf[mtype.MDouble].getValue == 123.9)

      val val2 = secondElement.getMap.get(new mtype.MString("123.9", "'"))
      assert(val2.isInstanceOf[mtype.MBool])
      assert(!val2.asInstanceOf[mtype.MBool].getValue)
    }

    {
      assert(funcResultArr(2).isInstanceOf[MArray])
      val thirdElement = funcResultArr(2).asInstanceOf[MArray]
      val thirdElementArr = thirdElement.arr

      val val1Third = thirdElementArr.head
      assert(val1Third.isInstanceOf[MMap])
      val mapSecond = GtypeConverter.messageToGtype(val1Third).asInstanceOf[mtype.MMap]

      val val1 = mapSecond.getMap.get(new mtype.MString("8.8", "\""))
      assert(val1.isInstanceOf[mtype.MDouble])
      assert(val1.asInstanceOf[mtype.MDouble].getValue == 123.9)

      val val2 = mapSecond.getMap.get(new mtype.MString("123.9", "'"))
      assert(val2.isInstanceOf[mtype.MBool])
      assert(!val2.asInstanceOf[mtype.MBool].getValue)

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
      new ExecuteResult("test-stmt", new MString("stub", "").asInstanceOf[Message], "client-stub")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[ExecuteResult])
    val msg = res.asInstanceOf[ExecuteResult]
    assertEquals(msg.clientIdentity(), "client-stub")
    assert(msg.result.isInstanceOf[MString])
    assertEquals(msg.result.asInstanceOf[MString].value, "stub")
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

  test("convert Error remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new Error("error-test")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[Error])
    val msg = res.asInstanceOf[Error]
    assertEquals(msg.getErrorMessage(), "error-test")
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
      new Param("test-param-name", new MString("123.9", "'"))
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val res = RemoteMessageConverter.unpackAnyMsg(json)
    assert(res.isInstanceOf[Param])
    val param = res.asInstanceOf[Param]
    assertEquals(param.name, "test-param-name")

    val msgRAW = param.msg
    assert(msgRAW.isInstanceOf[MString])
    val msg = msgRAW.asInstanceOf[MString]
    assertEquals(msg.value, "123.9")
  }

  test("convert ExecuteFunction remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new ExecuteFunction("stub", "stub-client", "testFuncName", Seq[Message](new MString("123.9", "'")).toArray)
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
      assert(value.isInstanceOf[MString])
      assertEquals(value.asInstanceOf[MString].value, "123.9")
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
      new PlatformVar("stub", "stub-client", "worker1233", "test-var-name", new MDouble(123.9))
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
    assert(valueRAW.isInstanceOf[MDouble])
    assert(valueRAW.asInstanceOf[MDouble].value == 123.9)
  }

  test("convert PlatformVars remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new PlatformVars(
        "stub",
        "stub-client",
        "worker1233",
        Seq[Param](new Param("test-param-name", new MString("123.9", "'"))).toArray
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
      assert(msgRAW.isInstanceOf[MString])
      val msg = msgRAW.asInstanceOf[MString]
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

  test("convert EngineFailed remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new EngineFailed("stub", "error-msg-test")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[EngineFailed])

    val res = resRAW.asInstanceOf[EngineFailed]

    assertEquals(res.engineName(), "stub")
    assertEquals(res.getErrorMessage(), "error-msg-test")
    assert(res.isInstanceOf[Error])
  }

  test("convert EngineIsReady remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new EngineIsReady("stub")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[EngineIsReady])

    val res = resRAW.asInstanceOf[EngineIsReady]

    assertEquals(res.engineName(), "stub")
  }

  test("convert GetPlatformVar remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new GetPlatformVar("worker1234", "test-var-name", "stub-client")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[GetPlatformVar])

    val res = resRAW.asInstanceOf[GetPlatformVar]

    assertEquals(res.workerIdentity(), "worker1234")
    assertEquals(res.name, "test-var-name")
    assertEquals(res.clientIdentity(), "stub-client")
  }

  test("convert GetPlatformVars remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new GetPlatformVars(
        "worker1234",
        Seq[String]("test-var-name-1", "test-var-name-2", "test-var-name-3").toArray,
        "stub-client"
      )
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[GetPlatformVars])

    val res = resRAW.asInstanceOf[GetPlatformVars]

    assertEquals(res.workerIdentity(), "worker1234")
    assertEquals(res.clientIdentity(), "stub-client")

    assert(res.names.length == 3)
    assertEquals(res.names(0), "test-var-name-1")
    assertEquals(res.names(1), "test-var-name-2")
    assertEquals(res.names(2), "test-var-name-3")
  }

  test("convert GetPlatformVarsNames remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new GetPlatformVarsNames("worker1234", "stub-client")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[GetPlatformVarsNames])

    val res = resRAW.asInstanceOf[GetPlatformVarsNames]

    assertEquals(res.workerIdentity(), "worker1234")
    assertEquals(res.clientIdentity(), "stub-client")
  }

  test("convert InvokeFunction remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new InvokeFunction(
        "worker1234",
        "funcNameTest",
        Seq[Message](new MString("123.9", "'"), new MDouble(123.9)).toArray,
        "stub-client"
      )
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[InvokeFunction])

    val res = resRAW.asInstanceOf[InvokeFunction]

    assertEquals(res.workerIdentity(), "worker1234")
    assertEquals(res.clientIdentity(), "stub-client")
    assertEquals(res.name, "funcNameTest")

    val args = res.args
    assert(args.length == 2)

    {
      val valueRAW = args(0)
      assert(valueRAW.isInstanceOf[MString])
      val arg = valueRAW.asInstanceOf[MString]
      assertEquals(arg.value, "123.9")
    }

    {
      val valueRAW = args(1)
      assert(valueRAW.isInstanceOf[MDouble])
      val arg = valueRAW.asInstanceOf[MDouble]
      assertEqualsDouble(Double.box(arg.value), Double.box(123.9), Double.box(0.0001))
    }
  }

  test("convert SendMsgToPlatform remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new SendMsgToPlatform(
        new ExecuteResult("test-stmt", new MString("stub", "").asInstanceOf[Message], "client-stub"),
        "worker1234"
      )
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[SendMsgToPlatform])

    val res = resRAW.asInstanceOf[SendMsgToPlatform]

    assertEquals(res.workerIdentity(), "worker1234")

    val msgRAW = res.msg
    assert(msgRAW.isInstanceOf[ExecuteResult])
    val msg = msgRAW.asInstanceOf[ExecuteResult]
    assertEquals(msg.clientIdentity(), "client-stub")
    assert(msg.result.isInstanceOf[MString])
    assertEquals(msg.result.asInstanceOf[MString].value, "stub")
    assertEquals(msg.stmt, "test-stmt")
  }

  test("convert SetPlatformVar remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new SetPlatformVar("worker1234", "test-var-name", new MString("stub", ""), "stub-client")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[SetPlatformVar])

    val res = resRAW.asInstanceOf[SetPlatformVar]

    assertEquals(res.workerIdentity(), "worker1234")
    assertEquals(res.name, "test-var-name")
    assertEquals(res.clientIdentity(), "stub-client")

    val msgRAW = res.msg
    assert(msgRAW.isInstanceOf[MString])
    val msg = msgRAW.asInstanceOf[MString]
    assertEquals(msg.value, "stub")
  }

  test("convert SetPlatformVars remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new SetPlatformVars(
        "worker1234", {
          val m = new java.util.HashMap[String, Message]()
          m.put("test-var-name-1", new MBool(false))
          m.put("test-var-name-2", new MDouble(123.9))
          m.put("test-var-name-3", new MString("8.8", "\""))
          m.put("test-var-name-4", new MString("123.9", "'"))
          m
        },
        "stub-client"
      )
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[SetPlatformVars])

    val res = resRAW.asInstanceOf[SetPlatformVars]

    assertEquals(res.workerIdentity(), "worker1234")
    assertEquals(res.clientIdentity(), "stub-client")

    val vars = res.vars

    assert(vars.keySet().size() == 4)

    {
      val valueRAW = vars.get("test-var-name-1")
      assert(valueRAW != null)

      assert(valueRAW.isInstanceOf[MBool])
      val value = valueRAW.asInstanceOf[MBool]
      assert(!value.value)
    }

    {
      val valueRAW = vars.get("test-var-name-2")
      assert(valueRAW != null)

      assert(valueRAW.isInstanceOf[MDouble])
      val value = valueRAW.asInstanceOf[MDouble]
      assertEqualsDouble(Double.box(value.value), Double.box(123.9), Double.box(0.0001))
    }

    {
      val valueRAW = vars.get("test-var-name-3")
      assert(valueRAW != null)

      assert(valueRAW.isInstanceOf[MString])
      val value = valueRAW.asInstanceOf[MString]
      assertEquals(value.quoted(), "\"8.8\"")
    }

    {
      val valueRAW = vars.get("test-var-name-4")
      assert(valueRAW != null)

      assert(valueRAW.isInstanceOf[MString])
      val value = valueRAW.asInstanceOf[MString]
      assertEquals(value.quoted(), "'123.9'")
    }

  }

  test("convert WorkerFinished remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new WorkerFinished("worker1234")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[WorkerFinished])

    val res = resRAW.asInstanceOf[WorkerFinished]

    assertEquals(res.workerIdentity(), "worker1234")
  }

  test("convert DefinedFunctions remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new DefinedFunctions(Seq[String]("funcName1", "funcName2", "funcName3").toArray, "stub-client")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[DefinedFunctions])

    val res = resRAW.asInstanceOf[DefinedFunctions]

    assertEquals(res.clientIdentity(), "stub-client")

    val funcNames = res.arr

    assert(funcNames.length == 3)
    assertEquals(funcNames(0), "funcName1")
    assertEquals(funcNames(1), "funcName2")
    assertEquals(funcNames(2), "funcName3")
  }

  test("convert ExecutedFunctionResult remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new ExecutedFunctionResult("funcName", new MString("8.8", "\""), "stub-client")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[ExecutedFunctionResult])

    val res = resRAW.asInstanceOf[ExecutedFunctionResult]

    assertEquals(res.clientIdentity(), "stub-client")
    assertEquals(res.functionName, "funcName")

    {
      val valueRAW = res.msg
      assert(valueRAW.isInstanceOf[MString])
      val value = valueRAW.asInstanceOf[MString]
      assertEquals(value.quoted(), "\"8.8\"")
    }
  }

  test("convert ExecutedFunctionResultFailed remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new ExecutedFunctionResultFailed("test-error-msg", "stub-client")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[ExecutedFunctionResultFailed])

    val res = resRAW.asInstanceOf[ExecutedFunctionResultFailed]

    assertEquals(res.clientIdentity(), "stub-client")
    assertEquals(res.getErrorMessage, "test-error-msg")

    assert(res.isInstanceOf[Error])
  }

  test("convert ExecuteResultFailed remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new ExecuteResultFailed("test-error-msg", "stub-client")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[ExecuteResultFailed])

    val res = resRAW.asInstanceOf[ExecuteResultFailed]

    assertEquals(res.clientIdentity(), "stub-client")
    assertEquals(res.getErrorMessage, "test-error-msg")

    assert(res.isInstanceOf[Error])
  }

  test("convert GetDefinedFunctionsError remote message to json and back") {

    val json = RemoteMessageConverter.toJson({
      new GetDefinedFunctionsError("test-error-msg", "stub-client")
    })

    assert(json.isInstanceOf[String])
    assert(isJson(json))

    val resRAW = RemoteMessageConverter.unpackAnyMsg(json)
    assert(resRAW.isInstanceOf[GetDefinedFunctionsError])

    val res = resRAW.asInstanceOf[GetDefinedFunctionsError]

    assertEquals(res.clientIdentity(), "stub-client")
    assertEquals(res.getErrorMessage, "test-error-msg")

    assert(res.isInstanceOf[Error])
  }

}
