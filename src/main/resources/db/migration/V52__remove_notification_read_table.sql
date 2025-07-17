ALTER TABLE notifications_read_by_users
    DROP CONSTRAINT fk_notreabyuse_on_notification;

ALTER TABLE notifications_read_by_users
    DROP CONSTRAINT fk_notreabyuse_on_user;

DROP TABLE notifications_read_by_users CASCADE;