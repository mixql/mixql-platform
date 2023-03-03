let res = "";
for r in [[1], [3], [5]] loop --simulate select that returns array in array
  for v in $r loop
    let res = $res || $v || ' ';
  end loop
end loop

print("res: " || $res);