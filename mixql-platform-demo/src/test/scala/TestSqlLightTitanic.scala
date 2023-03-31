class TestSqlLightTitanic extends MixQLClusterTest {

  behavior of "execute sql on sqlite titanic database"

  it should("select count(who) from Observation") in {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("mixql-test-titanic-count.sql").get))
  }

  it should("count Survival_rate from Observatione") in {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("mixql-test-titanic-rate.sql").get))
  }
}
