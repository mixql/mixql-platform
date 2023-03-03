import org.mixql.cluster.IExecutor
import org.mixql.engine.sqlite.MixQlEngineSqlight

import scala.concurrent.Future
object MixQlEngineSqliteExecutor extends IExecutor {
  override def start(identity: String, host: String, backendPort: String): Future[Unit] = {
    import concurrent.ExecutionContext.Implicits.global
    Future{
      MixQlEngineSqlight.main(
        Seq(
          "--port", backendPort,
          "--host", host,
          "--identity", identity
        ).toArray
      )
    }
  }
}
