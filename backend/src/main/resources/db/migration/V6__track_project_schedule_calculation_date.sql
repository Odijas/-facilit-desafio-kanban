-- Data de referência ("hoje") do último cálculo de status, dias de atraso e % de tempo restante.
-- Os projetos não concluídos calculados antes de hoje são recalculados pela aplicação (ADR 0002).
ALTER TABLE projects ADD COLUMN schedule_calculated_on DATE;

-- Até a v1.0.0 o cálculo acontecia só ao gravar, com o relógio em UTC. A data do cálculo é,
-- portanto, a data UTC da última gravação.
UPDATE projects SET schedule_calculated_on = (updated_at AT TIME ZONE 'UTC')::date;

ALTER TABLE projects ALTER COLUMN schedule_calculated_on SET NOT NULL;

-- Atende a busca de desatualizados (status IN (...) AND schedule_calculated_on < hoje).
CREATE INDEX ix_projects_status_schedule_calculated_on ON projects (status, schedule_calculated_on);
