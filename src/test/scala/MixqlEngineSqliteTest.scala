import org.mixql.engine.sqlite.SQLightJDBC
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec

object MixqlEngineSqliteTest:
  var context: SQLightJDBC = null
  val identity = "MixqlEngineSqliteTest"


class MixqlEngineSqliteTest extends AnyFlatSpec with BeforeAndAfterAll:
  import MixqlEngineSqliteTest._
  override def beforeAll(): Unit =
    context = SQLightJDBC(identity)
    super.beforeAll()

  def execute(stmt: String): scalapb.GeneratedMessage =
    context.execute(stmt)

  override def afterAll(): Unit =
    context.close()
    super.afterAll()
