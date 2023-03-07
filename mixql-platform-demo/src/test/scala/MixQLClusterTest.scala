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
import org.mixql.protobuf.messages.clientMsgs.ShutDown

object MixQLClusterTest{
  val engines = {
    println(s"Mixql engine demo platform: initialising engines")
    mutable.Map[String, Engine](
//      "stub" -> new ClientModule(
//      //Name of client, is used for identification in broker,
//      //must be unique
//      "mixql-engine-stub-demo-platform",
//      //Name of remote engine, is used for identification in broker,
//      //must be unique
//      "mixql-engine-stub",
//      //will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
//      //in base path
//      None,
//      Some(MixQlEngineStubExecutor),
//      None, None, None, None
//    ),
//      "sqlite" -> new ClientModule(
//        //Name of client, is used for identification in broker,
//        //must be unique
//        "mixql-engine-sqlite-demo-platform",
//        //Name of remote engine, is used for identification in broker,
//        //must be unique
//        "mixql-engine-sqlite",
//        //will be started mixql-engine-demo on linux or mixql-engine-demo.bat on windows
//        //in base path
//        None,
//        Some(MixQlEngineSqliteExecutor),
//        None, None, None, None
//      ),
      "stub-local" -> EngineStubLocal,
      "sqlite-local" -> EngineSqlightLocal
    )
  }


  val context = {
    println(s"Mixql engine demo platform: init Cluster context")
    new Context(engines, "stub-local")
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
        println(s"mixql core context: sending shutdwon to remote engine " + cl.name)
        cl.sendMsg(ShutDown())
      }
    )
    context.close()
    if ClientModule.broker != null then ClientModule.broker.close()
    super.afterAll()
  }
}
