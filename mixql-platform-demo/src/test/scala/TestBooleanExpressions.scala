import org.mixql.platform.demo.utils.FilesOperations

class TestBooleanExpressions extends MixQLClusterTest {

  behavior of "correctly execute boolean expressions"

  it should ("work correctly boolean expressions") in {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_create_table_concat.sql").get))
  }
}
