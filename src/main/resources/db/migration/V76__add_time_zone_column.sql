ALTER TABLE organizations
    ADD COLUMN time_zone VARCHAR(255);

UPDATE organizations
SET time_zone = 'Asia/Karachi'
WHERE time_zone IS NULL;

ALTER TABLE organizations
    ALTER COLUMN time_zone SET NOT NULL;
