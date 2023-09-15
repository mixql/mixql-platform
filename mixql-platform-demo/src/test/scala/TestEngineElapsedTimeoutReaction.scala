class TestEngineElapsedTimeoutReaction extends MixQLClusterTest {

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
}
