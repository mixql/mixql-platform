import org.mixql.engine.sqlite.local.SQLightJDBC

class TestSqliteSakilaDB extends MixqlEngineSqliteTest(Some("mixql.org.engine.sqlight.sakila-db.path")) {

  import org.mixql.core.context.gtype

  it should ("select rating and count from film") in {
      val gType = execute(
        """
          |SELECT rating AS Rating, COUNT(title) AS Count
          |        FROM film
          |        GROUP BY rating
          |        ORDER BY Count DESC;
      """.stripMargin
      )
      assert(
        gType.toString == "[[\"PG-13\", 223], [\"NC-17\", 210], [\"R\", 195], [\"PG\", 194], [\"G\", 178]]"
      )
  }

}
