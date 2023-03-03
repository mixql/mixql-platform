class TestSqlArrayInArray extends MixQLClusterTest {

  behavior of "test array in array index"

  it should("execute test array in array index") in {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_array_in_array.sql").get))
  }

  it should ("execute for in select from array in array") in {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_array_in_array_for_in_select.sql").get))
  }
}
