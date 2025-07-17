ALTER TABLE attachments
    DROP CONSTRAINT fk_attachments_on_notification;

ALTER TABLE notifications
    ADD attachment_id BIGINT;

ALTER TABLE notifications
    ALTER COLUMN attachment_id SET NOT NULL;

ALTER TABLE notifications
    ADD CONSTRAINT uc_notifications_attachment UNIQUE (attachment_id);

ALTER TABLE notifications
    ADD CONSTRAINT FK_NOTIFICATIONS_ON_ATTACHMENT FOREIGN KEY (attachment_id) REFERENCES attachments (id);

ALTER TABLE attachments
    DROP COLUMN notification_id;