import org.mixql.cluster.{BrokerModule, ClientModule}
import org.mixql.core.engine.Engine
import org.mixql.net.PortOperations
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.BeforeAndAfterAll

import java.io.File
import scala.collection.mutable
import org.mixql.core.context.Context
import org.mixql.protobuf.messages.clientMsgs.ShutDown

object MixQLClusterTest{
  val host = "0.0.0.0"
  val portFrontend = PortOperations.isPortAvailable(0)
  val portBackend = PortOperations.isPortAvailable(0)
  val broker = {
    println(s"Mixql engine demo platform: init broker messager with" +
      s" frontend port $portFrontend and backend port $portBackend on host $host")
    new BrokerModule(portFrontend, portBackend, host)
  }

  val engines = {
    println(s"Mixql engine demo platform: initialising engines")
    mutable.Map[String, Engine]("demo" -> new ClientModule(
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
      host, portFrontend, portBackend, new File(".")
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
        host, portFrontend, portBackend, new File(".")
      )
    )
  }


  val context = {
    println(s"Mixql engine demo platform: init Cluster context")
    new Context(engines, "demo")
  }
}
class MixQLClusterTest extends AnyFlatSpec with BeforeAndAfterAll {
  import MixQLClusterTest._


  override def beforeAll(): Unit =
    println(s"Mixql engine demo platform beforeAll: starting broker messager with" +
      s" frontend port $portFrontend and backend port $portBackend on host $host")
    broker.start()
    super.beforeAll()

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
    broker.close()
    super.afterAll()
  }
}
