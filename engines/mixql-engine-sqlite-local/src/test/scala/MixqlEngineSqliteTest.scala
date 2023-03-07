import org.mixql.engine.sqlite.local.SQLightJDBC
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.mixql.core.context.gtype

object MixqlEngineSqliteTest:
  var context: SQLightJDBC = null
  val identity = "MixqlEngineSqliteTest"

class MixqlEngineSqliteTest extends AnyFlatSpec with BeforeAndAfterAll:
  import MixqlEngineSqliteTest._
  override def beforeAll(): Unit =
    context = SQLightJDBC(identity)
    super.beforeAll()

  def execute(code: String): gtype.Type =
    context.execute(code)

  override def afterAll(): Unit =
    context.close()
    super.afterAll()
