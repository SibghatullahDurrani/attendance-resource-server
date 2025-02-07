ALTER TABLE users
    ADD email VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE users
    ADD identification_number VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE users
    ALTER COLUMN email DROP DEFAULT;
ALTER TABLE users
    ALTER COLUMN identification_number DROP DEFAULT;
