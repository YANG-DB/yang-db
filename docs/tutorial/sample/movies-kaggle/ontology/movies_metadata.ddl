-- adult,belongs_to_collection,budget,genres,homepage,id,imdb_id,original_language,original_title,overview,popularity,poster_path,production_companies,production_countries,release_date,revenue,runtime,spoken_languages,status,tagline,title,video,vote_average,vote_count
CREATE TABLE movies_metadata
(
    id                    NUMBER(7)     NOT NULL PRIMARY KEY,
    adult                 VARCHAR(4)    NOT NULL,
    belongs_to_collection VARCHAR(1200),
    budget                NUMBER(10),
    genres                VARCHAR(1200),
    homepage              NUMBER(100),
    imdb_id               NUMBER(100)   NOT NULL,
    original_language     VARCHAR(4)    NOT NULL,
    original_title        VARCHAR(100)  NOT NULL,
    overview              VARCHAR(1200),
    popularity            FLOAT,
    poster_path           VARCHAR(30),
    production_companies  VARCHAR(500)  NOT NULL,
    production_countries  VARCHAR(500)  NOT NULL,
    release_date          DATE      NOT NULL,
    revenue               NUMBER(10)    NOT NULL,
    runtime               FLOAT(4)      NOT NULL,
    spoken_languages      VARCHAR(1200) NOT NULL,
    status                VARCHAR(100)  NOT NULL,
    tagline               VARCHAR(1000),
    title                 VARCHAR(100)  NOT NULL,
    video                 VARCHAR(4)    NOT NULL,
    vote_avarage          FLOAT(4)      NOT NULL,
    vote_count            NUMBER(5)     NOT NULL
)
