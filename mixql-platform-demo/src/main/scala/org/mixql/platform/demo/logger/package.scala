package org.mixql.platform.demo

import com.typesafe.config.ConfigFactory
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.{Level, LogManager}

import scala.util.Try

package object logger {
  val config: com.typesafe.config.Config = ConfigFactory.load()
  Configurator.setAllLevels(LogManager.getRootLogger.getName, getLoggingLevel())

  val log = LogManager.getRootLogger

  def logInfo(msg: String) = {
    log.info("[mixql-platform-demo] " + msg)
  }

  def logDebug(msg: String) = {
    log.debug("[mixql-platform-demo] " + msg)
  }
  def logWarn(msg: String) = {
    log.warn("[mixql-platform-demo] " + msg)
  }
  def logError(msg: String) = {
    log.error("[mixql-platform-demo] " + msg)
  }

  def getLoggingLevel(): org.apache.logging.log4j.Level =
    Try(getLogLevelFromConfig(config.getString("org.mixql.platform.logger")))
      .getOrElse(Try(getLogLevelFromConfig(config.getString("org.mixql.platform.demo.logger"))).getOrElse(Level.WARN))

  private def getLogLevelFromConfig(logLevelConf: String): org.apache.logging.log4j.Level =
    logLevelConf.toUpperCase match
      case "DEBUG"   => Level.DEBUG
      case "INFO"    => Level.INFO
      case "WARNING" => Level.WARN
      case "WARN"    => Level.WARN
      case "ERROR"   => Level.ERROR
      case "OFF"     => Level.OFF
      case "ALL"     => Level.ALL
}
