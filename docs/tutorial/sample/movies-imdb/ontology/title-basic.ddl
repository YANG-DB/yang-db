--
--     tconst (string) - alphanumeric unique identifier of the title
--     titleType (string) – the type/format of the title (e.g. movie, short, tvseries, tvepisode, video, etc)
--     primaryTitle (string) – the more popular title / the title used by the filmmakers on promotional materials at the point of release
--     originalTitle (string) - original title, in the original language
--     isAdult (boolean) - 0: non-adult title; 1: adult title
--     startYear (YYYY) – represents the release year of a title. In the case of TV Series, it is the series start year
--     endYear (YYYY) – TV Series end year. ‘\N’ for all other title types
--     runtimeMinutes – primary runtime of the title, in minutes
--     genres (string array) – includes up to three genres associated with the title
--

CREATE TABLE title_basic
(
    tconst         VARCHAR(100) NOT NULL PRIMARY KEY,
    titleType      VARCHAR(100),
    primaryTitle   VARCHAR(100),
    originalTitle  VARCHAR(100),
    isAdult        VARCHAR(1)   NOT NULL,
    startYear      VARCHAR(4)   NOT NULL,
    endYear        VARCHAR(4)   NOT NULL,
    runtimeMinutes NUMBER(4)    NOT NULL,
    genres         VARCHAR(400),

    -- reference to title
    CONSTRAINT fk_basic2title FOREIGN KEY (tconst) REFERENCES title (titleId)

)
