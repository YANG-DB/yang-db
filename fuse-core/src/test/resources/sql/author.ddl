CREATE TABLE author
(
    id            NUMBER(7)    NOT NULL PRIMARY KEY,
    first_name    VARCHAR2(50),
    last_name     VARCHAR2(50) NOT NULL,
    date_of_birth DATE,
    year_of_birth NUMBER(7),
    distinguished NUMBER(1)
);
