ALTER TABLE users
    ALTER COLUMN id SET DEFAULT nextval('user_id_sequence'::regclass)