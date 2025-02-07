CREATE TABLE departments_cameras
(
    camera_id     BIGINT NOT NULL,
    department_id BIGINT NOT NULL
);

ALTER TABLE departments_cameras
    ADD CONSTRAINT fk_depcam_on_camera FOREIGN KEY (department_id) REFERENCES cameras (id);

ALTER TABLE departments_cameras
    ADD CONSTRAINT fk_depcam_on_department FOREIGN KEY (camera_id) REFERENCES departments (id);