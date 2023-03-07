class TestExecution extends MixqlEngineSqliteTest {
  behavior of "start engine, execute sql statements and close engine"

  it should ("execute statements that create table, insert value into it and" +
    " select values from table") in {
    import org.mixql.core.context.gtype

    {
      println(
        MixqlEngineSqliteTest.identity + ": execute create table customers"
      )
      val gType = execute(
        TestOps
          .readContentFromResource("TestExecution/create_table_customers.sql")
      )
      println(
        MixqlEngineSqliteTest.identity + " create table res : " + gType.toString
      )
      assert(gType.isInstanceOf[gtype.Null.type])
    }

    {
      println(
        MixqlEngineSqliteTest.identity + ": execute insert into customers"
      )
      val gType = execute(
        TestOps
          .readContentFromResource("TestExecution/insert_into_customers.sql")
      )
      println(
        MixqlEngineSqliteTest.identity + " insert into res : " + gType.toString
      )
      assert(gType.isInstanceOf[gtype.Null.type])
    }

    {
      val code =
        """
          |select * from Customers;
          """.stripMargin
      println(
        MixqlEngineSqliteTest.identity + ": execute select from customers"
      )
      val gType = execute(code)
      println(
        MixqlEngineSqliteTest.identity + " select from customers res : " + gType.toString
      )
      assert(
        gType.toString == "[[\"Cardinal\", \"Tom B. Erichsen\", \"Skagen 21\", 0, \"4006\"]]"
      )
    }
  }
}
