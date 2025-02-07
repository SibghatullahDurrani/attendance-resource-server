CREATE SEQUENCE IF NOT EXISTS attendance_id_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE attendances
(
    id      BIGINT                      NOT NULL,
    user_id BIGINT                      NOT NULL,
    date    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_attendances PRIMARY KEY (id)
);

ALTER TABLE attendances
    ADD CONSTRAINT FK_ATTENDANCES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);