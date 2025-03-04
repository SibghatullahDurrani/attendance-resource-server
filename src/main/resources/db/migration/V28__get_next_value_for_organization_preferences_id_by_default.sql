ALTER TABLE organization_preferences
    ALTER COLUMN id SET DEFAULT nextval('organization_id_sequence'::regclass)
