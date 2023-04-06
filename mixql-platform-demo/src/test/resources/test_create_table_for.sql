let engine "sqlite-local";
let prefix = "table_prefix_";
let exponent = 5;

for i in 1..3 LOOP
    let table_name = $prefix || 'test_table_t' || $i;
    drop table if exists $table_name;
--     create table $table_name as select $i as number, pow($i, $exponent) as 'POWER_OF_$exponent';
    create table $table_name as select $i as number, pow($i, $exponent) as ${'POWER_OF_' || $exponent};
    PRINT ("$table_name: " || (select * from $table_name));
end loop