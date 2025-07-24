ALTER TABLE shifts
    ADD is_saved_in_producer BOOLEAN;

ALTER TABLE shifts
    ADD last_saved_in_producer_date TIMESTAMP WITHOUT TIME ZONE;

UPDATE shifts
SET is_saved_in_producer = FALSE
WHERE shifts.is_saved_in_producer IS NULL;

ALTER TABLE shifts
    ALTER COLUMN is_saved_in_producer SET NOT NULL;