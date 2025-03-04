ALTER TABLE organization_policies
    DROP COLUMN check_in_time_for_user;

ALTER TABLE organization_policies
    DROP COLUMN check_out_time_for_user;

ALTER TABLE organization_policies
    ADD check_in_time_for_user VARCHAR(255);

ALTER TABLE organization_policies
    ADD check_out_time_for_user VARCHAR(255);