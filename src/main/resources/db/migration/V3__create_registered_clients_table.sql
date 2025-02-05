CREATE TABLE registered_clients
(
    id            BIGINT NOT NULL,
    client_id     VARCHAR(30),
    client_secret VARCHAR(30),
    redirect_uri  VARCHAR(500),
    CONSTRAINT pk_registered_clients PRIMARY KEY (id)
);

ALTER TABLE users
    ALTER COLUMN role TYPE VARCHAR(30) USING (role::VARCHAR(30));