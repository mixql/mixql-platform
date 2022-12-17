import org.mixql.engine.sqlite.SQLightJDBC
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
    import org.mixql.protobuf.RemoteMsgsConverter

    val res = context.execute(code)
    RemoteMsgsConverter.toGtype(res)

  override def afterAll(): Unit =
    context.close()
    super.afterAll()
