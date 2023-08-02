package org.mixql.engine.local.logger

import org.apache.logging.log4j.LogManager

trait IEngineLogger {
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
