ALTER TABLE cameras
    ADD channel INTEGER;

ALTER TABLE cameras
    ALTER COLUMN channel SET NOT NULL;