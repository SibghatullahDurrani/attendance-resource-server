CREATE SEQUENCE IF NOT EXISTS notification_user_id_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE users_notifications
(
    id              BIGINT NOT NULL,
    user_id         BIGINT,
    notification_id BIGINT,
    status          VARCHAR(255),
    CONSTRAINT pk_users_notifications PRIMARY KEY (id)
);

ALTER TABLE users_notifications
    ADD CONSTRAINT FK_USERS_NOTIFICATIONS_ON_NOTIFICATION FOREIGN KEY (notification_id) REFERENCES notifications (id);

ALTER TABLE users_notifications
    ADD CONSTRAINT FK_USERS_NOTIFICATIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);