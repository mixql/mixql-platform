import scala.concurrent.duration.Duration

class TestStubEngineAsync extends MixQLClusterTest {

//  override val munitTimeout: Duration = Duration(100, "s")
//  override val timeoutRun: Long = 100000

  test("execute 3 tasks in parallel and wait them all in the end") {
    run("""
        |let engine "stub";
        |let async1 = async
        |  async1 statement1;
        |  async1 statement2;
        |  async1 statement3;
        |  async1 statement4;
        |  async1 statement5;
        |  async1 statement6;
        |  return none;
        |end async;
        |
        |let async2 = async
        |  async2 statement1;
        |  async2 statement2;
        |  async2 statement3;
        |  async2 statement4;
        |  async2 statement5;
        |  async2 statement6;
        |end async;
        |
        |let async3 = async
        |  async3 statement1;
        |  async3 statement2;
        |  async3 statement3;
        |  async3 statement4;
        |  async3 statement5;
        |  async3 statement6;
        |end async;
        |
        |let res = await_all($async1, $async2, $async3);
        |let engine "stub-local";
        |""".stripMargin)
  }

  test("execute 3 tasks in parallel but with closure") {
    run("""
        |let engine "stub";
        |let async1 = async
        |  async1 statement1;
        |  async1 statement2;
        |  let res = execute_stub_func_using_platform_in_stub_func(
        |     "test_execute_stub_func_using_platform_in_stub_func", 4);
        |  print("SUCCESS:async1:" || $res);
        |  async1 statement3;
        |  async1 statement4;
        |  async1 statement5;
        |  async1 statement6;
        |  return none;
        |end async;
        |
        |let async2 = async
        |  async2 statement1;
        |  async2 statement2;
        |  async2 statement3;
        |  async2 statement4;
        |  async2 statement5;
        |  let res = execute_stub_func_using_platform_in_stub_func(
        |     "test_execute_stub_func_using_platform_in_stub_func", 4);
        |  print("SUCCESS:async2:" || $res);
        |  async2 statement6;
        |end async;
        |
        |let async3 = async
        |  async3 statement1;
        |  async3 statement2;
        |  async3 statement3;
        |  let res = execute_stub_func_using_platform_in_stub_func(
        |     "test_execute_stub_func_using_platform_in_stub_func", 4);
        |  print("SUCCESS:async3:" || $res);
        |  async3 statement4;
        |  async3 statement5;
        |  async3 statement6;
        |  return 1;
        |end async;
        |
        |print("main1:$res");
        |let res = await_all($async1, $async2, $async3);
        |print("main2:$res");
        |let engine "stub-local";
        |""".stripMargin)
  }

  test("execute 3 tasks in parallel and wait them all in the end One of them closes engine") {
    run("""
        |let engine "stub";
        |let async1 = async
        |  async1 statement1;
        |  async1 statement2;
        |  async1 statement3;
        |  async1 statement4;
        |  async1 statement5;
        |  async1 statement6;
        |  return none;
        |end async;
        |
        |let async2 = async
        |  async2 statement1;
        |  async2 statement2;
        |  async2 statement3;
        |  closeEngine("stub");
        |  async2 statement5;
        |  async2 statement6;
        |end async;
        |
        |let async3 = async
        |  async3 statement1;
        |  async3 statement2;
        |  async3 statement3;
        |  async3 statement4;
        |  async3 statement5;
        |  async3 statement6;
        |end async;
        |
        |let res = await_all($async1, $async2, $async3);
        |print("res: " + $res);
        |let engine "stub-local";
        |""".stripMargin)
  }

}
