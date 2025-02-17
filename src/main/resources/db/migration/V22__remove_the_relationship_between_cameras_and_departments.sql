ALTER TABLE departments_cameras
    DROP CONSTRAINT fk_depcam_on_camera;

ALTER TABLE departments_cameras
    DROP CONSTRAINT fk_depcam_on_department;

DROP TABLE departments_cameras CASCADE;