ALTER TABLE cameras
    ADD organization_id BIGINT;

ALTER TABLE cameras
    ADD CONSTRAINT FK_CAMERAS_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);