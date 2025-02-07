CREATE SEQUENCE IF NOT EXISTS organization_id_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE organizations
(
    id                BIGINT       NOT NULL,
    organization_name VARCHAR(255) NOT NULL,
    organization_type VARCHAR(255) NOT NULL,
    CONSTRAINT pk_organizations PRIMARY KEY (id)
);