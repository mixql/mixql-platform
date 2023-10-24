class TestEngineServiceFail extends MixQLClusterTest {

  test("should fail if engine started and then failed without notification of platform") {
    interceptMessage[java.lang.Exception]("Broker: elapsed timeout for engine mixql-engine-dummy") {
      run("""
          |let engine engine-fail;
          |
          |print("launch command on first time on new instance of engine-fail");
          |
          |INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)
          |VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21', 'Stavanger', '4006', 'Norway');
          |
          |print("ask engine-fail to fail with runtime exception");
          |fail_on_next_cmd_whith_runtime_exception;
          |
          |INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)
          |VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21', 'Stavanger', '4006', 'Norway');
          |
          |""".stripMargin)
    }
  }
}
