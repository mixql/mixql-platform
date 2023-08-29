class TestSqlArrayInArray extends MixQLClusterTest {

  test("execute test array in array index") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_array_in_array.sql").get))
  }

  test("execute for in select from array in array") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_array_in_array_for_in_select.sql").get))
  }
}
