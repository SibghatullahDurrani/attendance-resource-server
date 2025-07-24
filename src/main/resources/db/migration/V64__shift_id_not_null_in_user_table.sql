INSERT INTO organizations (id, organization_name, organization_type, organization_policies_id)
VALUES (1, 'Default Organization', 'General', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO shifts (id, name, check_in_time, check_out_time, organization_id)
VALUES (1, 'Default Shift', '09:00', '17:00', 1)
ON CONFLICT (id) DO NOTHING;

UPDATE users
SET shift_id = 1
WHERE shift_id IS NULL;

ALTER TABLE users
    ALTER COLUMN shift_id SET NOT NULL;