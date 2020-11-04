CREATE TABLE book
(
    id           NUMBER(7)     NOT NULL PRIMARY KEY,
    author_id    NUMBER(7)     NOT NULL,
    title        VARCHAR(400) NOT NULL,
    published_in NUMBER(7)     NOT NULL,
    language_id  NUMBER(7)     NOT NULL,

    CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES author(id),
    CONSTRAINT fk_book_language FOREIGN KEY (language_id) REFERENCES language(id)
);