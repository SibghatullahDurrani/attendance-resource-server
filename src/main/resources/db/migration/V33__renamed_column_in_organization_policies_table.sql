ALTER TABLE organization_policies
    ADD late_attendance_tolerance_time_in_minutes INTEGER;

ALTER TABLE organization_policies
    ALTER COLUMN late_attendance_tolerance_time_in_minutes SET NOT NULL;

ALTER TABLE organization_policies
    DROP COLUMN minutes_till_attendance_counts_as_late;