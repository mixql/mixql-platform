class TestSqliteEngineSimpleFuncs extends MixQLClusterTest {

  behavior of "correctly execute remote sqlite engine's functions, pass parameters to them and work with result"

  it should("execute sqlite_simple_proc function") in {
    run(
      """
        |let engine "sqlite";
        |print("sqlite_simple_proc res: " || sqlite_simple_proc());
        |""".stripMargin)
  }

  it should ("execute sqlite_simple_proc_params function") in {
    run(
      """
        |let engine "sqlite";
        |let a = "test";
        |let b = 5;
        |print(sqlite_simple_proc_params($a, $b));
        |""".stripMargin)
  }

  it should ("execute sqlite_simple_proc_context_params function") in {
    run(
      """
        |let engine "sqlite";
        |let a = "test";
        |let b = 5;
        |print(sqlite_simple_proc_context_params($a, $b));
        |""".stripMargin)
  }
}
