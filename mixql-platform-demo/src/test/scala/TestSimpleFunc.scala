class TestSimpleFunc extends MixQLClusterTest {

  behavior of "correctly launch simple_func, pass parameters to it and print result"

  it should("execute simple_func with passed parameter a") in {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_simple_func.sql").get))
  }
}
