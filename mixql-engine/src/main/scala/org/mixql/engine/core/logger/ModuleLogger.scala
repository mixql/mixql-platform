package org.mixql.engine.core.logger

import org.apache.logging.log4j.{Level, LogManager}
import org.apache.logging.log4j.core.config.Configurator

class ModuleLogger(indentity: String, logLevel: String) {
  Configurator.setAllLevels(LogManager.getRootLogger.getName, convertToLog4JLevel(logLevel))
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

  private def convertToLog4JLevel(logLevelConf: String): org.apache.logging.log4j.Level = {
    logLevelConf.toUpperCase match {
      case "DEBUG"   => Level.DEBUG
      case "INFO"    => Level.INFO
      case "WARNING" => Level.WARN
      case "WARN"    => Level.WARN
      case "ERROR"   => Level.ERROR
      case "OFF"     => Level.OFF
      case "ALL"     => Level.ALL
    }
  }

}
