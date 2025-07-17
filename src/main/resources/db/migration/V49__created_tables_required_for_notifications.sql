CREATE SEQUENCE IF NOT EXISTS attachment_id_sequence START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE IF NOT EXISTS notification_id_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE attachments
(
    id              BIGINT       NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    file_path       VARCHAR(255) NOT NULL,
    notification_id BIGINT       NOT NULL,
    CONSTRAINT pk_attachments PRIMARY KEY (id)
);

CREATE TABLE notifications
(
    id      BIGINT       NOT NULL,
    title   VARCHAR(255),
    message VARCHAR(255) NOT NULL,
    status  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id)
);

CREATE TABLE notifications_users
(
    notification_id BIGINT NOT NULL,
    user_id         BIGINT NOT NULL
);

ALTER TABLE attachments
    ADD CONSTRAINT FK_ATTACHMENTS_ON_NOTIFICATION FOREIGN KEY (notification_id) REFERENCES notifications (id);

ALTER TABLE notifications_users
    ADD CONSTRAINT fk_notuse_on_notification FOREIGN KEY (user_id) REFERENCES notifications (id);

ALTER TABLE notifications_users
    ADD CONSTRAINT fk_notuse_on_user FOREIGN KEY (notification_id) REFERENCES users (id);