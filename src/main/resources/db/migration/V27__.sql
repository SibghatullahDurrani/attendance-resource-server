ALTER TABLE organizations
    ADD organization_preferences_id BIGINT;

ALTER TABLE organizations
    ADD CONSTRAINT uc_organizations_organization_preferences UNIQUE (organization_preferences_id);

ALTER TABLE organizations
    ADD CONSTRAINT FK_ORGANIZATIONS_ON_ORGANIZATION_PREFERENCES FOREIGN KEY (organization_preferences_id) REFERENCES organization_preferences (id);