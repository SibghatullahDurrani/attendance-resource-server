ALTER TABLE leaves
    ADD date TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE leaves
    ALTER COLUMN date SET NOT NULL;