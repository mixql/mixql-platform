import org.mixql.platform.demo.utils.FilesOperations
import org.scalatest.flatspec.AnyFlatSpec

class TestCorrectReloadOfEngine extends MixQLClusterTest {

  test("successfully reload remote engine") {
    run("""
        |let engine stub;
        |
        |print("launch command on first time on new instance of stub")
        |
        |INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)
        |VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21', 'Stavanger', '4006', 'Norway');
        |
        |print("close stub engine");
        |closeEngine("stub");
        |
        |print("launch command on new instance of stub")
        |INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)
        |VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21', 'Stavanger', '4006', 'Norway');
        |""".stripMargin)
  }
}
