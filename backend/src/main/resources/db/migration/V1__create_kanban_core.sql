CREATE TABLE secretariats (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL CONSTRAINT ck_secretariats_name_not_blank CHECK (btrim(name) <> ''),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_secretariats_audit_order CHECK (updated_at >= created_at)
);

CREATE TABLE responsibles (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL CONSTRAINT ck_responsibles_name_not_blank CHECK (btrim(name) <> ''),
    email TEXT NOT NULL CONSTRAINT ck_responsibles_email_not_blank CHECK (btrim(email) <> ''),
    position TEXT NOT NULL CONSTRAINT ck_responsibles_position_not_blank CHECK (btrim(position) <> ''),
    secretariat_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_responsibles_email UNIQUE (email),
    CONSTRAINT fk_responsibles_secretariat FOREIGN KEY (secretariat_id) REFERENCES secretariats (id),
    CONSTRAINT ck_responsibles_audit_order CHECK (updated_at >= created_at)
);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL CONSTRAINT ck_projects_name_not_blank CHECK (btrim(name) <> ''),
    status TEXT NOT NULL,
    planned_start DATE,
    planned_end DATE,
    actual_start DATE,
    actual_end DATE,
    delay_days BIGINT NOT NULL,
    remaining_time_percentage SMALLINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_projects_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'OVERDUE', 'COMPLETED')),
    CONSTRAINT ck_projects_planned_dates CHECK (
        planned_start IS NULL OR planned_end IS NULL OR planned_end >= planned_start
    ),
    CONSTRAINT ck_projects_actual_dates CHECK (
        actual_start IS NULL OR actual_end IS NULL OR actual_end >= actual_start
    ),
    CONSTRAINT ck_projects_delay_days CHECK (delay_days >= 0),
    CONSTRAINT ck_projects_remaining_percentage CHECK (remaining_time_percentage BETWEEN 0 AND 100),
    CONSTRAINT ck_projects_audit_order CHECK (updated_at >= created_at)
);

CREATE TABLE project_responsibles (
    project_id UUID NOT NULL,
    responsible_id UUID NOT NULL,
    PRIMARY KEY (project_id, responsible_id),
    CONSTRAINT fk_project_responsibles_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_responsibles_responsible FOREIGN KEY (responsible_id) REFERENCES responsibles (id)
);

CREATE INDEX ix_responsibles_secretariat_id ON responsibles (secretariat_id);
CREATE INDEX ix_projects_status ON projects (status);
CREATE INDEX ix_projects_planned_start ON projects (planned_start);
CREATE INDEX ix_projects_planned_end ON projects (planned_end);
CREATE INDEX ix_project_responsibles_responsible_id ON project_responsibles (responsible_id);
