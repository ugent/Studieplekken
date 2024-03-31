DROP TABLE IF EXISTS translations;
DROP SEQUENCE IF EXISTS translations_id_seq;

CREATE SEQUENCE translations_id_seq;
CREATE TABLE translations (
    /* Auto-incrementing ID */
    id       bigint default nextval('translations_id_seq'),
    /* 2-letter language code */
    language varchar(2),
    /* Actual content of the translation */
    value    text,

    /* Composite primary key */
    constraint pk_translations primary key (id, language)
);