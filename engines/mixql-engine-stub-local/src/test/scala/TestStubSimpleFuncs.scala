import org.scalatest.funsuite.AnyFunSuite
import org.mixql.engine.stub.local.{EngineStubLocal, StubContext}
import org.mixql.core.context.gtype

class TestStubSimpleFuncs extends AnyFunSuite {
  val engine = EngineStubLocal

  test("Invoke stub_simple_proc function") {
    val res = gtype.unpack(engine._executeFunc("stub_simple_proc", null))
    assert(res == "SUCCESS")
  }

  test("Invoke stub_simple_proc_params function") {
    val a = "test"
    val b = 5

    val res = gtype.unpack(engine._executeFunc("stub_simple_proc_params", null, gtype.pack(a), gtype.pack(b)))
    assert(res == s"SUCCESS:$a:${b.toString}")
  }

  test("Invoke stub_simple_proc_context_params function") {
    val a = "test"
    val b = 5

    val ctx = StubContext()

    val res = gtype.unpack(engine._executeFunc("stub_simple_proc_context_params", null, gtype.pack(a), gtype.pack(b)))
    assert(res == s"SUCCESS:${ctx.name}:$a:${b.toString}")
  }
}
