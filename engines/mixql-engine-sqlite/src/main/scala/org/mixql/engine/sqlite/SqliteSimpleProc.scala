package org.mixql.engine.sqlite

object SqliteSimpleProc {
  val simple_func = new (() => String) {
    override def apply(): String = {
      "SUCCESS"
    }
  }

  val simple_func_params = new ((String, Int) => String) {
    override def apply(a: String, b: Int): String = {
      s"SUCCESS:$a:${b.toString}"
    }
  }

  val simple_func_context_params = new ((SQLightJDBC, String, Int) => String) {
    override def apply(ctx: SQLightJDBC, a: String, b: Int): String = {
      s"SUCCESS:${ctx.getClass}:$a:${b.toString}"
    }
  }
}
