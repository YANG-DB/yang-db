--     tconst (string) - alphanumeric unique identifier of the title
--     directors (array of nconsts) - director(s) of the given title
--     writers (array of nconsts) – writer(s) of the given title

CREATE TABLE crew
(
    tconst    VARCHAR(100) NOT NULL PRIMARY KEY,
    directors VARCHAR(100),
    writers   VARCHAR(100),

    -- self reference parent-child hirarchy
    CONSTRAINT fk_crew2title FOREIGN KEY (tconst) REFERENCES title (titleId)

)
