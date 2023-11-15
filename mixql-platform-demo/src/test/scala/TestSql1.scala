class TestSql1TestSql1 extends MixQLClusterTest {

  test("create table, insert table and print select from table") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_sql1.sql").get))
  }

  test("send error to platform, and it should stop") {
    interceptMessage[java.lang.Exception](
      "Module mixql-engine-sqlite: SQLightJDBC error while execute: " +
        "[SQLITE_ERROR] SQL error or missing database (near \"rubish\": syntax error)"
    ) {
      run("""
          |let engine sqlite;
          |
          |rubish comand;
          |""".stripMargin)
    }
  }
}
