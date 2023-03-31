let engine "sqlite-local-sakila";

let res = "";
for r in get_engines_list() loop
    if $res != "" then
        let res = $res || ',' || $r;
    else
            let res = $res || $r;
    end if
end loop

print("current engine's list: " || $res);

let a = (SELECT rating AS Rating, COUNT(title) AS Count
        FROM film
        GROUP BY rating
        ORDER BY Count DESC);

print($a);

let engine "stub-local";