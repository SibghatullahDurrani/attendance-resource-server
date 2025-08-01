ALTER TABLE users
    ADD is_saved_in_producer BOOLEAN;

ALTER TABLE users
    ADD last_saved_in_producer_date TIMESTAMP WITHOUT TIME ZONE;

UPDATE users
SET is_saved_in_producer= FALSE
WHERE is_saved_in_producer IS NULL;

ALTER TABLE users
    ALTER COLUMN is_saved_in_producer SET NOT NULL;

ALTER TABLE rabbitmq_message_backups
    ADD message_type VARCHAR(255);

UPDATE rabbitmq_message_backups
SET message_type='SHIFT'
WHERE message_type IS NULL;

ALTER TABLE rabbitmq_message_backups
    ALTER COLUMN message_type SET NOT NULL;