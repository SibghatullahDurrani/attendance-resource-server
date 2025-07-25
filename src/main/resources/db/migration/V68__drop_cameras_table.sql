ALTER TABLE cameras
    DROP CONSTRAINT fk_cameras_on_organization;

DROP TABLE cameras CASCADE;