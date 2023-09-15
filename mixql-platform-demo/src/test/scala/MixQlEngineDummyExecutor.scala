import org.mixql.cluster.IExecutor

import java.lang.Thread.sleep
import scala.concurrent.Future

object MixQlEngineDummyExecutor extends IExecutor {

  override def start(identity: String, host: String, backendPort: String): Future[Unit] = {
    import concurrent.ExecutionContext.Implicits.global
    Future {
      sleep(600000)
    }
  }

}
