ALTER TABLE registered_clients
    ALTER COLUMN client_secret TYPE VARCHAR(255) USING (client_secret::VARCHAR(255));