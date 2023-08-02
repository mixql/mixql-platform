class TestSqlCreateTableFor extends MixQLClusterTest {

  behavior of "test of create table in for loop"

  it should ("create table in for loop") in {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_create_table_for.sql").get))
  }
}
