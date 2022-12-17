class TestExecution extends MixqlEngineSqliteTest {
  behavior of "start engine, execute sql statements and close engine"

  it should ("create table") in {
    val code =
      """
        |CREATE TABLE Customers (
        |                           CustomerName varchar(255),
        |                           ContactName varchar(255),
        |                           Address varchar(255),
        |                           City varchar(255),
        |                           PostalCode int,
        |                           Country varchar(255)
        |);
        """.stripMargin
    execute(code)
  }

  it should ("insert value in table") in {
    val code =
      """
        |INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)
        |VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21', 'Stavanger', '4006', 'Norway');
        """.stripMargin
    execute(code)
  }

  it should ("execute select and create array of array object converted to protobuf") in {
    val code =
      """
        |select * from Customers
        """.stripMargin
    val res = execute(code)
  }

}
