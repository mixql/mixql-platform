package org.mixql.test.engines.Stub5SecTimeout

import org.mixql.cluster.IExecutor
import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.engine.demo.MixQlEngineDemo
import org.mixql.engine.core

import scala.concurrent.Future

object MixQlEngineStub5SecExecutor extends IExecutor {

  override def start(identity: String, host: String, backendPort: String): Future[Unit] = {
    import concurrent.ExecutionContext.Implicits.global
    Future {
      implicit val logger = new ModuleLogger(identity)
      logger.logInfo("Starting MixQlEngineStub5Sec")

      new core.Module(EngineDemo5SecExecutor, identity, host, backendPort.toInt).startServer()
    }
  }
}
