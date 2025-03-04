ALTER TABLE organizations
    DROP CONSTRAINT fk_organizations_on_organization_preferences;

CREATE TABLE organization_policies
(
    id                                     BIGINT  NOT NULL,
    check_in_time_for_user                 TIMESTAMP WITHOUT TIME ZONE,
    check_out_time_for_user                TIMESTAMP WITHOUT TIME ZONE,
    minutes_till_attendance_counts_as_late INTEGER NOT NULL,
    retake_attendance_in_hour              INTEGER NOT NULL,
    check_out_tolerance_time_in_hour       INTEGER NOT NULL,
    CONSTRAINT pk_organization_policies PRIMARY KEY (id)
);

ALTER TABLE organizations
    ADD organization_policies_id BIGINT;

ALTER TABLE organizations
    ADD CONSTRAINT uc_organizations_organization_policies UNIQUE (organization_policies_id);

ALTER TABLE organizations
    ADD CONSTRAINT FK_ORGANIZATIONS_ON_ORGANIZATION_POLICIES FOREIGN KEY (organization_policies_id) REFERENCES organization_policies (id);

DROP TABLE organization_preferences CASCADE;

ALTER TABLE organizations
    DROP COLUMN organization_preferences_id;