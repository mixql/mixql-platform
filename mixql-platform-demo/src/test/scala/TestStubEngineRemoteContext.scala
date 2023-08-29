class TestStubEngineRemoteContext extends MixQLClusterTest {

  test("execute stub_simple_proc function") {
    run("""
        |let engine "stub";
        |
        |let a.a = "gf";
        |let a.b = "gf";
        |let a.c = "gf";
        |let a.d = "gf";
        |let a.e = "gf";
        |let a.f = "gf";
        |let a.g = "gf";
        |let a.h = "gf";
        |let a.i = "gf";
        |let a.j = "gf";
        |let a.k = "gf";
        |let a.l = "gf";
        |let a.m = "gf";
        |let a.n = "gf";
        |let a.o = "gf";
        |let a.p = "gf";
        |let a.q = "gf";
        |let a.r = "gf";
        |let a.s = "gf";
        |let a.t = "gf";
        |let a.u = "gf";
        |
        |let b.a = "gf";
        |let b.b = "gf";
        |let b.c = "gf";
        |let b.d = "gf";
        |let b.e = "gf";
        |let b.f = "gf";
        |let b.g = "gf";
        |let b.h = "gf";
        |let b.i = "gf";
        |let b.j = "gf";
        |let b.k = "gf";
        |let b.l = "gf";
        |let b.m = "gf";
        |let b.n = "gf";
        |let b.o = "gf";
        |let b.p = "gf";
        |let b.q = "gf";
        |let b.r = "gf";
        |let b.s = "gf";
        |let b.t = "gf";
        |let b.u = "gf";
        |print("stub_simple_proc_context res: " || stub_simple_proc_context());
        |let engine "stub-local";
        |""".stripMargin)
  }
}
