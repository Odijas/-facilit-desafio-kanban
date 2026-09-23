# F4 — AUDITORIA REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-23 · Release `1.0.0` · Fonte dos requisitos: documento do desafio (Desafio Técnico Backend — Kanban) e `PROMPT-EXECUTIVO-KANBAN-v1.0.md`.

Legenda da coluna Evidência:

- **gate `<LOTE>`**: execução do gate do lote pelo desenvolvedor, com saída registrada em `docs/evidence/<LOTE>/`;
- **F4**: gate de freeze desta release (`VERIFICACAO-USUARIO.md` e `SAIDA-GATE.txt`);
- **CI**: GitHub Actions (`.github/workflows/ci.yml`).

## Obrigatórios do desafio

| # | Requisito | Implementação | Teste | Evidência |
|---|---|---|---|---|
| 1 | CRUD de Projeto | `ProjectService`, `ProjectRestController`, `ProjectGraphQlController` | `ProjectServiceTest.createsReadsListsUpdatesAndDeletesProjectWithRecalculatedMetrics`, `KanbanApiIT` | gate F1-L1; F4 `F4_API_GREEN` |
| 2 | CRUD de Responsável com e-mail único | `ResponsibleService.ensureEmailAvailable`, migration `V2__enforce_case_insensitive_responsible_email.sql` | `ResponsibleServiceTest`, `ResponsibleTest`, `KanbanApiIT` | gate F1-L1; F4 cenário "e-mail duplicado" (409) |
| 3 | Projeto com 1+ responsáveis, datas previstas e realizadas, dias de atraso e % de tempo restante | domínio `Project`, `ProjectDates`, `ProjectScheduleCalculator`; `project_responsibles` (V1) | `ProjectTest`, `ProjectScheduleCalculatorTest` (12 casos, incluindo os zeros previstos) | gate F0-L3 |
| 4 | Status calculado pelas datas; editar datas recalcula | `ProjectScheduleCalculator` chamado em `ProjectService.create/update` | `ProjectScheduleCalculatorTest.classifies*`, `keepsProjectInProgressOnItsPlannedEndDate` | gate F0-L3 e F1-L1 |
| 5 | Transições Kanban da tabela, com efeito automático, recálculo e bloqueio com mensagem | `ProjectStatusTransition`, `PATCH /api/v1/projects/{id}/status`, mutation `transitionProject` | `ProjectStatusTransitionTest`: 17 testes, cobrindo as 12 transições da tabela (efeito automático e bloqueio, quando a tabela prevê os dois) e a rejeição do mesmo status | gate F1-L2; F4 cenários 12 e 13 |
| 6 | Listagem por status (quadro) | `ProjectService.listByStatus/search`; filtro `status` REST e GraphQL | `KanbanApiIT`, `ProjectServiceTest.searchesProjectsAndBuildsIndicators` | gate F1-L2; F4 cenário 14 |
| 7 | Swagger com exemplos, schemas e mensagens de erro | springdoc: `/api-docs` e `/swagger-ui.html`; `@Schema(example=…)` nos DTOs | `KanbanApiIT.publishesOpenApiWithSchemasAndExamples` | gate F1-L3; F4 `F4_UI_SWAGGER_GREEN` |
| 8 | Testes JUnit de services e controllers, incluindo erro e bloqueio | unitários de domínio e aplicação; `HealthRestControllerTest` (`@WebMvcTest`), `HealthGraphQlControllerTest`, `RestExceptionHandlerTest`; ITs de API | `mvn clean verify` (Surefire + Failsafe) | F4 `F4_BACKEND_GREEN`; CI job `backend` |
| 9 | Erros padronizados e validação de entrada | `RestExceptionHandler` (ProblemDetail + `code`), `GraphQlErrorHandler`, Bean Validation | `KanbanApiIT.validatesRestInputAndReturnsStableProblemCodes`, `RestExceptionHandlerTest` | gate F1-L3; F4 cenários 9, 11 e 13 |
| 10 | Paginação e índices | `PageQuery`/`PageResponse`; `V3__optimize_api_list_indexes.sql` | `KanbanApiIT.appliesMigrationsAndApiIndexesAgainstRealPostgres` | gate F1-L3 |
| 11 | Docker Compose (app + banco) | `compose.yaml` (db, backend, frontend) | subida integral com banco novo | F4 `F4_DOCKER_CLEAN_DB_GREEN` (migrations V1–V5 do zero) |
| 12 | Indicadores: contagem e média de atraso por status (Etapa 3) | `ProjectService.indicators`, `GET /api/v1/indicators/projects`, `projectIndicators` | `ProjectServiceTest.searchesProjectsAndBuildsIndicators`, `KanbanApiIT.exposesDifferentialSecretariatFiltersAndIndicatorsOverRestAndGraphql` | gate F3-L1; F4 cenários 15 e 16 |
| 13 | `AI_USAGE.md` (Etapa 4): ferramentas, estruturação, sugestão rejeitada e trecho de prompt | [`AI_USAGE.md`](../../../AI_USAGE.md) | checagem de seções no gate | gate F3-L4 `F3_L4_DOCS_GREEN`; F4 `F4_STATIC_GREEN` |
| 14 | README: visão geral, decisões, como rodar, testar, Swagger, estrutura, convenções, limitações | [`README.md`](../../../README.md) | checagem de seções e links no gate | F4 `F4_STATIC_GREEN` |
| 15 | Repositório público no GitHub, Git com commits granulares | GitHub `Odijas/facilit-desafio-kanban`; histórico Gitflow por lote | Conventional Commits verificados desde `283ce5d` | gate F3-L4 (etapas 2 a 4); F4 `F4_STATIC_GREEN` |
| 16 | Logs adequados | logs JSON ECS no Compose; `incidentId` em erro 500; sem PII nem segredos | logs ECS e ausência de segredo checados no gate | gate F3-L3; F4 `F4_OBSERVABILITY_GREEN` |

