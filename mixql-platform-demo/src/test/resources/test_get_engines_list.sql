let res = "";
for r in get_engines_list() loop
    if $res != "" then
        let res = $res || ',' || $r;
    else
        let res = $res || $r;
    end if
end loop

print("current engine's list: " || $res);