class TestSqlChangeRemoteLocalEngines extends MixQLClusterTest {

  behavior of "test changing local and remote engines"

  it should("change local and remote engines and stash parameters for remote engines") in {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_array_in_array_2.sql").get))
  }
}
