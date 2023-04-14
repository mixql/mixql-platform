let engine "stub-scala-2-12";

let arr = [TRUE, [TRUE, "gg", 12], 12];
print("first element in array is " || $arr[0]);

--trigger execute on remote stub engine, so that params will be sent to engine
INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)
VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21', 'Stavanger', '4006', 'Norway');

print("second element in array is : " || $arr[1]);

print("third element in array is : " || $arr[2]);

print("second element of nested array that is second element of original array: "
    || $arr[1][1]);

print($arr);