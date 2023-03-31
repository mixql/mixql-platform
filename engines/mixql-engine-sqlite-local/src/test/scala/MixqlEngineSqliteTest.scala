import org.mixql.engine.sqlite.local.SQLightJDBC
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.mixql.core.context.gtype

class MixqlEngineSqliteTest(dbPathParameter: Option[String] = None) extends AnyFlatSpec with BeforeAndAfter :
  var context: SQLightJDBC = null
  val identity = "MixqlEngineSqliteTest"

  before {
    context = SQLightJDBC(identity, dbPathParameter)
  }

  def execute(code: String): gtype.Type =
    context.execute(code)

  after {
    context.close()
    SQLightJDBC.c = null
  }
