ALTER TABLE check_outs
    ALTER COLUMN face_image_name DROP NOT NULL;

ALTER TABLE check_outs
    ALTER COLUMN full_image_name DROP NOT NULL;