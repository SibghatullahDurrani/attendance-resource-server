CREATE TABLE rabbitmq_message_backups
(
    id             UUID         NOT NULL,
    message        TEXT         NOT NULL,
    message_status VARCHAR(255) NOT NULL,
    CONSTRAINT pk_rabbitmq_message_backups PRIMARY KEY (id)
);