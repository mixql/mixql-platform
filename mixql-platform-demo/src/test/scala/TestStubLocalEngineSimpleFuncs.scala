class TestStubLocalEngineSimpleFuncs extends MixQLClusterTest {

//  behavior of "correctly execute stub-local engine's functions, pass parameters to them and work with result"

  test("execute stub_simple_proc function") {
    run("""
        |print("stub_simple_proc res: " || stub_simple_proc());
        |""".stripMargin)
  }

  test("execute stub_simple_proc_params function") {
    run("""
        |let a = "test";
        |let b = 5;
        |print(stub_simple_proc_params($a, $b));
        |""".stripMargin)
  }

  test("execute stub_simple_proc_context_params function") {
    run("""
        |let a = "test";
        |let b = 5;
        |print(stub_simple_proc_context_params($a, $b));
        |""".stripMargin)
  }

}
