--     titleId (string) - a tconst, an alphanumeric unique identifier of the title
--     ordering (integer) – a number to uniquely identify rows for a given titleId
--     title (string) – the localized title
--     region (string) - the region for this version of the title
--     language (string) - the language of the title
--     types (array) - Enumerated set of attributes for this alternative title. One or more of the following: "alternative", "dvd", "festival", "tv", "video", "working", "original", "imdbDisplay". New values may be added in the future without warning
--     attributes (array) - Additional terms to describe this alternative title, not enumerated
--     isOriginalTitle (boolean) – 0: not original title; 1: original title

CREATE TABLE title
(
    titleId         VARCHAR(100)  NOT NULL PRIMARY KEY,
    ordering        VARCHAR(400),
    title           VARCHAR(1000) NOT NULL,
    region          VARCHAR(100)  NOT NULL,
    language        VARCHAR(100)  NOT NULL,
    types           VARCHAR(400)  ,
    attributes      VARCHAR(400)  ,
    isOriginalTitle VARCHAR(1)    NOT NULL
)
