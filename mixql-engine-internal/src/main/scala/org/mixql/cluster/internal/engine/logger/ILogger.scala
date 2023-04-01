package org.mixql.cluster.internal.engine.logger

import org.apache.logging.log4j.LogManager

trait ILogger {
  private val log = LogManager.getRootLogger

  def name: String

  def logInfo(msg: String) = {
    log.info(s"[ENGINE ${this.name}] " + msg)
  }

  def logDebug(msg: String) = {
    log.debug(s"[ENGINE ${this.name}] " + msg)
  }
  def logWarn(msg: String) = {
    log.warn(s"[ENGINE ${this.name}] " + msg)
  }
  def logError(msg: String) = {
    log.error(s"[ENGINE ${this.name}] " + msg)
  }
}
