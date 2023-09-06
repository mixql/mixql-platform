class TestSqlChangeRemoteLocalEngines extends MixQLClusterTest {

  test("change local and remote engines and stash parameters for remote engines") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_array_in_array_2.sql").get))
  }
}
