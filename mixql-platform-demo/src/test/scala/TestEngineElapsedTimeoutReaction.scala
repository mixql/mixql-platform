import org.mixql.core.exception.MException

class TestEngineElapsedTimeoutReaction extends MixQLClusterTest {
  import scala.concurrent.duration.Duration
  override val munitTimeout: Duration = Duration(100, "s")
  override val timeoutRun: Long = 100000

  test("if engine did not started and timeout elapsed, then send error message to clients of engine") {
    import org.mixql.platform.demo.utils.FilesOperations
    interceptMessage[java.lang.Exception]("Broker: elapsed timeout for engine mixql-engine-dummy") {
      run("""
          |let engine dummy;
          |
          |execute dummy command on dummy engine;
          |""".stripMargin)
    }
  }

  test("should return failed tasks if if engine did not started and timeout elapsed during async execution") {
    run("""
        |let engine dummy;
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
        |let res1, res2, res3 = await_all($async1, $async2, $async3);
        |let engine "stub-local";
        |""".stripMargin)
    val res1 = context().getVar("res1")
    assert(res1.isInstanceOf[MException])
    assert(res1.asInstanceOf[MException].getMessage == "Broker: elapsed timeout for engine mixql-engine-dummy")
    val res2 = context().getVar("res2")
    assert(res2.isInstanceOf[MException])
    assert(res2.asInstanceOf[MException].getMessage == "Broker: elapsed timeout for engine mixql-engine-dummy")
    val res3 = context().getVar("res3")
    assert(res3.isInstanceOf[MException])
    assert(res3.asInstanceOf[MException].getMessage == "Broker: elapsed timeout for engine mixql-engine-dummy")
  }

  test("execute statements sequentially and no timeout error must appear") {
    run("""
        |let engine "stub-5sec";
        |
        |seq1 statement1;
        |seq1 statement2;
        |seq1 statement3;
        |seq1 statement4;
        |seq1 statement5;
        |seq1 statement6;
        |
        |
        |
        |
        |seq2 statement1;
        |seq2 statement2;
        |seq2 statement3;
        |seq2 statement4;
        |seq2 statement5;
        |seq2 statement6;
        |
        |
        |
        |seq3 statement1;
        |seq3 statement2;
        |seq3 statement3;
        |seq3 statement4;
        |seq3 statement5;
        |seq3 statement6;
        |
        |let engine "stub-local";
        |""".stripMargin)
  }
}
