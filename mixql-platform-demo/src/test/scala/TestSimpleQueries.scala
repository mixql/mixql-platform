import org.mixql.platform.demo.utils.FilesOperations

class TestSimpleQueries extends MixQLClusterTest {

  behavior of "correctly exeute simple commands"

  it should("work correctly with variables") in {
    run(
      """
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

  it should("work correctly with variables 2 test") in {
    run(
      """
        |let a="fgfg";
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

  it should("make for several variables in array in array") in {
    run(
      """
        |let res = 0;
        |let c = [[1, 2, 4], [3, 4, 8], [5, 6, 9]];
        |for a, b in $c loop
        | -- let res = $res + $a + $b;
        | print( "a: $a, b: $b");
        |end loop
        |""".stripMargin)
  }

  it should("make for several variables in array") in {
    run(
      """
        |let res = 0;
        |let c = [1, 2, 3, 4, 5, 6, 7, 8, 9];
        |for a, b in $c loop
        | -- let res = $res + $a + $b;
        | print( "a: $a, b: $b");
        |end loop
        |""".stripMargin)
  }
}
