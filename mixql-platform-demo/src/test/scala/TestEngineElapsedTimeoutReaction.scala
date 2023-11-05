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
