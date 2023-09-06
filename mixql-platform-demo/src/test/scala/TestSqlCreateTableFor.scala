class TestSqlCreateTableFor extends MixQLClusterTest {

  test("create table in for loop") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_create_table_for.sql").get))
  }
}
