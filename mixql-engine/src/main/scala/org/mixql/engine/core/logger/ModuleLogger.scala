package org.mixql.engine.core.logger

import org.apache.logging.log4j.LogManager

class ModuleLogger(indentity: String) {
  private val log = LogManager.getRootLogger

  def logInfo(msg: String) = {
    log.info(s"[module-$indentity] " + msg)
  }

  def logDebug(msg: String) = {
    log.debug(s"[module-$indentity] " + msg)
  }
  def logWarn(msg: String) = {
    log.warn(s"[module-$indentity] " + msg)
  }
  def logError(msg: String) = {
    log.error(s"[module-$indentity] " + msg)
  }
}
