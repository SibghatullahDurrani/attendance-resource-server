ALTER TABLE notifications
    ALTER COLUMN attachment_id DROP NOT NULL;

ALTER TABLE check_ins
    ALTER COLUMN face_image_name DROP NOT NULL;

ALTER TABLE check_ins
    ALTER COLUMN full_image_name DROP NOT NULL;