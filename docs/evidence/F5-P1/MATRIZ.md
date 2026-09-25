# F5-P1 — MATRIZ LACUNA → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-25

| Lacuna (`ADERENCIA-V2.0.0.md`) | Requisito (PDF) | Implementação | Teste | Evidência | Estado |
|---|---|---|---|---|---|
| 2 — exemplos de resposta no Swagger | "Swagger/OpenAPI com exemplos de requests/responses, schemas, e mensagens de erro" (obrigatório #40) | `@Schema(example)` em todos os records de request/resposta, exemplo no item das 3 listas (`ApiListExampleDocumentation`), `@Parameter(example)` em 25 parâmetros, `@ApiResponse` no health, `ApiExamples` | `OpenApiContractIT.everyRestOperationHasExamplesForParametersRequestBodyAndSuccessResponses` | `F5_P1_API_GREEN`: regra aplicada ao `/api-docs` real, 26 operações e 0 faltas. Conferência no Swagger UI (seção 3). | GREEN |
| 8 (parte) — `LIKE` sem escapar `%`/`_` (decisão 1 do usuário) | "Filtros avançados" (diferencial D4); correção de comportamento errado | `containsPattern` + `like(…, LIKE_ESCAPE)` | `ProjectPersistenceAdapterIT.searchTreatsPercentUnderscoreAndBackslashInTheTextAsLiterals` | `F5_P1_API_GREEN`: buscas `100%`, `%`, `_`, `lote_a` e `meta` (REST) e `_` (GraphQL) | GREEN |
| — | versionamento da patch release | `2.0.1` em pom, Dockerfile e package.json | build com `kanban-2.0.1.jar` | `F5_P1_BACKEND_GREEN`, `F5_P1_STATIC_GREEN` e `F5_P1_DOCKER_GREEN` | GREEN |
