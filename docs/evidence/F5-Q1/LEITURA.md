# F5-Q1 — LIVRO-RAZÃO DE LEITURA

Data: 2026-09-25

Base: `facilit-desafio-kanban-entrega-20260925-094920.tar.gz`, SHA-256 `27f370b2fdc51b40bc4f9532a3dd671130b9e1b93372347d9d8b40fba7211318`.

## Lidos integralmente antes da alteração

- `docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md`.
- `docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md`.
- `docs/governance/PLANO-CONFORMIDADE-F5.md`.
- `PLANO-CORRECAO-RELEASE-2.0.2.md` fornecido pelo usuário; depois copiado sem alteração para `docs/governance/PLANO-CORRECAO-RELEASE-2.0.2.md`.
- `backend/pom.xml`, `backend/Dockerfile`, `frontend/package.json`.
- `backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java`.
- `backend/src/main/java/br/com/facilit/kanban/application/project/ProjectRepository.java`.
- `backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java`.
- `backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaRepository.java`.
- `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java`.
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ApiErrorDocumentation.java`.
- `backend/src/main/java/br/com/facilit/kanban/delivery/common/ApiExamples.java`.
- `backend/src/main/java/br/com/facilit/kanban/application/common/Actor.java`.
- `backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleService.java`.
- `backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleCredentialService.java`.
- `backend/src/main/java/br/com/facilit/kanban/application/secretariat/SecretariatService.java`.
- `backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java`.
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRequest.java`.
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java` (reaberto integralmente após o RED 1).
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectStatusRequest.java`.
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleRequest.java`.
- `compose.yaml`.
- testes diretamente afetados: `ProjectServiceTest`, `ProjectServiceMockitoTest`, `ProjectScheduleRefresherTest`, `InMemoryProjectRepository`, `ProjectStatusTransitionTest`, `OpenApiContractIT`, `ProjectPersistenceAdapterIT`, `ScheduleFreshnessIT`.

## Releitura integral depois das alterações

Todos os arquivos de produção e teste alterados no lote foram relidos do início ao fim. Após o RED 1, `OpenApiContractIT.java` e `VERIFICACAO-USUARIO.md` foram relidos integralmente depois da correção, e `ProjectRestController.java` foi relido integralmente para confirmar a origem do exemplo 400 já declarado. O plano copiado foi comparado byte a byte com o arquivo fornecido (`cmp_exit=0`). Os três arquivos de versão foram relidos integralmente.

## Amostragem

- Nenhuma amostragem é usada como fundamento para uma afirmação sobre arquivo alterado.
- Arquivos históricos de evidência F5-P1/F5-P3 foram consultados apenas como modelo de gate e para a contagem GREEN da v2.0.1; não foram alterados.
