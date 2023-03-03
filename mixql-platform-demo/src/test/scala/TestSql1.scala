class TestSql1 extends MixQLClusterTest {

  behavior of "test of create table, insert table and print select from table"

  it should("create table, insert table and print select from table") in {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_sql1.sql").get))
  }
}
