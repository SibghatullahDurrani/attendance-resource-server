ALTER TABLE check_ins
    ADD image_path VARCHAR(255);

ALTER TABLE check_ins
    ALTER COLUMN image_path SET NOT NULL;

ALTER TABLE check_outs
    ADD image_path VARCHAR(255);

ALTER TABLE check_outs
    ALTER COLUMN image_path SET NOT NULL;