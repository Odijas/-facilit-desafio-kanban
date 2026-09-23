# F1-L3 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-22

| Requisito | Implementação | Verificação | Estado |
|---|---|---|---|
| Erros padronizados REST | `ApiErrorCode` + `RestExceptionHandler` + `ProblemDetail` | `KanbanApiIT` 400/404/409 | GREEN |
| Erros padronizados GraphQL | `GraphQlErrorHandler` + `extensions.code` | `KanbanApiIT` validação/data inválida | GREEN |
| Validação de input REST | Bean Validation nos request records | e-mail inválido real | GREEN |
| Validação de input GraphQL | `@Argument @Valid` + constraints | mutation inválida real | GREEN |
| Paginação | `PageQuery`/`PageResult` já prevalentes | testes de aplicação + API | GREEN |
| Filtro necessário | status do Kanban já prevalente | REST + GraphQL | GREEN F1-L2 |
| Índices coerentes | migration V3: ordenação/filtro reais | consulta `pg_indexes` no Testcontainers | GREEN |
| Swagger/OpenAPI | springdoc 2.8.17 + annotations + config | `/api-docs` e `/swagger-ui.html` | GREEN |
| Exemplos request/response | annotations OpenAPI nos CRUDs | inspeção `/api-docs` | GREEN |
| Testes unitários | domínio/aplicação prevalentes | Surefire via `mvn verify` | GREEN |
| Teste integração DB | `KanbanApiIT` + PostgreSQL 18.6 | Failsafe, migration e índices reais | GREEN |
| Teste API REST | `KanbanApiIT` | CRUD/erros/filtro/transição | GREEN |
| Teste GraphQL | `KanbanApiIT` | transição, filtro e validação | GREEN |
| Reuso REST/GraphQL | ambos chamam os mesmos services | fluxo cruzado GraphQL→REST no IT | GREEN |
