package org.mixql.cluster

import org.apache.logging.log4j.LogManager

package object logger {
  private val log = LogManager.getRootLogger

  def logInfo(msg: String) = {
    log.info("[mixql-cluster] " + msg)
  }

  def logDebug(msg: String) = {
    log.debug("[mixql-cluster] " + msg)
  }

  def logWarn(msg: String) = {
    log.warn("[mixql-cluster] " + msg)
  }

  def logError(msg: String) = {
    log.error("[mixql-cluster] " + msg)
  }
}
