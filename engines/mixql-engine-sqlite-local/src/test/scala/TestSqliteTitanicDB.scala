import org.mixql.engine.sqlite.local.SQLightJDBC

class TestSqliteTitanicDB extends MixqlEngineSqliteTest(Some("mixql.org.engine.sqlight.titanic-db.path")) {

  import org.mixql.core.context.gtype

  it should ("select count(who) from Observation") in {
      val gType = execute(
        """
          |select  count(who) from Observation t1 join who t2 on t1.who_id = t2.who_id;
      """.stripMargin
      )
      assert(
        gType.toString == "[[891]]"
      )
  }

  it should ("count Survival_rate from Observation") in {
      val gType = execute(
        """
          |select
          |    case when f.survived = 1 then 'Survived' else 'Not Survived' end as Survival_status,
          |    count(*) as Survival_rate,
          |    printf("%.2f", 100.0 * count(*) / max(f.total_passeng)) || " %" as Percent,
          |    max(f.total_passeng) as Total_passengers
          |from
          |    (
          |        select  count(*) over() as total_passeng,
          |                t.*
          |        from Observation t
          |    ) f
          |group by f.alive_id;
      """.stripMargin
      )
      assert(
        gType.toString == "[[\"Not Survived\", 549, \"61.62 %\", 891], [\"Survived\", 342, \"38.38 %\", 891]]"
      )
  }
}
