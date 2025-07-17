CREATE TABLE notifications_read_by_users
(
    notification_id BIGINT NOT NULL,
    user_id         BIGINT NOT NULL
);

ALTER TABLE notifications_read_by_users
    ADD CONSTRAINT fk_notreabyuse_on_notification FOREIGN KEY (user_id) REFERENCES notifications (id);

ALTER TABLE notifications_read_by_users
    ADD CONSTRAINT fk_notreabyuse_on_user FOREIGN KEY (notification_id) REFERENCES users (id);