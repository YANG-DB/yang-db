--     tconst (string) - alphanumeric unique identifier of the title
--     ordering (integer) – a number to uniquely identify rows for a given titleId
--     nconst (string) - alphanumeric unique identifier of the name/person
--     category (string) - the category of job that person was in
--     job (string) - the specific job title if applicable, else '\N'
--     characters (string) - the name of the character played if applicable, else '\N'
CREATE TABLE principals
(
    tconst     VARCHAR(400) NOT NULL,
    ordering   NUMBER(4),
    nconst     VARCHAR(400) NOT NULL,
    category   VARCHAR(400),
    job        VARCHAR(400),
    characters VARCHAR(100),

    PRIMARY KEY (tconst, nconst),

    -- reference to title
    CONSTRAINT fk_principals2title FOREIGN KEY (tconst) REFERENCES title (titleId),
    -- reference to names
    CONSTRAINT fk_principals2title FOREIGN KEY (nconst) REFERENCES name (nconst)

)
