--     tconst (string) - alphanumeric unique identifier of the title
--     averageRating – weighted average of all the individual user ratings
--     numVotes - number of votes the title has received
CREATE TABLE ratings
(
    tconst        VARCHAR(400) NOT NULL PRIMARY KEY,
    averageRating float(4),
    numVotes      NUMBER(6),

    -- reference to title
    CONSTRAINT fk_ratings2title FOREIGN KEY (tconst) REFERENCES title (titleId)


)
