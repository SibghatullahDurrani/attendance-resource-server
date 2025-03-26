ALTER TABLE check_ins
    ADD face_image_path VARCHAR(255);

UPDATE check_ins
SET face_image_path = full_image_path;

ALTER TABLE check_ins
    ALTER COLUMN face_image_path SET NOT NULL;

ALTER TABLE check_outs
    ADD face_image_path VARCHAR(255);

UPDATE check_outs
SET face_image_path = full_image_path;

ALTER TABLE check_outs
    ALTER COLUMN face_image_path SET NOT NULL;