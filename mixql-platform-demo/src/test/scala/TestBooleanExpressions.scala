import com.typesafe.config.ConfigFactory
import munit.FunSuite
import org.mixql.cluster.ClientModule
import org.mixql.core.context.{Context, mtype}
import org.mixql.core.context.mtype.MType
import org.mixql.core.engine.Engine
import org.mixql.engine.sqlite.local.EngineSqlightLocal
import org.mixql.engine.stub.local.EngineStubLocal
import org.mixql.platform.demo.logger.{logDebug, logInfo}
import org.mixql.platform.demo.procedures.SimpleFuncs
import org.mixql.platform.demo.utils.FilesOperations
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable

class TestBooleanExpressions extends MixQLClusterTest {

  test("work correctly boolean expressions") {
    import org.mixql.platform.demo.utils.FilesOperations
    run(FilesOperations.readFileContent(TestOps.getFileFromResource("test_create_table_concat.sql").get))
  }
}
