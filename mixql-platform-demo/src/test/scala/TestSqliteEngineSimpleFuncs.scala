class TestSqliteEngineSimpleFuncs extends MixQLClusterTest {

  test("execute sqlite_simple_proc function") {
    run("""
        |let engine "sqlite";
        |print("sqlite_simple_proc res: " || sqlite_simple_proc());
        |""".stripMargin)
  }

  test("execute sqlite_simple_proc_params function") {
    run("""
        |let engine "sqlite";
        |let a = "test";
        |let b = 5;
        |print(sqlite_simple_proc_params($a, $b));
        |""".stripMargin)
  }

  test("execute sqlite_simple_proc_context_params function") {
    run("""
        |let engine "sqlite";
        |let a = "test";
        |let b = 5;
        |print(sqlite_simple_proc_context_params($a, $b));
        |""".stripMargin)
  }
}
