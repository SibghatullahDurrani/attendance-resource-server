CREATE SEQUENCE IF NOT EXISTS department_id_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE departments
(
    id              BIGINT       NOT NULL,
    department_name VARCHAR(255) NOT NULL,
    organization_id BIGINT       NOT NULL,
    CONSTRAINT pk_departments PRIMARY KEY (id)
);

ALTER TABLE departments
    ADD CONSTRAINT FK_DEPARTMENTS_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);