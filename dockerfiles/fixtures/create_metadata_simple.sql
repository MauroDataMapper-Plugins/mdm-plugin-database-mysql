CREATE DATABASE metadata_simple;

USE metadata_simple;

CREATE TABLE IF NOT EXISTS catalogue_item (
    id            BINARY(16)   NOT NULL
        PRIMARY KEY,
    version       BIGINT       NOT NULL,
    date_created  TIMESTAMP    NOT NULL,
    domain_type   VARCHAR(255) NOT NULL,
    last_updated  TIMESTAMP    NOT NULL,
    path          TEXT         NOT NULL,
    depth         INTEGER      NOT NULL,
    created_by_id BINARY(16)   NOT NULL,
    label         TEXT         NOT NULL,
    description   TEXT
);

CREATE INDEX catalogue_item_domain_type_index
    ON catalogue_item(domain_type);

CREATE INDEX catalogue_item_created_by_idx
    ON catalogue_item(created_by_id);

CREATE TABLE IF NOT EXISTS catalogue_user (
    id               BINARY(16)   NOT NULL
        PRIMARY KEY,
    version          BIGINT       NOT NULL,
    salt             BLOB         NOT NULL,
    date_created     TIMESTAMP    NOT NULL,
    first_name       VARCHAR(255) NOT NULL,
    domain_type      VARCHAR(255) NOT NULL,
    last_updated     TIMESTAMP    NOT NULL,
    organisation     VARCHAR(255),
    user_role        VARCHAR(255) NOT NULL,
    job_title        VARCHAR(255),
    email_address    VARCHAR(255) NOT NULL
        UNIQUE,
    user_preferences VARCHAR(255),
    password         BLOB,
    created_by_id    BINARY(16)
        REFERENCES catalogue_user,
    temp_password    VARCHAR(255),
    last_name        VARCHAR(255) NOT NULL,
    last_login       TIMESTAMP,
    disabled         BOOLEAN
);

CREATE INDEX catalogue_user_created_by_idx
    ON catalogue_user(created_by_id);

ALTER TABLE catalogue_item
    ADD FOREIGN KEY (created_by_id) REFERENCES catalogue_user(id);

CREATE TABLE IF NOT EXISTS metadata (
    id                BINARY(16)   NOT NULL
        PRIMARY KEY,
    version           BIGINT       NOT NULL,
    date_created      TIMESTAMP    NOT NULL,
    domain_type       VARCHAR(255) NOT NULL,
    catalogue_item_id BINARY(16)   NOT NULL
        REFERENCES catalogue_item,
    last_updated      TIMESTAMP    NOT NULL,
    namespace         TEXT         NOT NULL,
    value             TEXT         NOT NULL,
    created_by_id     BINARY(16)   NOT NULL
        REFERENCES catalogue_user,
    `key`             TEXT         NOT NULL
);

CREATE INDEX metadata_catalogue_item_idx
    ON metadata(catalogue_item_id);

CREATE INDEX metadata_created_by_idx
    ON metadata(created_by_id);

