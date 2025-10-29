ALTER TABLE attendances
    ALTER COLUMN date TYPE timestamptz
        USING date AT TIME ZONE 'Asia/Karachi';

ALTER TABLE attendances
    ALTER COLUMN date SET NOT NULL;