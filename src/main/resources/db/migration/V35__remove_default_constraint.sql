ALTER TABLE users
    ALTER COLUMN remaining_annual_leaves DROP DEFAULT;

ALTER TABLE users
    ALTER COLUMN remaining_sick_leaves DROP DEFAULT;

ALTER TABLE organization_policies
    ALTER COLUMN annual_leaves_allowed DROP DEFAULT;

ALTER TABLE organization_policies
    ALTER COLUMN sick_leaves_allowed DROP DEFAULT
