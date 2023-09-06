import org.mixql.platform.demo.utils.FilesOperations
import org.scalatest.flatspec.AnyFlatSpec

class TestSimpleFuncs extends MixQLClusterTest {

  test("execute simple_func with passed parameter a") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_simple_func.sql").get))
  }

  test("execute print_current_vars") {
    run("""
        |print_current_vars();
        |print(concat("a", "b"));
        |""".stripMargin)
  }

  test("execute startsWith") {
    run("""
        |
        |print(startsWith("adddd", "b"));
        |""".stripMargin)
  }

  test("execute get_engines_list and work with returned array") {
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_get_engines_list.sql").get))
  }

}
