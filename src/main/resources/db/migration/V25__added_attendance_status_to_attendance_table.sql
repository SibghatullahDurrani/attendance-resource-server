ALTER TABLE attendances
    ADD status VARCHAR(255);

ALTER TABLE attendances
    ALTER COLUMN status SET NOT NULL;