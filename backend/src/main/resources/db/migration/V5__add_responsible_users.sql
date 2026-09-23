ALTER TABLE app_users DROP CONSTRAINT ck_app_users_role;

ALTER TABLE app_users ADD COLUMN responsible_id UUID;

ALTER TABLE app_users
    ADD CONSTRAINT fk_app_users_responsible
        FOREIGN KEY (responsible_id) REFERENCES responsibles (id) ON DELETE CASCADE;

ALTER TABLE app_users
    ADD CONSTRAINT uq_app_users_responsible UNIQUE (responsible_id);

ALTER TABLE app_users
    ADD CONSTRAINT ck_app_users_role CHECK (role IN ('ADMIN', 'RESPONSIBLE'));

ALTER TABLE app_users
    ADD CONSTRAINT ck_app_users_responsible_link CHECK ((role = 'RESPONSIBLE') = (responsible_id IS NOT NULL));
