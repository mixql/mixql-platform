package org.mixql.cluster

import org.apache.logging.log4j.LogManager

package object logger {
  private val log = LogManager.getRootLogger

  def logInfo(msg: String): String = {
    log.info("[mixql-cluster] " + msg)
    "[mixql-cluster] " + msg
  }

  def logDebug(msg: String): String = {
    log.debug("[mixql-cluster] " + msg)
    "[mixql-cluster] " + msg
  }

  def logWarn(msg: String): String = {
    log.warn("[mixql-cluster] " + msg)
    "[mixql-cluster] " + msg
  }

  def logError(msg: String): String = {
    log.error("[mixql-cluster] " + msg)
    "[mixql-cluster] " + msg
  }
}
