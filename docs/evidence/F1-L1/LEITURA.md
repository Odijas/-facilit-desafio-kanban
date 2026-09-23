# F1-L1 — Livro-razão de leitura

Data: 2026-09-22

## Lidos integralmente — governança e requisito

- `docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md`
- `docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md`
- desafio técnico fornecido pelo usuário, integralmente lido antes da execução da F1.

## Lidos integralmente — baseline diretamente relacionada

- `README.md`
- `backend/pom.xml`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/migration/V1__create_kanban_core.sql`
- `backend/src/main/resources/graphql/health.graphqls`
- `backend/src/main/java/br/com/facilit/kanban/domain/common/AuditMetadata.java`
- `backend/src/main/java/br/com/facilit/kanban/domain/project/Project.java`
- `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectDates.java`
- `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java`
- `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleMetrics.java`
- `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatus.java`
- `backend/src/main/java/br/com/facilit/kanban/domain/responsible/Responsible.java`
- `backend/src/main/java/br/com/facilit/kanban/domain/secretariat/Secretariat.java`
- `backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java`
- `backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectTest.java`
- `backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculatorTest.java`
- `compose.yaml`

## Lidos integralmente — criados ou alterados no F1-L1

- todos os arquivos de `backend/src/main/java/br/com/facilit/kanban/application/common/`;
- todos os arquivos de `backend/src/main/java/br/com/facilit/kanban/application/project/`;
- todos os arquivos de `backend/src/main/java/br/com/facilit/kanban/application/responsible/`;
- `backend/src/main/java/br/com/facilit/kanban/domain/responsible/Responsible.java`;
- `backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java`;
- todos os arquivos de `backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/PageResponse.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleRequest.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleResponse.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleRestController.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRequest.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectResponse.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ResponsibleGraphQlController.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java`;
- `backend/src/main/resources/graphql/kanban.graphqls`;
- `backend/src/main/resources/db/migration/V2__enforce_case_insensitive_responsible_email.sql`;
- todos os arquivos novos de `backend/src/test/java/br/com/facilit/kanban/application/`;
- `backend/src/test/java/br/com/facilit/kanban/domain/responsible/ResponsibleTest.java`;
- todos os documentos de `docs/evidence/F1-L1/`;
- `README.md`.

## Não abertos por estarem fora do escopo de mudança

- fontes do frontend em `frontend/src/**`: preservadas sem alteração; regressão obrigatória está no gate local;
- evidências históricas em `docs/evidence/F0-L1/**`, `F0-L2/**` e `F0-L3/**`: preservadas sem alteração;
- internals de `.git`: não fazem parte do código do lote.

Não houve arquivo de produção alterado por automação sem releitura integral posterior.

## Releitura integral — correção pós-gate RED

- `backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java`;
- `backend/src/main/java/br/com/facilit/kanban/application/health/HealthQuery.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/HealthRestController.java`;
- `backend/src/main/java/br/com/facilit/kanban/delivery/graphql/HealthGraphQlController.java`;
- `backend/src/test/java/br/com/facilit/kanban/delivery/rest/HealthRestControllerTest.java`;
- `backend/src/test/java/br/com/facilit/kanban/delivery/graphql/HealthGraphQlControllerTest.java`.
