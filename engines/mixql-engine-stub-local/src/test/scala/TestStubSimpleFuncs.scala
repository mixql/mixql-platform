import org.scalatest.funsuite.AnyFunSuite
import org.mixql.engine.stub.local.{EngineStubLocal, StubContext}
import org.mixql.core.context.{Context, EngineContext, gtype}
import scala.collection.mutable

class TestStubSimpleFuncs extends AnyFunSuite {
  val engine = EngineStubLocal

  test("Invoke stub_simple_proc function") {
    val res = gtype.unpack(
      engine.executeFunc(
        "stub_simple_proc",
        null, {
          val kwargs: Map[String, Object] = Map.empty
          kwargs
        }
      )
    )
    assert(res == "SUCCESS")
  }

  test("Invoke stub_simple_proc_params function") {
    val a = "test"
    val b = 5

    val res = {
      gtype.unpack(
        engine.executeFunc(
          "stub_simple_proc_params",
          new EngineContext(
            Context(mutable.Map("stub-local" -> engine), "stub-local", mutable.Map(), mutable.Map()),
            "stub-local"
          ), {
            val kwargs: Map[String, Object] = Map.empty
            kwargs
          },
          gtype.pack(a),
          gtype.pack(b)
        )
      )
    }
    assert(res == s"SUCCESS:$a:${b.toString}")
  }

  test("Invoke stub_simple_proc_context_params function") {
    val a = "test"
    val b = 5

    val ctx = StubContext()

    val res = {
      gtype.unpack({
        engine.executeFunc(
          "stub_simple_proc_context_params",
          new EngineContext(
            Context(mutable.Map("stub-local" -> engine), "stub-local", mutable.Map(), mutable.Map()),
            "stub-local"
          ), {
            val kwargs: Map[String, Object] = Map.empty
            kwargs
          },
          gtype.pack(a),
          gtype.pack(b)
        )
      })
    }
    assert(res == s"SUCCESS:${ctx.name}:$a:${b.toString}")
  }
}
