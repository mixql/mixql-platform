class TestStubEngineSimpleFuncs extends MixQLClusterTest {

  behavior of "correctly execute stub engine's functions, pass parameters to them and work with result"

  it should("execute stub_simple_proc function") in {
    run(
      """
        |let engine "stub";
        |print("stub_simple_proc res: " || stub_simple_proc());
        |let engine "stub-local";
        |""".stripMargin)
  }

  it should ("execute stub_simple_proc_params function") in {
    run(
      """
        |let engine "stub";
        |let a = "test";
        |let b = 5;
        |print(stub_simple_proc_params($a, $b));
        |let engine "stub-local";
        |""".stripMargin)
  }

  it should ("execute stub_simple_proc_context_params function") in {
    run(
      """
        |let engine "stub";
        |let a = "test";
        |let b = 5;
        |print(stub_simple_proc_context_params($a, $b));
        |let engine "stub-local";
        |""".stripMargin)
  }

  it should ("execute stub_simple_func_return_arr function") in {
    run(
      """
        |let engine "stub";
        |print("SUCCESS:" || stub_simple_func_return_arr());
        |let engine "stub-local";
        |""".stripMargin)
  }

  it should ("execute stub_simple_func_return_map function") in {
    run(
      """
        |let engine "stub";
        |
        |let res = stub_simple_func_return_map();
        |print("SUCCESS:" || $res);
        |
        |let customer3 = $res["3"];
        |print("customer's name of user with id 3: " || $customer3["CustomerName"]);
        |print("customer's name of user with id 3: " || $res["3"]["CustomerName"]);
        |--does not work!!!
        |--print("customer's name of user with id 3: " || $res.3.CustomerName);
        |
        |let engine "stub-local";
        |""".stripMargin)
  }
}
