CREATE TABLE translations
(
    id       BIGINT NOT NULL,
    language VARCHAR(255),
    value    VARCHAR(255),
    CONSTRAINT pk_translations PRIMARY KEY (id)
);