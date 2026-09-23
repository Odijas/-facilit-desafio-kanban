# F3-L1 — GATE

Data: 2026-09-22

Estado: **GREEN** (rev4, gate local em 2026-09-23). Histórico: rev2 RED (2 falhas em `KanbanApiIT`); rev3 RED (variável GraphQL do gate com tipo incompatível).

Critérios:

- `F3_L1_RECONCILIATION_GREEN` (órfão ausente e repositório Git presente);
- `F3_L1_LIST_UNFILTERED_GREEN` (listagem sem filtros e filtro de texto isolado);
- frontend formatado, lint/typecheck/test/build GREEN;
- backend `mvn clean verify` GREEN;
- Docker Compose e health GREEN;
- autenticação/CSRF pela origem frontend GREEN;
- CRUD real de Secretaria e bloqueio 409 enquanto referenciada;
- filtros combinados por secretaria, responsável, período e texto retornando somente o projeto esperado;
- indicadores REST e GraphQL respondendo valores coerentes;
- filtro GraphQL exercitado;
- cleanup funcional;
- scans estritos e `git diff --check` GREEN;
- marcador final `=== F3-L1 GREEN ===` e exit code 0.

Evidência final recebida do usuário:

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] gate rev4 (trecho recebido a partir do health):
  health => {"status":"UP"}
  F3_L1_AUTH_GREEN
  F3_L1_SECRETARIAT_CREATE_GREEN
  F3_L1_RESPONSIBLE_GREEN
  F3_L1_PROJECT_FIXTURES_GREEN
  F3_L1_LIST_UNFILTERED_GREEN
  F3_L1_FILTERS_REST_GREEN
  F3_L1_FILTER_VALIDATION_GREEN
  F3_L1_INDICATORS_REST_GREEN
  F3_L1_GRAPHQL_GREEN
  F3_L1_SECRETARIAT_UPDATE_GREEN
  F3_L1_SECRETARIAT_BLOCK_GREEN
  F3_L1_CRUD_GREEN
  F3_L1_STRICT_TYPES_GREEN 30
  docker compose ps: db healthy; backend e frontend up
  F3_L1_STATIC_GREEN
  === F3-L1 GREEN ===
  Resultado: exit code 0
[VERIFICADO: script com `set -euo pipefail`] F3_L1_RECONCILIATION_GREEN, F3_L1_FRONTEND_GREEN e F3_L1_BACKEND_GREEN precedem o Docker Compose; o exit code 0 final só ocorre se todas as etapas anteriores terminarem com sucesso. Os três marcadores não constam do trecho recebido.
[VERIFICADO: script] OpenAPI (`/api/v1/secretariats`, `/api/v1/indicators/projects`), ausência de localStorage/sessionStorage e `git diff --check` executam antes de F3_L1_STATIC_GREEN.
```

Conclusão: F3-L1 promovido para GREEN em 2026-09-23.
