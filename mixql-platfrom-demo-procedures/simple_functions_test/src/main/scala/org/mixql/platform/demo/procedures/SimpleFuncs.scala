package org.mixql.platform.demo.procedures
import org.mixql.core.context.Context

object SimpleFuncs{
  val simple_func = new ((String) => String) {
    override def apply(arg: String): String = {
      println("[simple_func] got argument " + arg)
      "SUCCESS"
    }
  }

  val print_current_vars = new ((Context, Boolean) => String) {
    override def apply(ctx: Context, currentScope: Boolean = false): String = {
      println("[print_current_vars] started")
      println("[print_current_vars] context scope: " + ctx.getScope().toString())
      "SUCCESS"
    }
  }

  val get_engines_list = new ((Context) => Array[String]) {
    override def apply(ctx: Context): Array[String] = {
      println("[get_engines_list] started")
      val engineNames = ctx.engines.keys
      println("[get_engines_list] supported engines: " + engineNames.mkString(","))
      engineNames.toArray
    }
  }
}


