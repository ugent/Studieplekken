DROP TABLE IF EXISTS translations;

CREATE TABLE translations (
    /* Auto-incrementing ID */
    id       SERIAL,
    /* 2-letter language code */
    language VARCHAR(2),
    /* Actual content of the translation */
    value    TEXT,

    /* Composite primary key */
    CONSTRAINT pk_translations PRIMARY KEY (id)
);