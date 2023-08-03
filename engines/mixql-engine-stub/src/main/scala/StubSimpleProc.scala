package org.mixql.engine.demo

import org.mixql.core.context.gtype.{Type, gInt, string}
import org.mixql.engine.core.PlatformContext

object StubSimpleProc {
  val simple_func =
    new (() => String) {
      override def apply(): String = {
        "SUCCESS"
      }
    }

  val simple_func_params =
    new ((String, Int) => String) {
      override def apply(a: String, b: Int): String = {
        s"SUCCESS:$a:${b.toString}"
      }
    }

  val simple_func_context_params =
    new ((StubContext, String, Int) => String) {
      override def apply(ctx: StubContext, a: String, b: Int): String = {
        s"SUCCESS:${ctx.name}:$a:${b.toString}"
      }
    }

  val simple_func_return_arr =
    new ((StubContext) => Array[Array[String]]) {
      override def apply(ctx: StubContext): Array[Array[String]] = {
        Array(
          Array("1", "Alfreds Futterkiste", "Maria Anders", "Obere Str. 57", "Berlin", "12209", "Germany"),
          Array(
            "2",
            "Ana Trujillo Emparedados y helados",
            "Ana Trujillo",
            "Avda. de la Constitución 2222",
            "México D.F.",
            "05021",
            "Mexico"
          ),
          Array("3", "Antonio Moreno Taquería", "Antonio Moreno", "Mataderos 2312", "México D.F.", "05023", "Mexico"),
          Array("4", "Around the Horn", "Thomas Hardy", "120 Hanover Sq.", "London", "WA1 1DP", "UK"),
          Array("5", "Berglunds snabbköp", "Christina Berglund", "Berguvsvägen 8", "Luleå", "S-958 22", "Sweden")
        )
      }
    }

  val execute_platform_func_in_stub_func =
    new ((PlatformContext, String) => String) {
      override def apply(ctx: PlatformContext, a: String): String = {
        val funcArgs = List(new string(a))
        s"SUCCESS:${ctx.invokeFunction("base64", funcArgs).asInstanceOf[string]}:$a"
      }
    }

  // closure
  val execute_stub_func_using_platform_in_stub_func =
    new ((PlatformContext, String, Int) => String) {
      override def apply(ctx: PlatformContext, a: String, b: Int): String = {
        val funcArgs = List(new string(a), new gInt(b))
        s"CLOSURE:${ctx.invokeFunction("stub_simple_proc_context_params", funcArgs).asInstanceOf[string]}"
      }
    }

  val simple_func_return_map =
    new ((StubContext) => Map[String, Map[String, String]]) {
      override def apply(ctx: StubContext): Map[String, Map[String, String]] = {
        Map(
          "1" -> Map(
            "CustomerName" -> "Alfreds Futterkiste",
            "ContactName" -> "Maria Anders",
            "Address" -> "Obere Str. 57",
            "City" -> "Berlin",
            "PostalCode" -> "12209",
            "Country" -> "Germany"
          ),
          "2" -> Map(
            "CustomerName" -> "Ana Trujillo Emparedados y helados",
            "ContactName" -> "Ana Trujillo",
            "Address" -> "Avda. de la Constitución 2222",
            "City" -> "México D.F.",
            "PostalCode" -> "05021",
            "Country" -> "Mexico"
          ),
          "3" -> Map(
            "CustomerName" -> "Antonio Moreno Taquería",
            "ContactName" -> "Antonio Moreno",
            "Address" -> "Mataderos 2312",
            "City" -> "México D.F.",
            "PostalCode" -> "05023",
            "Country" -> "Mexico"
          ),
          "4" -> Map(
            "CustomerName" -> "Around the Horn",
            "ContactName" -> "Thomas Hardy",
            "Address" -> "120 Hanover Sq.",
            "City" -> "London",
            "PostalCode" -> "WA1 1DP",
            "Country" -> "UK"
          ),
          "5" -> Map(
            "CustomerName" -> "Berglunds snabbköp",
            "ContactName" -> "Christina Berglund",
            "Address" -> "Berguvsvägen 8",
            "City" -> "Luleå",
            "PostalCode" -> "S-958 22",
            "Country" -> "Sweden"
          )
        )
      }
    }
}
