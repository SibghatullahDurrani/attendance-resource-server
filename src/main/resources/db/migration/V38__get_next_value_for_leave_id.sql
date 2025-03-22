ALTER TABLE leaves
    ALTER COLUMN id SET DEFAULT nextval('leave_id_sequence'::regclass)