ALTER TABLE registered_clients
    DROP COLUMN id;

ALTER TABLE registered_clients
    ADD id VARCHAR(255) NOT NULL PRIMARY KEY;