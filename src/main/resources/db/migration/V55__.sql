ALTER TABLE attachments
    ADD CONSTRAINT uc_attachments_notification UNIQUE (notification_id);