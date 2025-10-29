ALTER TABLE check_ins
    ALTER COLUMN date TYPE timestamptz
        USING date AT TIME ZONE 'Asia/Karachi';

ALTER TABLE check_ins
    ALTER COLUMN date SET NOT NULL;