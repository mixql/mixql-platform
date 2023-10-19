package org.mixql.platform.demo.procedures

import org.mixql.core.context.Context
import org.mixql.platform.demo.logger.{logInfo, logWarn}
import org.mixql.core.context.mtype.MNone
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
}
