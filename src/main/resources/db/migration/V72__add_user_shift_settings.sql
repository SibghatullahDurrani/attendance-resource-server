CREATE SEQUENCE IF NOT EXISTS user_shift_settings_id_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE user_shift_settings
(
    id         BIGINT       NOT NULL,
    shift_mode varchar(255) NOT NULL,
    "from"     TIMESTAMP WITHOUT TIME ZONE,
    "to"       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_user_shift_settings PRIMARY KEY (id)
);

ALTER TABLE users
    ADD user_shift_setting_id BIGINT;

INSERT INTO user_shift_settings (id, shift_mode, "from", "to")
SELECT nextval('user_shift_settings_id_sequence'),
       'PERMANENT',
       NULL,
       NULL
FROM users;

UPDATE users
SET user_shift_setting_id = (SELECT id
                             FROM user_shift_settings
                             WHERE user_shift_settings.id > (SELECT COALESCE(MAX(id), 0)
                                                             FROM user_shift_settings
                                                             WHERE id < (SELECT MIN(id)
                                                                         FROM user_shift_settings
                                                                         WHERE id > (SELECT COALESCE(MAX(user_shift_setting_id), 0) FROM users)))
                               AND NOT EXISTS (SELECT 1
                                               FROM users u2
                                               WHERE u2.user_shift_setting_id = user_shift_settings.id)
                             LIMIT 1);

ALTER TABLE users
    ALTER COLUMN user_shift_setting_id SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT uc_users_user_shift_setting UNIQUE (user_shift_setting_id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_USER_SHIFT_SETTING FOREIGN KEY (user_shift_setting_id) REFERENCES user_shift_settings (id);