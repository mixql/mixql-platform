package org.mixql.platform.demo.procedures

import org.mixql.core.context.Context
import org.mixql.platform.demo.logger.{logInfo, logWarn}
import org.mixql.core.context.gtype.none
import org.mixql.core.engine.Engine

object SimpleFuncs {

  val simple_func =
    new ((String) => String) {

      override def apply(arg: String): String = {
        logInfo("[simple_func] got argument " + arg)
        "SUCCESS"
      }
    }

  val print_current_vars =
    new ((Context, Boolean) => String) {

      override def apply(ctx: Context, currentScope: Boolean = false): String = {
        logInfo("[print_current_vars] started")
        logInfo("[print_current_vars] context scope: " + ctx.getParams().toString())
        "SUCCESS"
      }
    }

  val get_engines_list =
    new ((Context) => Array[String]) {

      override def apply(ctx: Context): Array[String] = {
        logInfo("[get_engines_list] started")
        val engineNames = ctx.engineNames
        logInfo("[get_engines_list] supported engines: " + engineNames.mkString(","))
        engineNames.toArray
      }
    }

  val closeEngine =
    new ((Context, String) => none) {

      override def apply(ctx: Context, engineName: String = ""): none = {
        logInfo("[close_engine] started")
        val engine: Engine =
          if engineName.isEmpty then ctx.currentEngine
          else ctx.getEngine(engineName.trim).get

        if engine.isInstanceOf[AutoCloseable] then
          val closableEngine = engine.asInstanceOf[AutoCloseable]
          logInfo("[close_engine] trigger engine's " + engine.name + " close")
          closableEngine.close()
        else logWarn("[close_engine] unsupported engine " + engine.name + ". It's not AutoCloseable. Ignore it")

        new none()
      }
    }
}
