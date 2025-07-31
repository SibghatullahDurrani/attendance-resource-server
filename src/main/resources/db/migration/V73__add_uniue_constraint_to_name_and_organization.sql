ALTER TABLE shifts
    ADD CONSTRAINT uk_shift_name_organization UNIQUE (name, organization_id);