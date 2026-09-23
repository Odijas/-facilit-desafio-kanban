# F3-L1 — GATE

Data: 2026-09-22

Estado: **CANDIDATE rev4 / aguardando execução local** (rev2 RED: 2 falhas em `KanbanApiIT`; rev3 RED: variável GraphQL do gate com tipo incompatível).

GREEN exige:

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
