import org.apache.logging.log4j.LogManager
import org.mixql.core.context.mtype.MType
import org.mixql.engine.sqlite.SQLightJDBC
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.mixql.core.context.{EngineContext, mtype}
import org.mixql.engine.core.PlatformContext
import org.mixql.engine.core.logger.ModuleLogger

import scala.collection.mutable

object MixqlEngineSqliteTest:
  var context: SQLightJDBC = null
  val identity = "MixqlEngineSqliteTest"

  val engineParams: mutable.Map[String, MType] = mutable
    .Map("mixql.org.engine.sqlight.db.path" -> mtype.MString("jdbc:sqlite::memory:", ""))
  val logger = new ModuleLogger(identity, LogManager.getRootLogger.getLevel.name())

class MixqlEngineSqliteTest extends AnyFlatSpec with BeforeAndAfterAll:

  import MixqlEngineSqliteTest._

  override def beforeAll(): Unit =
    context = SQLightJDBC(identity, new PlatformContextTest(engineParams, identity), logger)
    super.beforeAll()

  def execute(code: String): mtype.MType =
    import org.mixql.remote.GtypeConverter

    val res = context.execute(code)
    GtypeConverter.messageToGtype(res)

  override def afterAll(): Unit =
    context.close()
    super.afterAll()
