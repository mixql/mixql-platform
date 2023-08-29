class TestSqlLightTitanic extends MixQLClusterTest {

  test("select count(who) from Observation") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("mixql-test-titanic-count.sql").get))
  }

  test("count Survival_rate from Observatione") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("mixql-test-titanic-rate.sql").get))
  }
}
