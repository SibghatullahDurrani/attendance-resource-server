CREATE SEQUENCE IF NOT EXISTS checkin_id_sequence START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE IF NOT EXISTS checkout_id_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE check_ins
(
    id            BIGINT                      NOT NULL,
    date          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    attendance_id BIGINT                      NOT NULL,
    CONSTRAINT pk_check_ins PRIMARY KEY (id)
);

CREATE TABLE check_outs
(
    id            BIGINT                      NOT NULL,
    date          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    attendance_id BIGINT                      NOT NULL,
    CONSTRAINT pk_check_outs PRIMARY KEY (id)
);

ALTER TABLE check_ins
    ADD CONSTRAINT FK_CHECK_INS_ON_ATTENDANCE FOREIGN KEY (attendance_id) REFERENCES attendances (id);

ALTER TABLE check_outs
    ADD CONSTRAINT FK_CHECK_OUTS_ON_ATTENDANCE FOREIGN KEY (attendance_id) REFERENCES attendances (id);