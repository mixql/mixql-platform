import com.google.gson.{GsonBuilder, JsonParser}
import org.mixql.core.context.gtype
import org.mixql.remote.RemoteMessageConverter
import org.mixql.remote.messages.Message
import org.mixql.remote.messages.`type`.gtype.{gArray, gString, map}
import org.mixql.remote.messages.client.InvokedPlatformFunctionResult
import org.mixql.remote.messages.module.toBroker.EnginePingHeartBeat

class RemoteMessageConverterToJson extends munit.FunSuite {

  def prettyPrintUsingGson(uglyJson: String): String = {
    val gson = new GsonBuilder().setPrettyPrinting.create
    val jsonElement = JsonParser.parseString(uglyJson)
    val prettyJsonString = gson.toJson(jsonElement)
    prettyJsonString
  }

  test("convert EnginePingHeartBeat remote message to json") {
    val res = prettyPrintUsingGson(RemoteMessageConverter.toJson(new EnginePingHeartBeat("stub")))
      .replace("\r\n", "\n")
    assert(res.isInstanceOf[String])
    assertEquals(
      res,
      """{
        |  "type": "org.mixql.remote.messages.module.toBroker.EnginePingHeartBeat",
        |  "engineName": "stub"
        |}""".stripMargin.replace("\r\n", "\n")
    )
  }

  test("convert InvokedPlatformFunctionResult remote message to json") {
    val m = new java.util.HashMap[Message, Message]()
    m.put(new gString("123.9", "'"), new org.mixql.remote.messages.`type`.gtype.Bool(false))
    m.put(new gString("8.8", "\""), new org.mixql.remote.messages.`type`.gtype.gDouble(123.9))

    val res = prettyPrintUsingGson(
      RemoteMessageConverter.toJson(
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
    ).replace("\r\n", "\n")
    assert(res.isInstanceOf[String])
    assertEquals(
      res,
      """{
        |  "result": {
        |    "arr": [
        |      {
        |        "quote": "",
        |        "type": "org.mixql.remote.messages.type.gtype.gString",
        |        "value": "test"
        |      },
        |      {
        |        "type": "org.mixql.remote.messages.type.gtype.map",
        |        "map": [
        |          {
        |            "value": {
        |              "type": "org.mixql.remote.messages.type.gtype.Bool",
        |              "value": "false"
        |            },
        |            "key": {
        |              "quote": "\u0027",
        |              "type": "org.mixql.remote.messages.type.gtype.gString",
        |              "value": "123.9"
        |            }
        |          },
        |          {
        |            "value": {
        |              "type": "org.mixql.remote.messages.type.gtype.gDouble",
        |              "value": "123.9"
        |            },
        |            "key": {
        |              "quote": "\"",
        |              "type": "org.mixql.remote.messages.type.gtype.gString",
        |              "value": "8.8"
        |            }
        |          }
        |        ]
        |      },
        |      {
        |        "arr": [
        |          {
        |            "type": "org.mixql.remote.messages.type.gtype.map",
        |            "map": [
        |              {
        |                "value": {
        |                  "type": "org.mixql.remote.messages.type.gtype.Bool",
        |                  "value": "false"
        |                },
        |                "key": {
        |                  "quote": "\u0027",
        |                  "type": "org.mixql.remote.messages.type.gtype.gString",
        |                  "value": "123.9"
        |                }
        |              },
        |              {
        |                "value": {
        |                  "type": "org.mixql.remote.messages.type.gtype.gDouble",
        |                  "value": "123.9"
        |                },
        |                "key": {
        |                  "quote": "\"",
        |                  "type": "org.mixql.remote.messages.type.gtype.gString",
        |                  "value": "8.8"
        |                }
        |              }
        |            ]
        |          }
        |        ],
        |        "type": "org.mixql.remote.messages.type.gtype.gArray"
        |      }
        |    ],
        |    "type": "org.mixql.remote.messages.type.gtype.gArray"
        |  },
        |  "name": "test_func",
        |  "type": "org.mixql.remote.messages.client.InvokedPlatformFunctionResult",
        |  "worker": "worker1234566",
        |  "moduleIdentity": "stub-engine",
        |  "clientIdentity": "stub"
        |}""".stripMargin.replace("\r\n", "\n")
    )
  }
}
