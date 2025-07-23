CREATE SEQUENCE IF NOT EXISTS shift_id_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE shifts
(
    id              BIGINT NOT NULL,
    name            VARCHAR(255),
    check_in_time   VARCHAR(255),
    check_out_time  VARCHAR(255),
    organization_id BIGINT,
    CONSTRAINT pk_shifts PRIMARY KEY (id)
);

ALTER TABLE users
    ADD shift_id BIGINT;

ALTER TABLE shifts
    ADD CONSTRAINT FK_SHIFTS_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_SHIFT FOREIGN KEY (shift_id) REFERENCES shifts (id);