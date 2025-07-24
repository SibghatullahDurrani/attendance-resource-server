ALTER TABLE shifts
    ADD is_default BOOLEAN;

UPDATE shifts
SET is_default = FALSE
WHERE is_default IS NULL;

ALTER TABLE shifts
    ALTER COLUMN is_default SET NOT NULL;