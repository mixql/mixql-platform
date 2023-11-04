 let engine "sqlite";
--let engine "sqlite-scala-2-12";

let async1 = async
    CREATE TABLE if not exists "Customers" (
                           CustomerName varchar(255),
                           ContactName varchar(255),
                           Address varchar(255),
                           City varchar(255),
                           PostalCode int,
                           Country varchar(255)
    );

    INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)
    VALUES
        ('Cardinal1', 'Tom B. Erichsen1', 'Skagen 21', 'Stavanger', '4006', 'Norway'),
        ('Cardinal1', 'Tom B. Erichsen2', 'Skagen 21', 'Stavanger', '4006', 'Norway'),
        ('Cardinal1', 'Tom B. Erichsen3', 'Skagen 21', 'Stavanger', '4006', 'Norway'),
        ('Cardinal1', 'Tom B. Erichsen4', 'Skagen 21', 'Stavanger', '4006', 'Norway'),
        ('Cardinal1', 'Tom B. Erichsen5', 'Skagen 21', 'Stavanger', '4006', 'Norway');
    return (select * from Customers);
end async;

let async2 = async
    CREATE TABLE if not exists "Customers2" (
                           CustomerName varchar(255),
                           ContactName varchar(255),
                           Address varchar(255),
                           City varchar(255),
                           PostalCode int,
                           Country varchar(255)
    );

    INSERT INTO Customers2 (CustomerName, ContactName, Address, City, PostalCode, Country)
    VALUES
        ('Cardinal2', 'Tom B. Erichsen6', 'Skagen 21', 'Stavanger', '4006', 'Norway'),
        ('Cardinal2', 'Tom B. Erichsen7', 'Skagen 21', 'Stavanger', '4006', 'Norway'),
        ('Cardinal2', 'Tom B. Erichsen8', 'Skagen 21', 'Stavanger', '4006', 'Norway'),
        ('Cardinal2', 'Tom B. Erichsen9', 'Skagen 21', 'Stavanger', '4006', 'Norway'),
        ('Cardinal2', 'Tom B. Erichsen10', 'Skagen 21', 'Stavanger', '4006', 'Norway');
    return (select * from Customers2);
end async;

let res = await_all($async1, $async2);
print("main:$res");

let engine "stub-local";