import org.mixql.remote.messages.Message
class TestExecution extends MixqlEngineSqliteTest {
  import MixqlEngineSqliteTest.logger._
  behavior of "start engine, execute sql statements and close engine"

  it should ("execute statements that create table, insert value into it and" +
    " select values from table") in {
    import org.mixql.core.context.gtype
    import org.mixql.remote.GtypeConverter

    {
      logInfo(
        MixqlEngineSqliteTest.identity + ": execute create table customers"
      )
      val gType = execute(
        TestOps
          .readContentFromResource("TestExecution/create_table_customers.sql")
      )
      logInfo(
        MixqlEngineSqliteTest.identity + " create table res : " + gType.toString
      )
      assert(gtype.isNull(gType))
    }

    {
      logInfo(
        MixqlEngineSqliteTest.identity + ": execute insert into customers"
      )
      val gType = execute(
        TestOps
          .readContentFromResource("TestExecution/insert_into_customers.sql")
      )
      logInfo(
        MixqlEngineSqliteTest.identity + " insert into res : " + gType.toString
      )
      assert(gtype.isNull(gType))
    }

    {
      val code =
        """
          |select * from Customers;
          """.stripMargin
      logInfo(
        MixqlEngineSqliteTest.identity + ": execute select from customers"
      )
      val gType = execute(code)
      logInfo(
        MixqlEngineSqliteTest.identity + " select from customers res : " + gType.toString
      )
      assert(
        gType.toString == "[[\"Cardinal\", \"Tom B. Erichsen\", \"Skagen 21\", \"Stavanger\", 4006, \"Norway\"]]"
      )
    }

    {
      val gType = execute(
        """
          |select ContactName from Customers;
  """.stripMargin
      )
      assert(
        gType.toString == "[[\"Tom B. Erichsen\"]]"
      )
    }

    {
      val gType = execute(
        """
          |select count(*) from Customers;
  """.stripMargin
      )
      assert(
        gType.toString == "[[1]]"
      )
    }
  }
}
