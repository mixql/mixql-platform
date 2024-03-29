package org.mixql.engine.stub.local

object StubSimpleProc {

  val simple_func =
    new (() => String) {

      override def apply(): String = {
        "SUCCESS"
      }
    }

  val simple_func_params =
    new ((String, Long) => String) {

      override def apply(a: String, b: Long): String = {
        s"SUCCESS:$a:${b.toString}"
      }
    }

  val simple_func_context_params =
    new ((StubContext, String, Long) => String) {

      override def apply(ctx: StubContext, a: String, b: Long): String = {
        s"SUCCESS:${ctx.name}:$a:${b.toString}"
      }
    }
}
