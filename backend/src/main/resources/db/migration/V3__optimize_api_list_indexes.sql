DROP INDEX IF EXISTS ix_projects_status;

CREATE INDEX ix_projects_name_id ON projects (name, id);
CREATE INDEX ix_projects_status_name_id ON projects (status, name, id);
CREATE INDEX ix_responsibles_name_id ON responsibles (name, id);
