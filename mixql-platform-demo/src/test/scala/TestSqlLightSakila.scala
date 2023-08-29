class TestSqlLightSakila extends MixQLClusterTest {

  test("select rating and count from film table") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("mixql-test-sakila-rating.sql").get))
  }
}
