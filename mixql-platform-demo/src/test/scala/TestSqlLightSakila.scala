class TestSqlLightSakila extends MixQLClusterTest {

  behavior of "test of create table, insert table and print select from table"

  it should ("select rating and count from film table") in {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("mixql-test-sakila-rating.sql").get))
  }
}
