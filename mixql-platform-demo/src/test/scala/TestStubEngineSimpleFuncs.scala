class TestStubEngineSimpleFuncs extends MixQLClusterTest {

  behavior of "correctly execute stub-local engine's functions, pass parameters to them and work with result"

  it should("execute stub_simple_proc function") in {
    run(
      """
        |print("stub_simple_proc res: " || stub_simple_proc());
        |""".stripMargin)
  }

  it should ("execute stub_simple_proc_params function") in {
    run(
      """
        |let a = "test";
        |let b = 5;
        |print(stub_simple_proc_params($a, $b));
        |""".stripMargin)
  }

  it should ("execute stub_simple_proc_context_params function") in {
    run(
      """
        |let a = "test";
        |let b = 5;
        |print(stub_simple_proc_context_params($a, $b));
        |""".stripMargin)
  }

}
