ALTER TABLE check_ins
    RENAME COLUMN full_image_path TO full_image_name;
ALTER TABLE check_ins
    RENAME COLUMN face_image_path TO face_image_name;

ALTER TABLE check_outs
    RENAME COLUMN full_image_path TO full_image_name;
ALTER TABLE check_outs
    RENAME COLUMN face_image_path TO face_image_name;

ALTER TABLE users
    RENAME COLUMN profile_picture_path TO profile_picture_name;
ALTER TABLE users
    RENAME COLUMN source_face_picture_path TO source_face_picture_name;
