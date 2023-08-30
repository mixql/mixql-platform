import com.typesafe.config.ConfigFactory
import munit.FunSuite
import org.mixql.cluster.{BrokerModule, ClientModule}
import org.mixql.core.engine.Engine
import org.mixql.net.PortOperations
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.io.File
import scala.collection.mutable
import org.mixql.core.context.{Context, gtype}
import org.mixql.core.context.gtype.Type
import org.mixql.engine.sqlite.local.EngineSqlightLocal
import org.mixql.engine.stub.local.EngineStubLocal
import org.mixql.platform.demo.logger.{logDebug, logInfo}
import org.mixql.platform.demo.procedures.SimpleFuncs
import org.mixql.remote.messages.module.ShutDown
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.duration.Duration
import scala.language.postfixOps

trait MixQLClusterTest extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent._
  import scala.concurrent.duration._

  def runWithTimeout[T](timeoutMs: Long)(f: => T): Option[T] = {
    Some(Await.result(Future(f), timeoutMs milliseconds))
  }

  def runWithTimeout[T](timeoutMs: Long, default: T)(f: => T): T = {
    runWithTimeout(timeoutMs)(f).getOrElse(default)
  }

  override val munitTimeout: Duration = Duration(300, "s")

  val context: Fixture[Context] =
    new Fixture[Context]("context") {
      var ctx: Context = null

      def apply(): Context = ctx

      override def beforeEach(context: BeforeEach): Unit = {
        logInfo("Triggering beforeEach")
        val config = ConfigFactory.load()

        val engines = {
          logDebug(s"Mixql engine demo platform: initialising engines")
          mutable.Map[String, Engine](
            "stub" -> new ClientModule(
              // Name of client, is used for identification in broker,
              // must be unique
              "mixql-engine-stub-demo-platform",
              // Name of remote engine, is used for identification in broker,
              // must be unique
              "mixql-engine-stub",
              // will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
              // in base path
              None,
              Some(MixQlEngineStubExecutor),
              None,
              None,
              None,
              None
            ),
            "sqlite" -> new ClientModule(
              // Name of client, is used for identification in broker,
              // must be unique
              "mixql-engine-sqlite-demo-platform",
              // Name of remote engine, is used for identification in broker,
              // must be unique
              "mixql-engine-sqlite",
              // will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
              // in base path
              None,
              Some(MixQlEngineSqliteExecutor),
              None,
              None,
              None,
              None
            ),
            "stub-local" -> EngineStubLocal,
            "sqlite-local" -> EngineSqlightLocal(),
            "sqlite-local-titanic" -> EngineSqlightLocal(Some("mixql.org.engine.sqlight.titanic-db.path")),
            "sqlite-local-sakila" -> EngineSqlightLocal(Some("mixql.org.engine.sqlight.sakila-db.path"))
          )
        }

        val functions: collection.mutable.Map[String, Any] = collection.mutable.Map(
          "simple_func" -> SimpleFuncs.simple_func,
          "print_current_vars" -> SimpleFuncs.print_current_vars,
          "get_engines_list" -> SimpleFuncs.get_engines_list
        )

        val variables: mutable.Map[String, Type] = mutable.Map[String, Type](
          "mixql.org.engine.sqlight.titanic-db.path" -> gtype
            .string(config.getString("mixql.org.engine.sqlight.titanic-db.path")),
          "mixql.org.engine.sqlight.sakila-db.path" -> gtype
            .string(config.getString("mixql.org.engine.sqlight.sakila-db.path")),
          "mixql.org.engine.sqlight.db.path" -> gtype.string(config.getString("mixql.org.engine.sqlight.db.path"))
        )

        logInfo("beforeEach: creating context")

        ctx = {
          logDebug(s"Mixql engine demo platform: init Cluster context")
          new Context(engines, "stub-local", functionsInit = functions, variablesInit = variables)
        }
      }

      override def afterEach(context: AfterEach): Unit = {
        // Always gets called, even if test failed.
        logInfo("afterEach: triggering afterEach")
        logInfo("afterEach: sending shutdowns to clients")
        import scala.util.Try
        ctx.engines.values.foreach(e =>
          if (e.isInstanceOf[ClientModule]) {
            Try({
              val cl: ClientModule = e.asInstanceOf[ClientModule]
              logDebug(s"mixql core context: sending shutdwon to remote engine " + cl.name)
              cl.ShutDown()
            })
          }
        )
        logInfo("afterEach: close context")
        Try(ctx.close())
        logInfo("afterEach: close broker")
        Try({ if BrokerModule.wasStarted then BrokerModule.close() })
      }
    }

  override def munitFixtures: Seq[Fixture[Context]] = List(context)

  def run(code: String): Unit = {
    runWithTimeout(300000) {
      org.mixql.core.run(code, context())
    }
  }
}
