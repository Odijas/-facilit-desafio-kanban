# F3-L2 — LIVRO-RAZÃO DE LEITURA

Data: 2026-09-23

## Integral

- `docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md`, `PROMPT-EXECUTIVO-KANBAN-v1.0.md`, `REPLANEJAMENTO-F3.md`
- `ApplicationBeans.java`, `application/common/*`, `ProjectService.java`, `SaveProjectCommand.java`, `ProjectRepository.java`
- `application/responsible/*`, `application/secretariat/*`, `domain/responsible/Responsible.java`, `domain/project/Project.java`
- `ProjectRestController.java`, `ResponsibleRestController.java`, `SecretariatRestController.java`, `ResponsibleRequest.java`, `ProjectRequest.java`, `RestExceptionHandler.java`
- `ProjectGraphQlController.java`, `ResponsibleGraphQlController.java`, `SecretariatGraphQlController.java`, `GraphQlErrorHandler.java`, `AuthRestController.java`
- `SecurityConfiguration.java`, `SecurityAdminBootstrap.java`, `DatabaseUserDetailsService.java`, `SecurityUserJpaEntity.java`, `SecurityUserJpaRepository.java`
- `ResponsiblePersistenceAdapter.java`, `ResponsibleJpaRepository.java`
- `V1__create_kanban_core.sql`, `V2__enforce_case_insensitive_responsible_email.sql`, `V4__create_security_users.sql`, `application.yml`, `kanban.graphqls`, `health.graphqls`, `compose.yaml`, `.env.example`
- `SecurityApiIT.java`, `ProjectServiceTest.java`, `ResponsibleServiceTest.java`, `SecretariatServiceTest.java`, `InMemoryResponsibleRepository.java`
- frontend: `api/auth.ts`, `features/kanban/KanbanBoard.tsx`, `KanbanColumn.tsx`, `ProjectDialog.tsx`, `ProjectFiltersBar.tsx`, `features/secretariats/SecretariatPanel.tsx`, `SecretariatPanel.test.tsx`, `KanbanBoard.test.tsx`, `features/dashboard/DashboardPage.tsx`, `vite.config.ts`

## Amostrado

- `KanbanApiIT.java` [AMOSTRAGEM: linhas 1–78 e 170–448]
- `App.test.tsx` [AMOSTRAGEM: linhas 1–200, arquivo inteiro exceto rodapé]
- `api/auth.test.ts` [AMOSTRAGEM: linhas 30–50]
- `HealthRestControllerTest.java`, `HealthGraphQlControllerTest.java` [AMOSTRAGEM: anotações de slice]
- `features/auth/ProtectedHome.tsx` [AMOSTRAGEM: uso de `DashboardPage`]

## Fontes externas

Ver `FONTES-RAG.md`.

## Reler após edição por script

Todos os arquivos Java e TypeScript editados por script foram revisados por diff integral contra o rev4 do F3-L1 antes do empacotamento.
