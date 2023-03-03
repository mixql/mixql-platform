// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html
class TestParsingUri extends munit.FunSuite {
  test("test parsing of uri") {
    val uri = "http://127.0.0.1:8080/org/mixql/engine-demo/mixql-engine-demo-0.1.0-SNAPSHOT.tar.gz"
    val expected = "mixql-engine-demo"

    val endPart = uri.split("""/""").last

    println("endPart: " + endPart)
    var name = """[A-Za-z\-]+""".r.findFirstIn(endPart).get
    if name.endsWith("-") then name = name.dropRight(1)

    assertEquals(name, expected)

    var version = """\d+\.\d+\.\d+(-SNAPSHOT)?""".r.findFirstIn(endPart).get
    assertEquals(version, "0.1.0-SNAPSHOT")
  }
}
