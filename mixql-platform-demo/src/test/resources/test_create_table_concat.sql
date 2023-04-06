let engine "sqlite-local";
let a='dfdff';

drop table if exists ${$a || 'test_concat1'};

create table ${$a || 'test_concat1'}(
    CustomerName varchar(30),
    ContactName varchar(30),
    Address varchar(30),
    City varchar(30),
    PostalCode varchar(30),
    Country varchar(30)
);

insert into ${$a || 'test_concat1'} values
    ("qsfdsfsf", "sdfsdfds", "sdfsdf street", "berlin", "12123", "Germany"),
    ("dfdfgdfg", "qehuimgy", "ghnngf street", "grlin", "12123", "Mexico"),
    ("yfgdfbnn", "uikmjhgs", "jmjhmj street", "berlin", "12123", "Mexico"),
    ("iuiouioo", "zdvbgngu", "myttbe street", "berlin", "12123", "UK"),
    ("bnmuyyuu", "imjmjhgh", "qbnygn street", "berlin", "12123", "Sweden"),
    ("zvnhjkuq", "jhk,munt", "pmuyngf street", "berlin", "12123", "France");

print ("--------------Fase 1 Test not a in-------------");

-- expected
-- for line in (
--     select CustomerName, ContactName, Address, City, Country
--     from ${$a || 'test_concat1'}
--     where not Country in ('Germany', 'France', 'UK')
-- ) loop
--     for CustomerName, ContactName, Address, City, Country in $line loop
--         print (${"CustomerName: " || $CustomerName ||
--             " ContactName: " || $ContactName ||
--             " Address: " || $Address ||
--             " City: " || $City ||
--             " Country: " || $Country
--         });
--     end loop
-- end loop

-- for CustomerName, ContactName, Address, City, Country in (
--     select CustomerName, ContactName, Address, City, Country
--     from ${$a || 'test_concat1'}
--     where not Country in ('Germany', 'France', 'UK')
-- ) loop
--     print (${"CustomerName: " || $CustomerName ||
--         " ContactName: " || $ContactName ||
--         " Address: " || $Address ||
--         " City: " || $City ||
--         " Country: " || $Country
--     });
-- end loop

for CustomerName, ContactName, Address, City, Country in (
    select CustomerName, ContactName, Address, City, Country
    from ${$a || 'test_concat1'}
    where not Country in ('Germany', 'France', 'UK')
) loop
   print ("CustomerName: $CustomerName ContactName: $ContactName Address: " +
    "$Address  City:  $City  Country: $Country");
end loop
print ("-----------------------------------------------");

let engine "stub-local";