import org.mixql.engine.sqlite.SQLightJDBC
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.mixql.core.context.gtype

import scala.collection.mutable
import org.mixql.protobuf.messages.clientMsgs

object MixqlEngineSqliteTest:
  var context: SQLightJDBC = null
  val identity = "MixqlEngineSqliteTest"
  val engineParams: mutable.Map[String, scalapb.GeneratedMessage] =
    mutable.Map(
      "mixql.org.engine.sqlight.db.path" -> clientMsgs.String("jdbc:sqlite::memory:")
    )

class MixqlEngineSqliteTest extends AnyFlatSpec with BeforeAndAfterAll:
  import MixqlEngineSqliteTest._
  override def beforeAll(): Unit =
    context = SQLightJDBC(identity, engineParams)
    super.beforeAll()

  def execute(code: String): gtype.Type =
    import org.mixql.protobuf.GtypeConverter

    val res = context.execute(code)
    GtypeConverter.toGtype(res)

  override def afterAll(): Unit =
    context.close()
    super.afterAll()
