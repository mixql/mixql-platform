import org.mixql.core.context.mtype
import org.mixql.engine.sqlite.local.SQLightJDBC

class TestSqliteInMemoryDB extends MixqlEngineSqliteTest {

  import org.mixql.core.context.mtype._

  it should ("create table") in {
    val g = createTableCustomers()
    assert(mtype.isNull(g))
  }

  it should ("insert values in table") in {
    createTableCustomers()
    val gType = insertValuesIntoCustomers()
    assert(mtype.isNull(gType))
  }

  it should ("select all values from table") in {
    prepareCustomersTable()
    val gType = execute("""
          |select * from Customers;
      """.stripMargin)
    assert(gType.toString == "[[\"Cardinal\", \"Tom B. Erichsen\", \"Skagen 21\", \"Stavanger\", 4006, \"Norway\"]]")
  }

  it should ("select column from table") in {
    prepareCustomersTable()
    val gType = execute("""
          |select ContactName from Customers;
  """.stripMargin)
    assert(gType.toString == "[[\"Tom B. Erichsen\"]]")

  }

  it should ("count correctly number of lines in table") in {
    prepareCustomersTable()
    val gType = execute("""
          |select count(*) from Customers;
  """.stripMargin)
    assert(gType.toString == "[[1]]")
  }

  def createTableCustomers(): mtype.MType = {
    execute(TestOps.readContentFromResource("TestExecution/create_table_customers.sql"))
  }

  def insertValuesIntoCustomers() = {
    execute(TestOps.readContentFromResource("TestExecution/insert_into_customers.sql"))
  }

  def prepareCustomersTable() = {
    createTableCustomers()
    insertValuesIntoCustomers()
  }
}
