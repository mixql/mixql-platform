package org.mixql.cluster

import scala.concurrent.Future

trait IExecutor:
  def start(identity: String, host: String, backendPort: String, logLevel: String): Future[Unit]
