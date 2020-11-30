--     nconst (string) - alphanumeric unique identifier of the name/person
--     primaryName (string)– name by which the person is most often credited
--     birthYear – in YYYY format
--     deathYear – in YYYY format if applicable, else '\N'
--     primaryProfession (array of strings)– the top-3 professions of the person
--     knownForTitles (array of tconsts) – titles the person is known for
CREATE TABLE name
(
    nconst            VARCHAR(400) NOT NULL PRIMARY KEY,
    primaryName       VARCHAR(400),
    birthYear         NUMBER(4),
    deathYear         NUMBER(4),
    primaryProfession VARCHAR(400),
    knownForTitles    VARCHAR(400)
)
