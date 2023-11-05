import org.mixql.core.exception.MException

import scala.concurrent.duration.Duration

class TestEngineServiceFail extends MixQLClusterTest {
  override val munitTimeout: Duration = Duration(100, "s")
  override val timeoutRun: Long = 100000

  test("should fail if engine started and then failed without notification of platform") {
    interceptMessage[java.lang.Exception]("Broker: elapsed timeout for engine mixql-engine-fail") {
      run("""
          |let engine engine-fail;
          |
          |print("launch command on first time on new instance of engine-fail");
          |
          |INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)
          |VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21', 'Stavanger', '4006', 'Norway');
          |
          |print("ask engine-fail to fail with runtime exception");
          |fail_on_next_cmd_whith_runtime_exception;
          |
          |INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)
          |VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21', 'Stavanger', '4006', 'Norway');
          |
          |""".stripMargin)
    }
  }

  test(
    "should return failed tasks if engine started and then failed without notification of platform during async execution"
  ) {
    run("""
          |let engine engine-fail;
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
          |  print("ask engine-fail to fail with runtime exception");
          |  fail_on_next_cmd_whith_runtime_exception;
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
          |let res1, res2, res3 = await_all($async1, $async2, $async3);
          |let engine "stub-local";
          |""".stripMargin)
    val res1 = context().getVar("res1")
    assert(res1.isInstanceOf[MException])
    assert(res1.asInstanceOf[MException].getMessage == "Broker: elapsed timeout for engine mixql-engine-fail")
    val res2 = context().getVar("res2")
    assert(res2.isInstanceOf[MException])
    assert(res2.asInstanceOf[MException].getMessage == "Broker: elapsed timeout for engine mixql-engine-fail")
    val res3 = context().getVar("res3")
    assert(res3.isInstanceOf[MException])
    assert(res3.asInstanceOf[MException].getMessage == "Broker: elapsed timeout for engine mixql-engine-fail")
  }
}
