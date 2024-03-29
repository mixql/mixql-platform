package org.mixql.engine.demo

import org.mixql.core.context.mtype.{MType, MInt, MString}
import org.mixql.engine.core.PlatformContext

import collection.mutable

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
        val funcArgs = List(new MString(a))
        s"SUCCESS:${ctx.invokeFunction("base64", funcArgs).asInstanceOf[MString]}:$a"
      }
    }

  val stub_simple_proc_context =
    new ((PlatformContext) => String) {

      override def apply(ctx: PlatformContext): String = {
        val filteredNames = ctx.getVarsNames().filter(p => p.startsWith("a."))
        val namesMap: mutable.Map[String, String] = mutable.Map()
        filteredNames.foreach(name => namesMap.put(name, ctx.getVar(name).toString))
        namesMap.mkString(" ")
      }
    }

  val stub_simple_proc_context_test_setting_getting_vars: PlatformContext => String =
    new ((PlatformContext) => String) {

      override def apply(ctx: PlatformContext): String = {
        val filteredNames = ctx.getVarsNames().filter(p => p.startsWith("a."))
        val namesMap: mutable.Map[String, String] = mutable.Map()

        val firstVarName = filteredNames.head
        println("firstVarName is " + firstVarName)
        val firstVar = ctx.getVar(firstVarName).asInstanceOf[MString]
        println("firstVarName before change" + firstVar.getValue)

        ctx.setVar(firstVarName, new MString(firstVar.getValue + "_changed", firstVar.getQuote))

        val firstVarChanged = ctx.getVar(firstVarName).asInstanceOf[MString]

        assert(firstVarChanged.getValue == firstVar.getValue + "_changed")

        val last4VarsNames = filteredNames.drop(filteredNames.length - 4)
        val last4Vars = ctx.getVars(last4VarsNames)
        ctx.setVars(
          last4Vars.map(t =>
            t._1 ->
              new MString(t._2.asInstanceOf[MString].getValue + "_changed", t._2.asInstanceOf[MString].getQuote)
          )
        )
        val last4VarsChanged = ctx.getVars(last4VarsNames)
        last4VarsChanged.foreach(t =>
          assert({
            val changedVarName = t._1
            val changedVar = t._2.asInstanceOf[MString]
            val originVar = last4Vars.apply(changedVarName).asInstanceOf[MString]
            changedVar.getValue == originVar.getValue + "_changed"
          })
        )
        "SUCCESS"
      }
    }

  // closure
  val execute_stub_func_using_platform_in_stub_func =
    new ((PlatformContext, String, Long) => String) {

      override def apply(ctx: PlatformContext, a: String, b: Long): String = {
        val funcArgs = List(new MString(a), new MInt(b))
        s"CLOSURE:${ctx.invokeFunction("stub_simple_proc_context_params", funcArgs).asInstanceOf[MString]}"
      }
    }

  val execute_stub_func_long_sleep =
    new ((StubContext) => String) {

      override def apply(ctx: StubContext): String = {
        Thread.sleep(90000)
        "SUCCESS"
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
