# F1-L1 — MATRIZ requisito → implementação → teste → evidência

| Requisito | Implementação | Teste/verificação | Estado |
|---|---|---|---|
| CRUD Responsável | `ResponsibleService`, adapter JPA, REST e GraphQL | `ResponsibleServiceTest` + gate REST/GraphQL real | GREEN |
| CRUD Projeto | `ProjectService`, adapter JPA, REST e GraphQL | `ProjectServiceTest` + gate REST/GraphQL real | GREEN |
| REST | `/api/v1/responsibles`, `/api/v1/projects` | curls do gate | GREEN |
| GraphQL | queries/mutations em `kanban.graphqls` | chamadas GraphQL do gate | GREEN |
| E-mail único | normalização no domínio + precheck + índice V2 `lower(email)` | unitário + duplicidade REST/GraphQL | GREEN |
| Auditoria | `AuditMetadata` preserva `createdAt` e atualiza `updatedAt` | testes de serviço | GREEN |
| Paginação | `PageQuery`/`PageResult` + Spring Data `PageRequest` | unitário + listagens REST/GraphQL | GREEN |
| Recalcular métricas ao criar/editar Projeto | `ProjectService` + `ProjectScheduleCalculator` | `ProjectServiceTest` + runtime | GREEN |
| FK Secretaria quando informada | `SecretariatRepository` + FK V1 | serviço + banco real no startup | GREEN |
| Bloquear exclusão de Responsável em uso | `ProjectRepository.existsByResponsibleId` | unitário + HTTP 409 | GREEN |
