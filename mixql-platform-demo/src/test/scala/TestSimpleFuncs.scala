import org.mixql.platform.demo.utils.FilesOperations

class TestSimpleFuncs extends MixQLClusterTest {

  behavior of "correctly launch platform demo functions, pass parameters to it and work with result"

  it should("execute simple_func with passed parameter a") in {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_simple_func.sql").get))
  }

  it should("execute print_current_vars") in {
    run(
      """
        |print_current_vars();
        |print(concat("a", "b"));
        |""".stripMargin)
  }

  it should ("execute get_engines_list and work with returned array") in {
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_get_engines_list.sql").get))
  }

}
