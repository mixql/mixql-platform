import org.mixql.platform.demo.utils.FilesOperations

class TestSimpleQueries extends MixQLClusterTest {

  test("work correctly with variables") {
    run("""
        |let a='dfdff';
        |let c="dfdff";
        |let d=dfdff;
        |print("a is [$a]");
        |let b="${$a || ""}";
        |print("b is [$b]");
        |print("${$a || ""}");
        |print(${$a || ""});
        |print('${a}test_concat1');
        |print('${$a}test_concat1');
        |
        |use database ${$a || ""};
        |use database $a;
        |use "${$a || $b}";
        |use $b;
        |use $c;
        |use "$b + $c";
        |use ${"$b" + "$c"};
        |use ${$b + $c};
        |use ${$a};
        |use $d;
        |
        |print_current_vars();
        |""".stripMargin)
  }

  test("work correctly with variables 2 test") {
    run("""
        |let a="fgfg";
        |let b="fgfg";
        |let c="fgfg";
        |
        |use database ${$a || ""}; -- нет кавычек
        |use database $a; -- нет кавычек
        |use database ${$a}; -- нет кавычек
        |use database "${$a}.inter"; -- кавычки
        |use database "$a"; -- кавычки
        |use database "${$a || ""}"; -- кавычки
        |use ${$b + $c}; -- нет кавычек
        |""".stripMargin)
  }

  test("get current engine during executing") {
    run("""
        |print($mixql.execution.engine);
        |""".stripMargin)
  }

  test("make for several variables in array in array") {
    run("""
        |let res = 0;
        |let c = [[1, 2, 4], [3, 4, 8], [5, 6, 9]];
        |for a, b in $c loop
        | -- let res = $res + $a + $b;
        | print( "a: $a, b: $b");
        |end loop
        |""".stripMargin)
  }
}
