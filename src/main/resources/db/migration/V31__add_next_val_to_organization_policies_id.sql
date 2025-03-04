ALTER TABLE organization_policies
    ALTER COLUMN id SET DEFAULT nextval('organization_policies_id_sequence'::regclass)