## Diferenciais

| Diferencial | Implementação | Teste | Evidência |
|---|---|---|---|
| GraphQL | `/graphql` sobre os mesmos casos de uso (`*GraphQlController`) | `KanbanApiIT.restAndGraphqlShareApplicationRulesAndGraphqlValidation`, `HealthGraphQlControllerTest` | gate F1-L1; F4 cenário 16 |
| UI Kanban com drag-and-drop | `frontend/src/features/kanban` (React 19 + MUI, drag-and-drop nativo) | 30 testes Vitest + Testing Library | gate F2-L3; CI job `frontend`; F4 `F4_FRONTEND_GREEN` |
| CRUD de Secretaria | `SecretariatService`, REST, GraphQL e UI | `SecretariatServiceTest`, `KanbanApiIT` | gate F3-L1 |
| Filtros avançados (secretaria, período, responsável, texto) | `ProjectFilter` + JPA `Specification` | `ProjectServiceTest.rejectsInvertedAdvancedFilterPeriod`, `KanbanApiIT` | gate F3-L1; F4 cenário 14 |
| Autenticação do responsável | perfil `RESPONSIBLE`, `Actor` na aplicação, credenciais só pelo ADMIN | `ResponsibleCredentialServiceTest`, `ProjectServiceTest.responsible*`, `SecurityApiIT.responsibleAuthenticatesAndManagesOnlyOwnProjects` | gate F3-L2; F4 `F4_RESPONSIBLE_AUTH_GREEN` |
| Observabilidade Prometheus/Grafana e logs estruturados | Actuator (`health`, `prometheus`), `compose.observability.yaml`, painel provisionado, logs ECS | `ObservabilityIT`, `MetricsCredentialsTest` | gate F3-L3; F4 `F4_OBSERVABILITY_GREEN` |
| CI/CD com GitHub Actions | `.github/workflows/ci.yml` (build, testes e análise; token só leitura; actions por SHA) | actionlint + política do workflow | pipeline 35894739171; F4 `F4_CI_GREEN` |
| Camada de IA (Etapa 5) | ADR [`0001-camada-ia-agente-rag.md`](../../adr/0001-camada-ia-agente-rag.md), só proposta | checagem de seções | gate F3-L4 |
| Coleções Postman/Insomnia e diagramas | [`docs/api/facilit-kanban.postman_collection.json`](../../api/facilit-kanban.postman_collection.json); mermaid no README e no ADR | newman no rev1 e no rev2 do F3-L4; mesmos 21 cenários por `curl` | gate F3-L4; F4 `F4_API_GREEN` |
| TDD | ciclo RED → GREEN registrado nas evidências (por exemplo, F3-L2: 3 testes do frontend RED antes da implementação) | — | `docs/evidence/*/EXECUCAO.md` |

## Lacunas declaradas

- **BDD:** não há especificação em Gherkin nem ferramenta de BDD. Os nomes dos testes descrevem o comportamento (por exemplo, `blocksCompletedToInProgressWhenClearingActualEndWouldMakeProjectOverdue`), mas isso não é BDD formal.
- **Dublês de teste:** os testes de serviço usam repositórios em memória (`application/support/InMemory*`) em vez de Mockito. É outro tipo de dublê, que testa comportamento real sem mockar chamadas.
- **Testes de controller:** controllers de CRUD não têm `@WebMvcTest` próprio; a cobertura vem dos ITs de API (`KanbanApiIT`, `SecurityApiIT`) contra PostgreSQL real. Testes com slice (`@WebMvcTest`, `@GraphQlTest`) existem para health; o tratador de erros tem teste unitário.
- **Swagger:** a UI fica em `/swagger-ui.html` e o contrato em `/api-docs`, que atende ao "`/swagger-ui` ou `/api-docs`" do desafio.
- **Histórico Git:** os commits de F2-L1 GREEN a F3-L4 foram reconstruídos por lote em 2026-09-23 a partir dos pacotes validados (`docs/evidence/F3-L4/RECONSTRUCAO-HISTORICO.md`). As datas são as da reconstrução, não retroagidas.
- **Camada de IA:** apenas o ADR, sem implementação.
- **Frontend:** sem tela de gestão de credenciais (o ADMIN usa REST, Swagger ou GraphQL). O bundle passa de 500 kB (aviso não bloqueante do Vite).
