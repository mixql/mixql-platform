class TestSql1TestSql1 extends MixQLClusterTest {

  test("create table, insert table and print select from table") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_sql1.sql").get))
  }
}
