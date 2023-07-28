import org.mixql.engine.sqlite.local.SQLightJDBC
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.mixql.core.context.gtype

import scala.collection.mutable

class MixqlEngineSqliteTest(dbPathParameter: Option[String] = None) extends AnyFlatSpec with BeforeAndAfter:
  var context: SQLightJDBC = null
  val identity = "MixqlEngineSqliteTest"
  val engineParams: mutable.Map[String, gtype.Type] = mutable.Map(
    "mixql.org.engine.sqlight.db.path" -> gtype.string("jdbc:sqlite::memory:"),
    "mixql.org.engine.sqlight.titanic-db.path" -> gtype.string("jdbc:sqlite:./samples/db/titanic.db"),
    "mixql.org.engine.sqlight.sakila-db.path" -> gtype.string("jdbc:sqlite:./samples/db/sakila.db")
  )

  before {
    context = SQLightJDBC(identity, engineParams, dbPathParameter)
  }

  def execute(code: String): gtype.Type = context.execute(code)

  after {
    context.close()
    SQLightJDBC.c = null
  }
