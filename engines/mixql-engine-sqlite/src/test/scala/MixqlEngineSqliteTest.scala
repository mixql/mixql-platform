import org.mixql.core.context.gtype.Type
import org.mixql.engine.sqlite.SQLightJDBC
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.mixql.core.context.{EngineContext, gtype}
import org.mixql.engine.core.PlatformContext
import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.engine.test.PlatformContextTest

import scala.collection.mutable

object MixqlEngineSqliteTest:
  var context: SQLightJDBC = null
  val identity = "MixqlEngineSqliteTest"
  val engineParams: mutable.Map[String, Type] =
    mutable.Map(
      "mixql.org.engine.sqlight.db.path" -> gtype.string(
        "jdbc:sqlite::memory:", "")
    )
  val logger = new ModuleLogger(identity)

class MixqlEngineSqliteTest extends AnyFlatSpec with BeforeAndAfterAll:

  import MixqlEngineSqliteTest._

  override def beforeAll(): Unit =
    context = SQLightJDBC(identity, new PlatformContextTest(engineParams, identity))
    super.beforeAll()

  def execute(code: String): gtype.Type =
    import org.mixql.remote.GtypeConverter

    val res = context.execute(code)
    GtypeConverter.messageToGtype(res)

  override def afterAll(): Unit =
    context.close()
    super.afterAll()
