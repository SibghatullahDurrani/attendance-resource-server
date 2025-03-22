CREATE SEQUENCE IF NOT EXISTS leave_id_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE leave
(
    id                BIGINT       NOT NULL,
    leave_application VARCHAR(255) NOT NULL,
    status            VARCHAR(255) NOT NULL,
    type              VARCHAR(255) NOT NULL,
    user_id           BIGINT,
    CONSTRAINT pk_leave PRIMARY KEY (id)
);

ALTER TABLE organization_policies
    ADD annual_leaves_allowed INTEGER DEFAULT 5;

ALTER TABLE organization_policies
    ADD sick_leaves_allowed INTEGER DEFAULT 5;

ALTER TABLE organization_policies
    ALTER COLUMN annual_leaves_allowed SET NOT NULL;

ALTER TABLE users
    ADD remaining_annual_leaves INTEGER DEFAULT 5;

ALTER TABLE users
    ADD remaining_sick_leaves INTEGER DEFAULT 5;

ALTER TABLE users
    ALTER COLUMN remaining_annual_leaves SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN remaining_sick_leaves SET NOT NULL;

ALTER TABLE organization_policies
    ALTER COLUMN sick_leaves_allowed SET NOT NULL;

ALTER TABLE leave
    ADD CONSTRAINT uc_leave_user UNIQUE (user_id);

ALTER TABLE leave
    ADD CONSTRAINT FK_LEAVE_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);