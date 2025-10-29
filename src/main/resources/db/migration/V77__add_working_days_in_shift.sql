CREATE TABLE shift_working_days
(
    shift_id BIGINT       NOT NULL,
    day      VARCHAR(255) NOT NULL,
    CONSTRAINT pk_shift_working_days PRIMARY KEY (shift_id, day)
);

ALTER TABLE shift_working_days
    ADD CONSTRAINT fk_shift_working_days_on_shift FOREIGN KEY (shift_id) REFERENCES shifts (id) ON DELETE CASCADE;

INSERT INTO shift_working_days (shift_id, day)
SELECT s.id, wd.day
FROM shifts s
         CROSS JOIN (VALUES ('MONDAY'),
                            ('TUESDAY'),
                            ('WEDNESDAY'),
                            ('THURSDAY'),
                            ('FRIDAY')) AS wd(day);