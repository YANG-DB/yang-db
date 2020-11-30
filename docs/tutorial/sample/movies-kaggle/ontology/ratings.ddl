-- userId,movieId,rating,timestamp
CREATE TABLE ratings
(
    userId  NUMBER(7) NOT NULL ,
    movieId NUMBER(7) NOT NULL ,
    rating  FLOAT     NOT NULL,
    crew    DATETIME  NOT NULL,

    PRIMARY KEY (userId, movieId)

)
