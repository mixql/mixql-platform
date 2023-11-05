package org.mixql.test.engines.EngineFail

import org.mixql.cluster.IExecutor
import org.mixql.test.utils.JavaProcess

import scala.concurrent.Future

object EngineFailStarter extends IExecutor {

  override def start(identity: String, host: String, backendPort: String, logLevel: String): Future[Unit] = {
    JavaProcess.exec(
      (new MixQlEngineFail()).getClass,
      List("--port", backendPort, "--host", host, "--identity", identity, "--log-level", logLevel)
    )
    import concurrent.ExecutionContext.Implicits.global
    Future {} // For compatibility
  }
}
