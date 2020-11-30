--     tconst (string) - alphanumeric identifier of episode
--     parentTconst (string) - alphanumeric identifier of the parent TV Series
--     seasonNumber (integer) – season number the episode belongs to
--     episodeNumber (integer) – episode number of the tconst in the TV series

CREATE TABLE episods
(
    tconst        VARCHAR(100) NOT NULL PRIMARY KEY,
    parentTconst  VARCHAR(100),
    seasonNumber  NUMERIC(6),
    episodeNumber NUMERIC(6),

    -- self reference parent-child hirarchy
    CONSTRAINT fk_episods2episods FOREIGN KEY (parentTconst) REFERENCES episods (tconst),
    -- reference to title
    CONSTRAINT fk_episods2title FOREIGN KEY (tconst) REFERENCES title (titleId)

)
