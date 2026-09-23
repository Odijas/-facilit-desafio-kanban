# F3-L1 — EXECUÇÃO

Data: 2026-09-22

## Baseline

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22] F2-L3: auth, responsável, projeto, listagem, transição legítima, bloqueio, edição, CRUD e tipos estritos GREEN.
[EXECUTADO PELO USUÁRIO · 2026-09-22] Docker Compose com db healthy, backend e frontend up.
[EXECUTADO PELO USUÁRIO · 2026-09-22] Resultado: exit code 0.
[CONCLUSÃO] F2-L3/F2 promovidos para GREEN.
```

## Executado neste ambiente

```text
[EXECUTADO · 2026-09-22] leitura/reconciliação do F2-L3 prevalente e do plano F3-L1.
[EXECUTADO · 2026-09-22] implementação de indicadores, CRUD de Secretaria e filtros avançados em application/persistence/REST/GraphQL/frontend.
[EXECUTADO · 2026-09-22] `javac -Xlint:all -Werror` sobre domínio + application: PURE_JAVA_GREEN.
[EXECUTADO · 2026-09-22] TypeScript parser em 31 arquivos: TS_SYNTAX_GREEN.
[EXECUTADO · 2026-09-22] AST scan em 31 arquivos: STRICT_ESCAPE_SCAN_GREEN; sem `any`, assertion `as` ou non-null assertion.
[EXECUTADO · 2026-09-22] `tsc --strict` sobre `api/auth.ts`, `api/health.ts`, `api/kanban.ts`: GREEN no TypeScript disponível no ambiente.
[EXECUTADO · 2026-09-22] consumidores de contratos alterados pesquisados e registrados.
```

## Limitação

```text
[EXECUTADO · 2026-09-22] ambiente possui Java 21, não possui Maven/Docker e não possui pnpm local.
[EXECUTADO · 2026-09-22] `corepack pnpm --version` falhou ao acessar registry.npmjs.org (`EAI_AGAIN`).
[DESCONHECIDO] compilação Spring completa, Biome 2.5.14 real, TypeScript 5.9.3 com node_modules, Vitest, Vite, Testcontainers e smoke Docker dependem do gate local do usuário.
```

## Correção de verificação interna

```text
[EXECUTADO · 2026-09-22] uma checagem auxiliar inicialmente usou `moduleResolution=NodeNext`, divergente do `tsconfig.app.json` real (`moduleResolution=Bundler`). O erro TS2835 resultante era do comando de verificação, não do código do projeto. O comando foi descartado e repetido com a resolução de módulos efetivamente configurada no projeto.
```

## Fechamento estático antes do empacotamento

```text
[EXECUTADO · 2026-09-22] `javac -Xlint:all -Werror` sobre domínio + application: PURE_JAVA_GREEN.
[EXECUTADO · 2026-09-22] `tsc --strict` sobre a camada API com `moduleResolution=Bundler`, coerente com `tsconfig.app.json`: API_TSC_GREEN.
[EXECUTADO · 2026-09-22] parser XML/YAML: XML_YAML_GREEN.
[EXECUTADO · 2026-09-22] scan de contratos obsoletos: STALE_CONTRACT_SCAN_GREEN.
[EXECUTADO · 2026-09-22] parser TypeScript em 31 arquivos: TS_SYNTAX_GREEN.
[EXECUTADO · 2026-09-22] AST scan em 31 arquivos: STRICT_ESCAPE_SCAN_GREEN.
[EXECUTADO · 2026-09-22] `bash -n` do primeiro bloco de `VERIFICACAO-USUARIO.md`: GATE_BASH_SYNTAX_GREEN.
[EXECUTADO · 2026-09-22] `git diff --no-index --check` contra o baseline F2-L3 não emitiu diagnóstico de whitespace: DIFF_WHITESPACE_GREEN.
```

## Rev2 — reconciliação com o snapshot local (2026-09-22)

Base: `facilit-desafio-kanban-F3-L1-local-20260922-143305.tar.gz` (SHA-256 `f3534889592a44ca304973f147dceebd4a085b2cc3ef7c31447cfbb404e7f5f6`), estado F2-L3 já formatado pelo Biome no gate local.

```text
[EXECUTADO · 2026-09-22] diff -rq candidate × local: arquivo órfão `application/responsible/SecretariatRepository.java` presente só no local; 5 arquivos frontend com diferença exclusivamente de formatação (auth.ts, auth.test.ts, LoginPage.tsx, ProtectedHome.tsx, ProjectDialog.tsx).
[EXECUTADO · 2026-09-22] rev2 = local + arquivos do candidate, preservando os 5 arquivos locais e removendo o órfão.
[EXECUTADO · 2026-09-22] Node v22.22.2 (projeto declara >=24 <25); pnpm 12.5.1 via corepack; `pnpm install --frozen-lockfile`: Done.
[EXECUTADO · 2026-09-22] `pnpm lint` antes do format: Found 10 errors (9 format + 1 assist/source/organizeImports), exit 1.
[EXECUTADO · 2026-09-22] `pnpm format`: Checked 37 files. Fixed 10 files. Conferência por tokens: somente formatação/ordem de imports.
[EXECUTADO · 2026-09-22] `pnpm test` (RED): Test Files 2 failed | 4 passed (6); Tests 3 failed | 24 passed (27).
  × requests projects with advanced text filtering
  × creates a responsible from the board
  × creates, updates and deletes a secretariat through explicit actions
[CONCLUSÃO] o candidate original não passaria no gate F3-L1; os testes nunca haviam sido executados.
[EXECUTADO · 2026-09-22] correções aplicadas (ver DECISOES.md) e suíte repetida:
  pnpm format: No fixes applied · exit 0
  pnpm lint: No fixes applied · exit 0
  pnpm typecheck: exit 0
  pnpm test: Test Files 6 passed (6); Tests 27 passed (27) · exit 0
  pnpm build: built · exit 0 (aviso Vite de chunk > 500 kB, não bloqueante)
