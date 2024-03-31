DROP TABLE IF EXISTS translations;

CREATE TABLE translations (
    /* Auto-incrementing ID */
    id       bigserial,
    /* 2-letter language code */
    language varchar(2),
    /* Actual content of the translation */
    value    text,

    /* Composite primary key */
    constraint pk_translations primary key (id, language)
);