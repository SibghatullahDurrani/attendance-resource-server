CREATE SEQUENCE IF NOT EXISTS organization_preferences_id_generator START WITH 1 INCREMENT BY 50;

CREATE TABLE organization_preferences
(
    id                                     BIGINT  NOT NULL,
    check_in_time_for_user                 TIMESTAMP WITHOUT TIME ZONE,
    check_out_time_for_user                TIMESTAMP WITHOUT TIME ZONE,
    minutes_till_attendance_counts_as_late INTEGER NOT NULL,
    retake_attendance_in_hour              INTEGER NOT NULL,
    check_out_tolerance_time_in_hour       INTEGER NOT NULL,
    CONSTRAINT pk_organization_preferences PRIMARY KEY (id)
);