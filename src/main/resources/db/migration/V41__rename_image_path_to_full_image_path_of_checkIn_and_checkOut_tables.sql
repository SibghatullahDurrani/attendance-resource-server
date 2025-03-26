ALTER TABLE check_ins
    RENAME COLUMN image_path TO full_image_path;
ALTER TABLE check_outs
    RENAME COLUMN image_path TO full_image_path;
