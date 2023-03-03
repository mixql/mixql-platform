import org.mixql.cluster.IExecutor
import org.mixql.engine.demo.MixQlEngineDemo

import scala.concurrent.Future
object MixQlEngineStubExecutor extends IExecutor {
  override def start(identity: String, host: String, backendPort: String): Future[Unit] = {
    import concurrent.ExecutionContext.Implicits.global
    Future{
      MixQlEngineDemo.main(
        Seq(
          "--port", backendPort,
          "--host", host,
          "--identity", identity
        ).toArray
      )
    }
  }
}
