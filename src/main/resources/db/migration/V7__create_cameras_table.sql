CREATE SEQUENCE IF NOT EXISTS camera_id__sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE cameras
(
    id         BIGINT       NOT NULL,
    ip_address VARCHAR(15)  NOT NULL,
    port       INTEGER      NOT NULL,
    username   VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_cameras PRIMARY KEY (id)
);

ALTER TABLE cameras
    ADD CONSTRAINT uc_cameras_ip_address UNIQUE (ip_address);