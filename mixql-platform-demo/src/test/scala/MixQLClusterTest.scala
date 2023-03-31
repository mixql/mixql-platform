import org.mixql.cluster.{BrokerModule, ClientModule}
import org.mixql.core.engine.Engine
import org.mixql.net.PortOperations
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.BeforeAndAfterAll

import java.io.File
import scala.collection.mutable
import org.mixql.core.context.Context
import org.mixql.engine.sqlite.local.EngineSqlightLocal
import org.mixql.engine.stub.local.EngineStubLocal
import org.mixql.platform.demo.logger.logDebug
import org.mixql.platform.demo.procedures.SimpleFuncs
import org.mixql.protobuf.messages.clientMsgs.ShutDown

object MixQLClusterTest{
  val engines = {
    logDebug(s"Mixql engine demo platform: initialising engines")
    mutable.Map[String, Engine](
      "stub" -> new ClientModule(
      //Name of client, is used for identification in broker,
      //must be unique
      "mixql-engine-stub-demo-platform",
      //Name of remote engine, is used for identification in broker,
      //must be unique
      "mixql-engine-stub",
      //will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
      //in base path
      None,
      Some(MixQlEngineStubExecutor),
      None, None, None, None
    ),
      "sqlite" -> new ClientModule(
        //Name of client, is used for identification in broker,
        //must be unique
        "mixql-engine-sqlite-demo-platform",
        //Name of remote engine, is used for identification in broker,
        //must be unique
        "mixql-engine-sqlite",
        //will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
        //in base path
        None,
        Some(MixQlEngineSqliteExecutor),
        None, None, None, None
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
    "get_engines_list" -> SimpleFuncs.get_engines_list,
  )


  val context = {
    logDebug(s"Mixql engine demo platform: init Cluster context")
    new Context(engines, "stub-local", functions = functions)
  }
}
class MixQLClusterTest extends AnyFlatSpec with BeforeAndAfterAll {
  import MixQLClusterTest._


//  override def beforeAll(): Unit =
//    super.beforeAll()

  def run(code: String): Unit = {
    org.mixql.core.run(code, context)
  }

  override def afterAll(): Unit = {
    context.engines.values.foreach(
      e => if (e.isInstanceOf[ClientModule]) {
        val cl: ClientModule = e.asInstanceOf[ClientModule]
        logDebug(s"mixql core context: sending shutdwon to remote engine " + cl.name)
        cl.ShutDown()
      }
    )
    context.close()
    if ClientModule.broker != null then ClientModule.broker.close()
    super.afterAll()
  }
}
