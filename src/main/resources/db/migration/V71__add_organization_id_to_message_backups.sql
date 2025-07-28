ALTER TABLE rabbitmq_message_backups
    ADD organization_id BIGINT;

UPDATE rabbitmq_message_backups
SET organization_id=1
WHERE organization_id IS NULL;

ALTER TABLE rabbitmq_message_backups
    ALTER COLUMN organization_id SET NOT NULL;

DROP SEQUENCE camera_id__sequence CASCADE;