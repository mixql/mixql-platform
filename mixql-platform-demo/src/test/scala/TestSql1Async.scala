class TestSql1Async extends MixQLClusterTest {

  test("create table, insert table and print select from table  asynchronously") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_sql1_async.sql").get))
  }
}
