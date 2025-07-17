ALTER TABLE notifications_users
    DROP CONSTRAINT fk_notuse_on_notification;

ALTER TABLE notifications_users
    DROP CONSTRAINT fk_notuse_on_user;

DROP TABLE notifications_users CASCADE;