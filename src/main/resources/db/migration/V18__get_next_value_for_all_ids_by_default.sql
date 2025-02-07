ALTER TABLE organizations
    ALTER COLUMN id SET DEFAULT nextval('organization_id_sequence'::regclass);
ALTER TABLE attendances
    ALTER COLUMN id SET DEFAULT nextval('attendance_id_sequence'::regclass);
ALTER TABLE cameras
    ALTER COLUMN id SET DEFAULT nextval('camera_id__sequence'::regclass);
ALTER TABLE check_ins
    ALTER COLUMN id SET DEFAULT nextval('checkin_id_sequence'::regclass);
ALTER TABLE check_outs
    ALTER COLUMN id SET DEFAULT nextval('checkout_id_sequence'::regclass);
ALTER TABLE departments
    ALTER COLUMN id SET DEFAULT nextval('department_id_sequence'::regclass);