DROP TABLE cameras CASCADE;

ALTER TABLE cameras
    DROP CONSTRAINT fk_cameras_on_organization;