[EXECUTADO · 2026-09-22] `javac -Xlint:all -Werror` (JDK 21) em domain + application: exit 0.
[EXECUTADO · 2026-09-22] grep: nenhuma referência a `application.responsible.SecretariatRepository`; órfão ausente no rev2.
[EXECUTADO · 2026-09-22] gate rev2 extraído por awk e `bash -n`: GATE_REV2_BASH_SYNTAX_GREEN; único delta contra o gate do candidate é o bloco de reconciliação.
[DESCONHECIDO] `mvn clean verify`, Testcontainers, Docker Compose e smoke: Maven Central bloqueado (403) e JDK 25 indisponível neste ambiente; dependem do gate local.
[DESCONHECIDO] comportamento do frontend em Node 24: executado aqui apenas em Node 22.
```

## Rev3 — correção do backend após gate RED (2026-09-22)

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22 14:57] gate rev2:
  F3_L1_RECONCILIATION_GREEN
  F3_L1_FRONTEND_GREEN (Biome sem correções; 27/27 testes; build OK)
  mvn clean verify: unitários 47/47 GREEN; KanbanApiIT 5 testes, 2 falhas; SecurityApiIT 3/3
  KanbanApiIT.restAndGraphqlShareApplicationRulesAndGraphqlValidation:153 expected 200, was 403
  KanbanApiIT.exposesDifferentialSecretariatFiltersAndIndicatorsOverRestAndGraphql:225 expected 200, was 403
  PSQLException: function lower(bytea) does not exist
  PSQLException: could not determine data type of parameter $7
  BUILD FAILURE · Resultado: exit code 1
[CONCLUSÃO] F3-L1 rev2 RED.
```

Erro próprio registrado (BASE 2.7): no rev2 a IA marcou a camada de persistência como "não aberta", embora `ProjectJpaRepository.java` tivesse sido alterado pelo F3-L1 e constasse do diff. A leitura foi menor que o escopo da mudança do lote (BASE 2.3 e 3). Depois de constatar que os testes frontend do candidate nunca haviam sido executados, a mesma inferência valia para o backend e não foi aplicada.

```text
[VERIFICADO: ProjectJpaRepository.java:28–58 (rev2) · 2026-09-22] consulta de busca com parâmetros anuláveis no padrão `(:p is null or ...)`; o PostgreSQL não infere o tipo do parâmetro e falha (bytea em :text; $7 indeterminado).
[VERIFICADO: ProjectRestController.java:128, ProjectGraphQlController.java:56] toda listagem de projetos (com ou sem filtro) passa por ProjectService.search → a falha atinge também o quadro Kanban sem filtros.
[EXECUTADO · 2026-09-22] grep "is null or" em backend/src/main após a correção: sem ocorrências.
[EXECUTADO · 2026-09-22] grep de consumidores de ProjectJpaRepository: somente ProjectPersistenceAdapter.
[EXECUTADO · 2026-09-22] gate rev3 extraído por awk e `bash -n`: GATE_REV3_BASH_SYNTAX_GREEN.
[DESCONHECIDO] compilação e testes backend do rev3: Maven Central bloqueado neste ambiente (403) e JDK 25 indisponível. Resolve: `mvn -B -ntp clean verify` no gate local.
```

## Rev4 — correção do gate após execução do rev3 (2026-09-22)

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22 15:25] gate rev3 (trecho recebido a partir do Docker Compose):
  Docker Compose: db healthy, backend e frontend started; health {"status":"UP"}
  F3_L1_AUTH_GREEN
  F3_L1_SECRETARIAT_CREATE_GREEN
  F3_L1_RESPONSIBLE_GREEN
  F3_L1_PROJECT_FIXTURES_GREEN
  F3_L1_LIST_UNFILTERED_GREEN
  F3_L1_FILTERS_REST_GREEN
  F3_L1_FILTER_VALIDATION_GREEN
  F3_L1_INDICATORS_REST_GREEN
  GraphQL: Validation error (VariableTypeMismatch@[secretariat]) : Variable 'secretariatId' of type 'ID' used in position expecting type 'ID!'
  Resultado: exit code 1
[VERIFICADO: script com `set -euo pipefail`] a execução só alcança o Docker Compose se frontend e `mvn clean verify` terminarem com exit 0; os marcadores F3_L1_FRONTEND_GREEN e F3_L1_BACKEND_GREEN não constam do trecho recebido.
```

Defeito no artefato de verificação, não no código de produção: a consulta GraphQL do gate declarava `$secretariatId: ID` e o reutilizava em `secretariat(id: ID!)`. O defeito existe desde o candidate original e só se manifestou agora porque as etapas anteriores falhavam antes.

```text
[EXECUTADO · 2026-09-22] graphql-js 16.14.2, validação dos documentos GraphQL do gate contra health.graphqls + kanban.graphqls do projeto:
  RED (rev3): INVALID gate:filtros+indicadores+secretaria: Variable "$secretariatId" of type "ID" used in position expecting type "ID!".
  GREEN (rev4): VALID gate:filtros+indicadores+secretaria; VALID gate:updateSecretariat; documents=2 failures=0
[EXECUTADO · 2026-09-22] gate rev4 extraído por awk e `bash -n`: sintaxe válida.
[DESCONHECIDO] etapas do gate após GraphQL (update/bloqueio/CRUD de secretaria, OpenAPI, scan estrito, git diff --check) ainda não executadas. Resolve: gate local.
```

## Gate final GREEN (2026-09-23)

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
[CONCLUSÃO] F3-L1 promovido para GREEN.
