CREATE SEQUENCE IF NOT EXISTS user_id_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE users
(
    id          BIGINT       NOT NULL,
    first_name  VARCHAR(30)  NOT NULL,
    second_name VARCHAR(30)  NOT NULL,
    password    VARCHAR(255) NOT NULL,
    username    VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE INDEX idx_77584fbe74cc86922be2a3560 ON users (username);