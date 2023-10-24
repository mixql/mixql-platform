import scala.concurrent.duration.Duration

class TestStubEngineSimpleFuncs extends MixQLClusterTest {

  override val munitTimeout: Duration = Duration(100, "s")
  override val timeoutRun: Long = 100000

  test("execute stub_simple_proc function") {
    run("""
        |let engine "stub";
        |print("stub_simple_proc res: " || stub_simple_proc());
        |let engine "stub-local";
        |""".stripMargin)
  }

  test("execute stub_simple_proc_params function") {
    run("""
        |let engine "stub";
        |let a = "test";
        |let b = 5;
        |print(stub_simple_proc_params($a, $b));
        |let engine "stub-local";
        |""".stripMargin)
  }

  test("execute stub_simple_proc_context_params function") {
    run("""
        |let engine "stub";
        |let a = "test";
        |let b = 5;
        |print(stub_simple_proc_context_params($a, $b));
        |let engine "stub-local";
        |""".stripMargin)
  }

  test("execute stub_simple_func_return_arr function") {
    run("""
        |let engine "stub";
        |print("SUCCESS:" || stub_simple_func_return_arr());
        |let engine "stub-local";
        |""".stripMargin)
  }

  test("execute execute_platform_func_in_stub_func function") {
    run("""
        |let engine "stub";
        |let res = execute_platform_func_in_stub_func("test_invoke_of_platform_base64_func_from_stub");
        |print("SUCCESS:" || $res);
        |let engine "stub-local";
        |""".stripMargin)
  }

  test("CLOSURE: execute execute_stub_func_using_platform_in_stub_func function") {
    run("""
        |let engine "stub";
        |let res = execute_stub_func_using_platform_in_stub_func(
        |     "test_execute_stub_func_using_platform_in_stub_func", 4);
        |print("SUCCESS:" || $res);
        |let engine "stub-local";
        |""".stripMargin)
  }

  test("execute stub_simple_func_return_map function") {
    run("""
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

  test("execute execute_stub_func_long_sleep function") {
    run("""
        |let engine "stub";
        |
        |let res = execute_stub_func_long_sleep();
        |print("SUCCESS:" || $res);
        |
        |""".stripMargin)
  }
}
