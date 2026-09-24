-- Controle de concorrência otimista (JPA @Version): uma edição que parte de uma versão antiga do projeto é
-- recusada (409 CONFLICT) em vez de sobrescrever a alteração de outra operação.
ALTER TABLE projects ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
