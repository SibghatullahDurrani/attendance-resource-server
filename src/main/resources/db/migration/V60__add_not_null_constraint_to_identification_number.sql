UPDATE users
SET identification_number = '0'
WHERE identification_number IS NULL;

ALTER TABLE users
    ALTER COLUMN email DROP NOT NULL;

ALTER TABLE users
    ALTER COLUMN identification_number SET NOT NULL;