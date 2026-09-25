# F5-L5 — MATRIZ DE FECHAMENTO

Data: 2026-09-24

| Escopo | Evidência anterior | Revalidação no L5 | Estado |
|---|---|---|---|
| Regras de status/métricas sempre atuais | F5-L1 GREEN | backend + Docker/API | GREEN promovido |
| Contrato 422, confirmação, OpenAPI e logs | F5-L2 GREEN/revalidado | API + Swagger + logs | GREEN promovido |
| Controllers com Mockito, transações e concorrência | F5-L3 GREEN | `mvn clean verify` | GREEN promovido |
| Etapa 3, BDD e cobertura ≥95% | F5-L4 GREEN | `mvn clean verify` + JaCoCo/BDD + API | GREEN promovido |
| Frontend | F5-L2 + CI | pnpm format/lint/typecheck/strict/test/build | GREEN (freeze L5) |
| Docker/migrations V1–V7 | F5-L3 | banco novo no Compose | GREEN (freeze L5) |
| Segurança/observabilidade | F4/F3-L3 | checks executáveis + Prometheus/Grafana | GREEN (freeze L5) |
| Documentação obrigatória | F4 + F5 | README, AI_USAGE, ADR, CHANGELOG, auditoria, links | GREEN (freeze L5) |
| Histórico/repositório/CI | F4 + lotes F5 | Conventional Commits, segredos, CI release/main | GREEN (freeze L5) |
| Release 2.0.0 | — | Gitflow + tag + refs + CI main | GREEN (freeze L5) |
