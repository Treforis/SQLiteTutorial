PRAGMA foreign_keys = ON;

CREATE TABLE City (
    Id    INTEGER PRIMARY KEY,
    City  VARCHAR(30) NOT NULL
);

CREATE TABLE Employee (
    Id              INTEGER PRIMARY KEY,
    Employee_Name   VARCHAR(50) NOT NULL,
    Employee_Number VARCHAR(10) NOT NULL UNIQUE,
    Date_Hired      DATE,
    City_Id         INTEGER NOT NULL REFERENCES City(Id)
);

CREATE TABLE Salaried_Employee (
    Id            INTEGER PRIMARY KEY,
    Annual_Salary FLOAT,
    Employee_Id   INTEGER NOT NULL UNIQUE REFERENCES Employee(Id)
);

CREATE TABLE Hourly_Employee (
    Id          INTEGER PRIMARY KEY,
    Hourly_Rate FLOAT,
    Employee_Id INTEGER NOT NULL UNIQUE REFERENCES Employee(Id)
);

INSERT INTO City VALUES (1, 'Ottawa');
INSERT INTO City VALUES (2, 'Gatineau');

INSERT INTO Employee VALUES (1, 'Sophie Tremblay', 'E-0001', '2021-03-15', 1);
INSERT INTO Employee VALUES (2, 'Marc Gagnon',     'E-0002', '2022-09-01', 1);
INSERT INTO Employee VALUES (3, 'Amina Diallo',    'E-0003', '2023-01-09', 2);
INSERT INTO Employee VALUES (4, 'Lucas Martin',    'E-0004', '2024-06-17', 2);

INSERT INTO Salaried_Employee VALUES (1, 72000.00, 1);
INSERT INTO Salaried_Employee VALUES (2, 65500.00, 3);

INSERT INTO Hourly_Employee VALUES (1, 22.50, 2);
INSERT INTO Hourly_Employee VALUES (2, 19.75, 4);

.mode box
SELECT * FROM City;
SELECT * FROM Employee;
SELECT * FROM Salaried_Employee;
SELECT * FROM Hourly_Employee;
SELECT e.Employee_Name, c.City,
       CASE WHEN s.Id IS NOT NULL THEN 'Salarié' ELSE 'Horaire' END AS Type,
       COALESCE(s.Annual_Salary, h.Hourly_Rate) AS Remuneration
FROM Employee e
JOIN City c ON c.Id = e.City_Id
LEFT JOIN Salaried_Employee s ON s.Employee_Id = e.Id
LEFT JOIN Hourly_Employee h ON h.Employee_Id = e.Id
ORDER BY e.Id;
