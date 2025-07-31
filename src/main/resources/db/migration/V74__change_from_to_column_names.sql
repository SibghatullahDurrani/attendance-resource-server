ALTER TABLE user_shift_settings
    RENAME COLUMN "from" TO start_date;

ALTER TABLE user_shift_settings
    RENAME COLUMN "to" TO end_date;
