# Facilit — Desafio Técnico Kanban

API Java para gestão de projetos em quadro Kanban, feita para o Desafio Técnico Backend (Back-End Sênior) da Facilit. O REST é o contrato obrigatório; GraphQL, interface web, autenticação do responsável, observabilidade e CI entram como diferenciais sobre os mesmos casos de uso.

## Sumário

- [Visão geral](#visão-geral)
- [Arquitetura e decisões técnicas](#arquitetura-e-decisões-técnicas)
- [Regras de negócio](#regras-de-negócio)
- [Como rodar (Docker)](#como-rodar-docker)
- [Como testar](#como-testar)
- [API: Swagger, GraphQL e erros](#api-swagger-graphql-e-erros)
- [Segurança](#segurança)
- [Observabilidade](#observabilidade)
- [CI (GitHub Actions)](#ci-github-actions)
- [Estrutura de pastas e convenções](#estrutura-de-pastas-e-convenções)
- [Uso de IA](#uso-de-ia)
- [Limitações e próximos passos](#limitações-e-próximos-passos)
- [Governança e histórico de entrega](#governança-e-histórico-de-entrega)
- [Changelog](CHANGELOG.md)

## Visão geral

| Item do desafio | Onde está |
|---|---|
| CRUD de Projeto e Responsável (e-mail único) com Swagger | REST `/api/v1/projects`, `/api/v1/responsibles`; `/swagger-ui.html` e `/api-docs` |
| Status calculado, dias de atraso e % de tempo restante | domínio puro: `ProjectScheduleCalculator` |
| Quadro Kanban e transições da tabela, com bloqueio e mensagem clara | `ProjectStatusTransition` + `PATCH /api/v1/projects/{id}/status` |
| Indicadores (status, secretaria, responsável e prazos) | REST `/api/v1/indicators/projects*` e GraphQL `projectIndicators*`/`projectDeadlines` |
| Testes JUnit de services, controllers e integração | `backend/src/test`: services com repositórios em memória e com Mockito; controllers REST e GraphQL isolados com o service simulado; repositório, transações e a tabela de transição pela API com PostgreSQL real (Testcontainers) |
| Docker Compose (app + banco) | `compose.yaml` |
| `AI_USAGE.md` | [`AI_USAGE.md`](AI_USAGE.md) |
| Diferencial — GraphQL | `/graphql` (mesmos casos de uso do REST) |
| Diferencial — UI Kanban com drag-and-drop | `frontend/` (React 19 + MUI) |
| Diferencial — CRUD de Secretaria e filtros avançados | `/api/v1/secretariats`; filtros por status, secretaria, responsável, período e texto |
| Diferencial — autenticação do responsável | perfil `RESPONSIBLE` (altera só os próprios projetos) |
| Diferencial — observabilidade | Actuator, Prometheus, Grafana e logs JSON (ECS) |
| Diferencial — CI/CD | `.github/workflows/ci.yml` |
| Diferencial — camada de IA (Etapa 5) | ADR [`docs/adr/0001-camada-ia-agente-rag.md`](docs/adr/0001-camada-ia-agente-rag.md) |

## Arquitetura e decisões técnicas

```mermaid
flowchart LR
  subgraph Cliente
    UI["Frontend React 19 + MUI<br/>(Vite, porta 5173)"]
    EXT["Swagger / Postman / GraphiQL"]
  end
  subgraph Backend["Backend Spring Boot 3.5 (Java 25, porta 8080)"]
    direction TB
    SEC["Spring Security<br/>sessão + CSRF · Basic só p/ métricas"]
    DEL["delivery<br/>REST · GraphQL · auth"]
    APP["application<br/>casos de uso + portas<br/>(Actor, regras de posse)"]
    DOM["domain<br/>Projeto, Responsável, Secretaria<br/>status, transições, métricas"]
    INF["infrastructure<br/>JPA, segurança, config"]
    SEC --> DEL --> APP --> DOM
    INF -. implementa portas .-> APP
  end
  DB[("PostgreSQL 18.6<br/>Flyway V1–V7")]
  PROM["Prometheus"] --> GRAF["Grafana"]
  UI -- "/api (proxy Vite)" --> SEC
  EXT --> SEC
  INF --> DB
  PROM -- "/actuator/prometheus (Basic)" --> SEC
```

- **Clean Architecture pragmática.** O domínio não conhece Spring, banco nem HTTP; a aplicação expõe casos de uso e portas, montados em `ApplicationBeans` (composition root). REST e GraphQL chamam os mesmos serviços, sem regra duplicada; controllers e resolvers ficam finos.
- **Regra de negócio determinística no domínio.** Status, dias de atraso e % de tempo restante são calculados por `ProjectScheduleCalculator` a partir da data de hoje obtida de um `Clock` no fuso de negócio (`APP_TIME_ZONE`, padrão `America/Sao_Paulo`) injetado no `ProjectService`; as transições partem do status de hoje, aplicam só os efeitos da tabela do desafio e recalculam o status, bloqueando quando o resultado difere do pedido.
- **Status sempre atual.** Os valores calculados ficam gravados, com a data do cálculo, para listar e filtrar por índice. Quando o dia muda, os projetos não concluídos são recalculados antes de qualquer leitura e também por agendamento à meia-noite ([ADR 0002](docs/adr/0002-status-sempre-atual.md)).
- **Persistência.** PostgreSQL com Flyway (schema versionado e `ddl-auto: validate`), índices alinhados às consultas paginadas e filtros via JPA `Specification`.
- **Transação por caso de uso.** Cada caso de uso de escrita roda inteiro numa transação, pela porta `TransactionRunner` (a aplicação continua sem Spring; a infraestrutura usa `TransactionTemplate`). Se algo falha no meio, nada do que o caso de uso gravou fica. Projetos têm `@Version`: duas edições simultâneas não se sobrescrevem, e a segunda a gravar recebe 409 `CONFLICT`.
- **Contrato de erro estável.** `ProblemDetail` (RFC 9457) com `code` fixo no REST e `extensions.code` no GraphQL.
- **Segurança por padrão.** Sessão HTTP com CSRF para SPA, senhas bcrypt, sem credencial padrão versionada, autorização de posse na camada de aplicação.
- **Versões congeladas.** Java 25, Spring Boot 3.5.16, PostgreSQL 18.6, React 19.3, MUI 9.4, TanStack Query 5.102, TypeScript 5.9, Vite 8.3, Vitest 4.1, Biome 2.5, pnpm 12.5.1, Node 24.

As decisões de cada lote, com fonte e marcação de verificação, estão em `docs/evidence/<LOTE>/DECISOES.md`.

## Regras de negócio

- **Status**: A iniciar (sem início e término realizados), Em andamento (início realizado, término previsto maior ou igual a hoje, sem término realizado; ver [interpretações](#interpretações-do-enunciado)), Atrasado (início previsto vencido sem início realizado, ou término previsto vencido sem término realizado) e Concluído (término realizado preenchido). Editar datas recalcula o status.
- **Status de hoje**: status, dias de atraso e % de tempo restante valem para a data de hoje, mesmo sem edição do projeto. Quando o dia muda, os projetos não concluídos são recalculados antes de qualquer leitura (projeto, quadro, filtros, indicadores e transição); `updatedAt` continua registrando só edições feitas pelo usuário.
- **Transições**: seguem a tabela do desafio linha a linha (efeito automático, recálculo e bloqueio com mensagem quando o status final diverge do solicitado). Transição bloqueada responde 422 `TRANSITION_BLOCKED`, com a orientação do desafio em pt-BR e os campos `currentStatus`/`requestedStatus`. Transições que apagam uma data já registrada (Em andamento → A iniciar; Concluído → Em andamento ou Atrasado) exigem `confirm: true`; sem ele, a resposta é 422 `CONFIRMATION_REQUIRED`, com `clearedField` indicando a data que seria apagada.
- **Métricas**: dias de atraso e % de tempo restante conforme as fórmulas do desafio, com os casos de zero previstos (sem datas, concluído, prazo vencido).
- **Responsável**: e-mail único (sem diferenciar maiúsculas); não pode ser removido enquanto estiver em projeto (409). **Secretaria**: não pode ser removida enquanto tiver responsável (409).

### Interpretações do enunciado

Pontos que o documento do desafio não define, com a escolha feita e onde ela é testada:

| Situação | Escolha | Teste |
|---|---|---|
| "Hoje" | data no fuso de negócio `America/Sao_Paulo` (`APP_TIME_ZONE`), não a data UTC | `ProjectScheduleRefresherTest.usesTheBusinessTimeZoneToDecideWhatTodayIs` |
| Início realizado preenchido e término previsto **igual** a hoje | Em andamento: é o último dia do prazo, que ainda não venceu. O enunciado exige término previsto > hoje para Em andamento e < hoje para Atrasado; o dia exato fica sem regra | `ProjectScheduleCalculatorTest.keepsProjectInProgressOnItsPlannedEndDate` |
| Mais de uma definição vale ao mesmo tempo | prioridade Concluído > Atrasado > Em andamento > A iniciar (por exemplo, início previsto vencido sem início realizado é Atrasado) | `ProjectScheduleCalculatorTest.classifiesProjectWithActualEndAsCompleted`, `classifiesMissedPlannedStartAsOverdue` |
| Início realizado preenchido sem término previsto | recusado: sem término previsto não há como classificar Em andamento nem Atrasado | `ProjectScheduleCalculatorTest.rejectsStartedProjectWithoutPlannedEndWhenItCannotBeClassified` |
| Início ou término realizado depois de hoje | recusado: data realizada registra fato já ocorrido | `ProjectDatesTest`, `ProjectServiceTest.rejectsActualDatesAfterTodayOnCreateAndUpdate` |
| Status gravado em outro dia | a transição parte do status de hoje; o valor gravado é recalculado antes da leitura | `ProjectStatusTransitionTest.line05DoesNotLetStaleInProgressBypassTheInProgressToOverdueBlock`, `ProjectScheduleRefresherTest`, `ScheduleFreshnessIT` |
| Linha "A iniciar → Atrasado" | com o status de hoje, nunca resulta em sucesso: se o início previsto já passou, o projeto já está Atrasado; se não passou, a tabela manda bloquear | `ProjectStatusTransitionTest.line02BlocksNotStartedToOverdueBeforePlannedStart`, `line02BlocksNotStartedToOverdueOnPlannedStartBecauseDatesStillClassifyAsNotStarted`, `line02TreatsStaleNotStartedAsOverdueOnceThePlannedStartHasPassed` |
| "Confirmações obrigatórias" (Etapa 2) | a transição cuja ação automática apaga uma data registrada só é aplicada com `confirm: true` no corpo do `PATCH /api/v1/projects/{id}/status` ou no argumento da mutation `transitionProject`: Em andamento → A iniciar apaga o início realizado; Concluído → Em andamento ou Atrasado apaga o término realizado. A confirmação só é pedida quando a transição passaria; se a tabela bloqueia, o bloqueio vem primeiro. Na UI, o quadro abre um diálogo com a mensagem do servidor | `ProjectStatusTransitionTest.line04*`, `line11*`, `line12*`; `ProjectServiceTest.clearsRecordedActualStartOnlyWithExplicitConfirmation`; `KanbanBoard.test.tsx` |
| Erro de regra de negócio × erro de entrada | entrada malformada ou fora do formato: 400 (`VALIDATION_ERROR`, `INVALID_REQUEST`); dado bem formado que viola regra do domínio: 422 (`BUSINESS_RULE_VIOLATION`, `TRANSITION_BLOCKED`, `CONFIRMATION_REQUIRED`) | `RestExceptionHandlerTest` |
| Edição das datas (`PUT`) × tabela de transição | a tabela e as confirmações valem para a mudança de status pedida (`PATCH /api/v1/projects/{id}/status` e `transitionProject`). O `PUT` edita as datas, e o status é recalculado a partir delas, sem confirmação: quem apaga uma data realizada pelo `PUT` está corrigindo o dado, não pedindo uma transição | `ProjectServiceTest.updateCanClearActualStartWithoutTransitionConfirmationAndRecalculatesStatus` |
| Transição que depende do término previsto, com ele vazio (linhas 1, 11 e 12) | 422 `BUSINESS_RULE_VIOLATION` pedindo o `plannedEnd`, e não `TRANSITION_BLOCKED`: depois da ação automática, falta um dado obrigatório para classificar o projeto; não é a tabela que recusa | `ProjectStatusTransitionTest.line01RequiresPlannedEndToClassifyTheStartedProject` |
| % de tempo restante | arredondado para o inteiro mais próximo; 100% antes do início previsto | `ProjectScheduleCalculatorTest.capsRemainingPercentageAtOneHundredBeforePlannedStart` |

## Como rodar (Docker)

Pré-requisitos: Docker com Compose v2. Para rodar fora do Docker: JDK 25 + Maven 3.9 e Node 24 + pnpm 12.5.1 (via corepack).

1. Prepare o `.env` (ignorado pelo Git). Não há credencial padrão: defina `POSTGRES_PASSWORD`, `APP_ADMIN_EMAIL` e `APP_ADMIN_PASSWORD`. O administrador é criado na primeira subida e a senha fica só como hash bcrypt.

   ```bash
   cp .env.example .env
   # edite .env: POSTGRES_PASSWORD, APP_ADMIN_EMAIL, APP_ADMIN_PASSWORD
   # opcional (responsável de demonstração, os quatro juntos, senha ≥ 12 caracteres):
   # APP_DEMO_RESPONSIBLE_NAME, _EMAIL, _POSITION, _PASSWORD
   ```

2. Suba banco, backend e frontend:

   ```bash
   docker compose up --build -d
   ```

Serviços:

- frontend: `http://localhost:5173`
- Swagger UI: `http://localhost:8080/swagger-ui.html` · OpenAPI: `http://localhost:8080/api-docs`
- GraphQL: `http://localhost:8080/graphql` (autenticado) · GraphiQL: `http://localhost:8080/graphiql`
- REST health: `http://localhost:8080/api/v1/health`
- autenticação: `GET /api/v1/auth/csrf`, `POST /api/v1/auth/login`, `GET /api/v1/auth/me`, `POST /api/v1/auth/logout`
- health do Actuator: `http://localhost:8080/actuator/health` (público, sem detalhes; também `/liveness` e `/readiness`)
- métricas Prometheus: `http://localhost:8080/actuator/prometheus` (HTTP Basic com a credencial técnica de métricas)
- PostgreSQL: só na rede interna do Compose (`db:5432`)

Usar a API pelo Swagger UI (a API exige sessão e token CSRF em toda escrita, inclusive no login):

1. `GET /api/v1/auth/csrf` → **Try it out** → **Execute**. O navegador guarda o cookie `XSRF-TOKEN`, e o Swagger UI passa a enviá-lo no cabeçalho `X-XSRF-TOKEN` (`springdoc.swagger-ui.csrf.enabled`).
2. `POST /api/v1/auth/login` com `{"email": "...", "password": "..."}` do administrador definido no `.env`.
3. `GET /api/v1/auth/csrf` de novo: o login troca o token, e esta chamada entrega o novo.
4. Qualquer operação. A sessão segue no cookie `JSESSIONID`.

No GraphiQL, faça o passo 1 a 3 pelo Swagger UI (mesma origem) e informe no painel **Headers** `{"X-XSRF-TOKEN": "<valor do cookie XSRF-TOKEN>"}`.

### Observabilidade (opcional)

Preencha `METRICS_PASSWORD` e `GRAFANA_ADMIN_PASSWORD` no `.env` (mínimo de 16 caracteres para `METRICS_PASSWORD`; gere com `python3 -c 'import secrets; print(secrets.token_urlsafe(32))'`) e suba com a sobreposição:

```bash
docker compose -f compose.yaml -f compose.observability.yaml up --build -d
```

- Prometheus: `http://127.0.0.1:9090` (coleta `backend:8080/actuator/prometheus` a cada 15 s);
- Grafana: `http://127.0.0.1:3000`, usuário `admin` e a senha de `GRAFANA_ADMIN_PASSWORD`; painel provisionado **Facilit Kanban — Backend**. A senha do Grafana vale na criação do contêiner; para trocá-la, recrie o serviço (`docker compose -f compose.yaml -f compose.observability.yaml up -d --force-recreate grafana`).

## Como testar

Backend (unitários com Surefire; integração com Failsafe + Testcontainers, exige Docker):

```bash
cd backend
mvn -B -ntp clean verify
```

Camadas de teste do backend:

| Camada | Como | Exemplos |
|---|---|---|
| Domínio | JUnit puro | `ProjectScheduleCalculatorTest`, `ProjectStatusTransitionTest` (as 12 linhas da tabela, com mensagens exatas) |
| Services | repositórios em memória; interações com Mockito | `ProjectServiceTest`, `ProjectServiceMockitoTest` (bloqueio não grava; o recálculo roda antes da leitura) |
| Controllers REST | `@WebMvcTest` com o service simulado (`@MockitoBean`) | `ProjectRestControllerTest`, `ResponsibleRestControllerTest`, `SecretariatRestControllerTest`, `ProjectIndicatorsRestControllerTest` |
| Controllers GraphQL | `@GraphQlTest` com o service simulado | `ProjectGraphQlControllerTest`, `ResponsibleGraphQlControllerTest`, `SecretariatGraphQlControllerTest` |
| Repositório | `@DataJpaTest` com PostgreSQL real e as migrations | `ProjectPersistenceAdapterIT` |
| Transações | Spring Boot com PostgreSQL real | `TransactionIT` (falha no meio desfaz tudo; edição concorrente não sobrescreve) |
| API | servidor real, sessão e CSRF | `StatusTransitionApiIT` (12 linhas pela REST e 3 pela GraphQL), `ProjectIndicatorsIT`, `KanbanApiIT`, `SecurityApiIT`, `OpenApiContractIT` |
| BDD | Cucumber + JUnit Platform, Gherkin em pt-BR | `features/transicoes.feature` cobre as 12 linhas da tabela |

Frontend:

```bash
cd frontend
corepack enable
corepack prepare pnpm@12.5.1 --activate
pnpm install --frozen-lockfile
pnpm lint          # Biome
pnpm typecheck     # TypeScript strict
pnpm check:strict  # proíbe any, asserção `as` e non-null `!` em src/
pnpm test          # Vitest + Testing Library
pnpm build
```

Coleção de API ([`docs/api/facilit-kanban.postman_collection.json`](docs/api/facilit-kanban.postman_collection.json), importável no Postman e no Insomnia): percorre autenticação com CSRF, cadastros, transição legítima, bloqueada (422 `TRANSITION_BLOCKED`) e com confirmação obrigatória (422 `CONFIRMATION_REQUIRED` e depois `confirm: true`), filtros, indicadores, GraphQL, erros 401/400/409 e limpeza. Com o backend no ar, informe `adminEmail` e `adminPassword` num ambiente e execute as pastas na ordem. Os cenários de erro e de confirmação também rodam por `curl` no gate do F5-L2 (`docs/evidence/F5-L2/VERIFICACAO-USUARIO.md`).

## API: Swagger, GraphQL e erros

- OpenAPI em `/api-docs`, com schemas e exemplos de parâmetros, corpos e respostas em todas as operações (verificado por `OpenApiContractIT`); Swagger UI em `/swagger-ui.html`.
- GraphQL (`backend/src/main/resources/graphql/*.graphqls`) sobre os mesmos casos de uso do REST: consultas de projetos com os mesmos filtros, indicadores, CRUD e transição. As páginas GraphQL trazem `page`, `size`, `totalPages`, `hasNext` e `hasPrevious`; o total de itens (`totalElements`) só existe no REST.
- Indicadores adicionais: `GET /api/v1/indicators/projects/by-secretariat`, `/by-responsible` e `/deadlines?withinDays=7` (`withinDays` entre 1 e 90). O GraphQL expõe `projectIndicatorsBySecretariat`, `projectIndicatorsByResponsible` e `projectDeadlines`.
- Erros REST em `ProblemDetail` (`application/problem+json`) com `code` estável e mensagem em pt-BR:

  | HTTP | `code` | Quando |
  |---|---|---|
  | 400 | `VALIDATION_ERROR` | Bean Validation (campos em `violations`) |
  | 400 | `INVALID_REQUEST` | parâmetro ou corpo malformado, paginação ou período inválido |
  | 401 | `UNAUTHORIZED` | sem sessão ou login inválido |
  | 403 | `FORBIDDEN` | sem permissão ou sem token CSRF |
  | 404 | `RESOURCE_NOT_FOUND` | recurso inexistente |
  | 409 | `CONFLICT` | e-mail já cadastrado, registro em uso, corrida com restrição do banco ou atualização concorrente |
  | 422 | `BUSINESS_RULE_VIOLATION` | regra de domínio (ordem das datas, data realizada futura, término previsto obrigatório) |
  | 422 | `TRANSITION_BLOCKED` | transição recusada pela tabela, com orientação e `currentStatus`/`requestedStatus` |
  | 422 | `CONFIRMATION_REQUIRED` | transição que apaga data registrada sem `confirm: true`, com `clearedField` |
  | 500 | `INTERNAL_ERROR` | erro inesperado, com `incidentId` e sem detalhe interno |

  No GraphQL, o mesmo código vai em `extensions.code`. O Swagger documenta os erros de cada operação com exemplos (`ApiErrorDocumentation`, verificado por `OpenApiContractIT`).
- Logs de negócio (`evento=projeto.criado|atualizado|transicao|transicao.recusada|excluido`, `responsavel.*`, `secretaria.*`, `credencial.*`, `projeto.status.recalculado`) em formato `chave=valor`, só com ids, status e perfil do ator, sem nome nem e-mail.
- Paginação por `page`/`size` nas listagens de projetos, responsáveis e secretarias. Os indicadores agrupados e os prazos são agregados e não paginam.
- Limites de entrada, iguais em REST e GraphQL (`InputLimits`, `ProjectFilter`): nome de projeto, responsável e secretaria e cargo até 200 caracteres; e-mail até 254; de 1 a 50 responsáveis por projeto → 400 `VALIDATION_ERROR` (no Swagger, `maxLength`/`maxItems`). Texto de busca até 100 caracteres → 400 `INVALID_REQUEST`, como os demais filtros. O banco guarda `TEXT`; o limite fica na borda da API.
- Busca por texto (`text`, REST e GraphQL): trecho do nome, sem diferenciar maiúsculas. `%`, `_` e `\` são procurados como caracteres comuns, não como curingas.

## Segurança

- Login por e-mail e senha, sessão em cookie `HttpOnly`/`SameSite=Lax` e CSRF para SPA (`XSRF-TOKEN` + cabeçalho `X-XSRF-TOKEN`). Senhas com `DelegatingPasswordEncoder`/bcrypt.
- Perfis: `ADMIN` (tudo) e `RESPONSIBLE` (vê o quadro inteiro; cria, edita, transiciona e exclui só os projetos em que é responsável). Responsáveis, secretarias e credenciais só com o ADMIN. O ADMIN define ou revoga a senha de um responsável em `PUT`/`DELETE /api/v1/responsibles/{id}/credentials` (mínimo de 12 caracteres, máximo de 72 bytes).
- Negar por padrão: caminhos fora das regras explícitas são negados; erros inesperados sem stack trace; endpoints do Actuator além de `health` e `prometheus` indisponíveis.
- Nenhuma credencial versionada: administrador, responsável de demonstração e senhas de observabilidade vêm do `.env`.

## Observabilidade

- Actuator expõe só `health` (público, sem detalhes) e `prometheus` (HTTP Basic com a credencial técnica `ROLE_METRICS`, cadeia stateless própria; sessão, ADMIN e responsável não leem métricas; sem senha configurada o endpoint fica fechado).
- Métricas com a tag `application="facilit-kanban"`: histograma de `http.server.requests` (p95), JVM e pool HikariCP.
- Logs JSON no formato ECS (`@timestamp`, `log.level`, `message`, `ecs.version`) no Docker Compose; execução local via Maven mantém o log legível.
- `compose.observability.yaml` adiciona Prometheus v3.14.0 e Grafana 13.1.3 ligados só em `127.0.0.1`, com datasource e painel provisionados em `observability/`.

## CI (GitHub Actions)

`.github/workflows/ci.yml` roda em `push`, `pull_request` e manualmente, com três jobs:

- `frontend`: Node 24, pnpm 12.5.1, `pnpm install --frozen-lockfile`, lint, typecheck, `check:strict`, testes e build;
- `backend`: JDK 25 (Temurin) com cache Maven, `mvn -B -ntp clean verify` (compilação com `-Xlint:all -Werror`, unitários, BDD, integração com Testcontainers e JaCoCo com mínimo global de 95% de linhas);
- `repository`: `git diff --check` sobre a árvore inteira, ausência de `localStorage`/`sessionStorage` no frontend e de `.env`/chaves versionados.

Práticas de segurança do workflow: `permissions: contents: read`, actions fixadas por SHA completo, `persist-credentials: false`, sem `pull_request_target`, sem runner próprio e sem segredos.

## Estrutura de pastas e convenções

```text
backend/                     API Spring Boot (Maven)
  src/main/java/br/com/facilit/kanban/
    domain/                  entidades e regras puras (projeto, responsável, secretaria)
    application/             casos de uso, portas e erros de aplicação (Actor, Conflict, NotFound, Forbidden)
    infrastructure/          JPA, segurança (sessão, CSRF, Actuator), configuração
    delivery/                REST, GraphQL e autenticação
  src/main/resources/        application.yml, db/migration (Flyway V1–V7), graphql/*.graphqls
  src/test/java/             unitários (domínio, serviços, controllers) e integração (*IT, Testcontainers)
frontend/                    React 19 + MUI + TanStack Query (Vite)
  src/api/                   cliente HTTP e contratos, independente do React Query
  src/features/              auth, dashboard, kanban, indicators, secretariats
  src/app, components, pages navegação, estados visuais compartilhados e página 404
  scripts/                   verificação de tipagem estrita
observability/               configuração do Prometheus e provisionamento do Grafana
docs/api/                    coleção Postman/Insomnia
docs/adr/                    decisões de arquitetura (0001 camada de IA; 0002 status sempre atual)
docs/governance/             prompts executivos e replanejamento que governam a entrega
docs/evidence/<LOTE>/        pacote de evidências e gate de cada lote
.github/workflows/           CI
```

Convenções: código e identificadores em inglês, documentação em português; testes de integração com sufixo `IT`; migrations `V<n>__descricao.sql` sem edição após aplicadas; frontend sem `any`, `as` ou `!` e sem credencial no navegador; cada lote fecha com gate executado (`docs/evidence/<LOTE>/VERIFICACAO-USUARIO.md`).

## Uso de IA

O uso de IA no desenvolvimento está descrito em [`AI_USAGE.md`](AI_USAGE.md). A proposta de camada de IA do produto (Etapa 5) está no ADR [`docs/adr/0001-camada-ia-agente-rag.md`](docs/adr/0001-camada-ia-agente-rag.md), sem implementação.

## Limitações e próximos passos

- A camada de IA (agente/RAG) está só especificada em ADR.
- Sem tela de gestão de credenciais no frontend; o ADMIN usa REST/Swagger ou GraphQL.
- Busca textual por substring sem índice dedicado (`pg_trgm` seria o próximo passo para volume maior).
- Sessão em memória do processo: escalar horizontalmente exigiria sessão compartilhada (por exemplo, Spring Session com Redis ou JDBC).
- Observabilidade sem tracing distribuído e sem regras de alerta.
- Bundle do frontend acima de 500 kB (aviso não bloqueante do Vite); divisão de código é o próximo passo.
- Execução dos testes de integração depende de Docker disponível (Testcontainers).
- O recálculo diário grava status e métricas com a data do cálculo; com várias instâncias, cada uma pode repetir a verificação no mesmo dia, sem efeito (é idempotente).
- Concorrência: o `@Version` protege contra duas gravações simultâneas. A API não recebe a versão do cliente, então não detecta que alguém editou o projeto entre a leitura na tela e o envio; nesse caso, a última gravação vale.
- GraphiQL sem envio automático do token CSRF: informe o cabeçalho `X-XSRF-TOKEN` no painel Headers (roteiro em "Como rodar").
- Limites de tamanho só na borda da API: o banco guarda `TEXT` sem restrição de tamanho; um `CHECK` no banco seria o reforço seguinte.

## Governança e histórico de entrega

Governança em `docs/governance`: `PROMPT-EXECUTIVO-BASE-v1.1.md`, `PROMPT-EXECUTIVO-KANBAN-v1.0.md`, `REPLANEJAMENTO-F3.md`, `PLANO-CONFORMIDADE-F5.md`, `PLANO-CORRECAO-RELEASE-2.0.0.md`, `PLANO-CORRECAO-RELEASE-2.0.1.md` e `PLANO-CORRECAO-RELEASE-2.0.2.md`. Cada lote gera o Pacote Anti-Alucinação (livro-razão, fontes, decisões, consumidores, matriz requisito → implementação → teste → evidência, riscos e gate) em `docs/evidence/<LOTE>/`. As auditorias de aderência ao desafio (análise própria confrontada com um agente independente) estão em `docs/evidence/F5/ADERENCIA-FINAL.md`, sobre a release candidata e origem dos lotes F5-C; em `docs/evidence/F5/ADERENCIA-V2.0.0.md`, sobre a tag `v2.0.0` e origem dos lotes F5-P; e em `docs/evidence/F5/ADERENCIA-V2.0.1.md`, sobre a tag `v2.0.1` e origem dos lotes F5-Q.

Histórico Git: os lotes de F2-L1 a F3-L4 foram commitados depois dos gates, reconstruídos por lote a partir dos pacotes verificados (`docs/evidence/F3-L4/RECONSTRUCAO-HISTORICO.md`), com a data do dia da reconstrução. Do F4 em diante, cada lote tem commits granulares no momento da entrega, em Gitflow (`feature/*` ou `bugfix/*` → `merge --no-ff`).

Estado dos lotes:

- F0-L1 — Bootstrap: GREEN em 2026-09-21.
- F0-L2 — Domínio: GREEN em 2026-09-21.
- F0-L3 — Motor de regras: GREEN em 2026-09-21.
- F0 — Fundação + domínio: GREEN em 2026-09-21.
- F1-L1 — CRUD REST + GraphQL: GREEN em 2026-09-22.
- F1-L2 — Kanban e transições: GREEN em 2026-09-22.
- F1-L3 — API e qualidade: GREEN em 2026-09-22.
- F1 — Backend funcional: GREEN em 2026-09-22.
- F2-L1 — Segurança: GREEN em 2026-09-22.
- F2-L2 — Fundação frontend autenticada: GREEN em 2026-09-22.
- F2-L3 — Kanban UI: GREEN em 2026-09-22.
- F2 — Segurança + UI: GREEN em 2026-09-22.
- F3-L1 — Funcionalidades diferenciais: GREEN em 2026-09-23.
- F3-L2 — Autenticação do responsável + erro seguro: GREEN em 2026-09-23.
- F3-L3 — Observabilidade: GREEN em 2026-09-23.
- F3-L4 — Engenharia de entrega: GREEN em 2026-09-23 (gate local, histórico por lote, migração para o GitHub e primeiro pipeline verde).
- F3 — Diferenciais: GREEN em 2026-09-23.
- F4 — Freeze e release `v1.0.0`: GREEN em 2026-09-23. Evidências em `docs/evidence/F4/` (auditoria requisito → implementação → teste → evidência, revisão de segurança, saída do gate de freeze).
- F5-L1 — Regras sempre corretas (status de hoje, fuso, datas realizadas): GREEN em 2026-09-24.
- F5-L2 — Contrato de erro, confirmações, Swagger e logs: GREEN em 2026-09-24.
- F5-L3 — Camadas de teste completas e transação por caso de uso: GREEN em 2026-09-24.
- F5-L4 — Etapa 3, BDD e cobertura: GREEN em 2026-09-24.
- F5-L5 — Release `v2.0.0`: GREEN em 2026-09-24 (freeze e verificação pública da tag). Roteiro, auditoria e gates em `docs/evidence/F5/`.
- F5-C1 — Entrega executável (build da imagem do backend e CSRF no Swagger UI), correção na `release/2.0.0` conforme `docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md`: GREEN em 2026-09-24.
- F5-C2 — Rigor de testes e validação (métricas linha a linha no BDD e limites de tamanho nas entradas): GREEN em 2026-09-24.
- F5-C3 — Documentação de entrega (coleção com os indicadores da Etapa 3, AI_USAGE com a F5, CHANGELOG e auditoria final): GREEN em 2026-09-24.
- F5-P1 — Swagger com exemplos em todas as operações e busca por texto literal, na `hotfix/2.0.1` conforme `docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md`: GREEN em 2026-09-25.
- F5-P2 — Documentação coerente com a tag (estado das releases, paginação, interpretações, AI_USAGE, CHANGELOG e evidências do F5-L5): GREEN em 2026-09-25.
- F5-P3 — Freeze da `hotfix/2.0.1` e release `v2.0.1`: freeze GREEN em 2026-09-25 (`docs/evidence/F5-P3/SAIDA-GATE.txt`); a tag `v2.0.1` marca o merge desta versão na `main`, e a verificação pública fica em `docs/evidence/F5-P3/SAIDA-RELEASE.txt`, na `develop`.
- F5-Q1 — Código e testes da patch `2.0.2` (E2–E5 e remoção D1), na `hotfix/2.0.2`: GREEN em 2026-09-25 (`docs/evidence/F5-Q1/SAIDA-GATE.txt`); 177 testes unitários/BDD, 52 de integração, JaCoCo 95,17%, BDD 12/12 e Docker/API real GREEN.

### Resumo por lote

### F1-L1 — CRUD

O F1-L1 adiciona CRUD paginado de Projeto e Responsável sobre os mesmos casos de uso para REST e GraphQL, persistência JPA sobre o schema Flyway existente, auditoria de criação/atualização, normalização e unicidade de e-mail e proteção contra remoção de responsável ainda associado a projeto. O contrato padronizado definitivo de erros permanece para F1-L3; neste lote existe apenas o mapeamento HTTP/GraphQL mínimo necessário para o CRUD.

### F1-L2 — Kanban e transições

O F1-L2 adiciona filtro paginado por status em REST/GraphQL e uma política de transição de domínio que aplica exclusivamente as ações automáticas da tabela, recalcula status/métricas e bloqueia qualquer resultado incompatível com o status solicitado. REST e GraphQL reutilizam `ProjectService.transition`.

### F1-L3 — API e qualidade

O F1-L3 padroniza erros REST/GraphQL com códigos estáveis, reforça Bean Validation nas duas fronteiras, publica OpenAPI/Swagger, adiciona índices alinhados às consultas paginadas reais e introduz integração/API tests com PostgreSQL 18.6 descartável via Testcontainers. O gate local foi concluído com exit code 0 e promoveu F1-L3/F1 para GREEN.

### F2-L1 — Segurança

O F2-L1 adiciona Spring Security com autenticação por e-mail/senha, conta administrativa persistida no PostgreSQL, senha armazenada somente por `DelegatingPasswordEncoder`/bcrypt, sessão HTTP com cookie `HttpOnly`/`SameSite=Lax`, autorização `ROLE_ADMIN`, CSRF compatível com SPA por cookie `XSRF-TOKEN`, login/logout, respostas 401/403 sem detalhes sensíveis e testes de integração específicos. REST de health e documentação permanecem públicos; os endpoints de negócio REST e `/graphql` exigem autenticação.

### F2-L2 — Fundação frontend autenticada

O F2-L2 conecta o frontend ao contrato de autenticação já validado no F2-L1. A aplicação passa a ter rota de login e área protegida, sessão validada por `GET /api/v1/auth/me`, login/logout com CSRF obtido do backend, estados explícitos de carregamento e erro e uma estrutura visual Material UI consistente para o painel. O estado remoto de autenticação permanece em React Query; nenhuma credencial é persistida em `localStorage` ou `sessionStorage`.

O roteamento deste lote cobre somente `/login` e `/` usando a History API nativa do navegador. Essa escolha evita dependência adicional para duas rotas e mantém o diff mínimo; a decisão deve ser reavaliada apenas se a navegação do F2-L3 exigir uma árvore de rotas maior.

### F2-L3 — Kanban UI

O F2-L3 adiciona o quadro Kanban autenticado com as quatro colunas do domínio (`NOT_STARTED`, `IN_PROGRESS`, `OVERDUE`, `COMPLETED`), drag-and-drop nativo, criação/edição/exclusão de projetos, cadastro de responsável, associação de responsáveis, feedback das mensagens de domínio e atualização do estado remoto via React Query. As mutações reutilizam o mesmo contrato CSRF validado no F2-L2.

A UI foi reorganizada por responsabilidade: `App.tsx` atua somente como composition root de navegação; autenticação fica em `features/auth`, painel em `features/dashboard`, Kanban em `features/kanban`, navegação em `app/navigation.ts` e estados visuais compartilhados em `components`. O quadro mantém a orquestração de queries/mutações enquanto coluna e diálogos permanecem componentes focados. A camada `api` continua independente do React Query; os callbacks do framework usam adaptadores explícitos.

### F3-L1 — Funcionalidades diferenciais

O F3-L1 adiciona indicadores de projetos, incluindo quantidade por status, média de dias de atraso por status, total e quantidade com atraso; CRUD de Secretaria em REST e GraphQL; e filtros avançados de projetos por status, secretaria, responsável, interseção do período previsto e texto. REST e GraphQL reutilizam o mesmo `ProjectService` e o mesmo `ProjectFilter`.

No frontend, indicadores, gerenciamento de secretarias e filtros ficam em features focadas. O quadro continua responsável pela orquestração do Kanban e invalida indicadores quando mutações de projeto alteram a carteira. Os filtros relacionais e temporais usam os índices já existentes da migration V1/V3; a busca textual por substring permanece sem extensão PostgreSQL adicional para manter o diferencial de baixo risco.

### F3-L2 — Autenticação do responsável + erro seguro

O F3-L2 adiciona o perfil `RESPONSIBLE` ao lado do `ADMIN`, conforme o `docs/governance/REPLANEJAMENTO-F3.md`:

- o ADMIN define ou revoga a senha de um responsável em `PUT`/`DELETE /api/v1/responsibles/{id}/credentials` ou pelas mutations GraphQL `setResponsibleCredentials`/`revokeResponsibleCredentials`; a senha exige no mínimo 12 caracteres e no máximo 72 bytes;
- o responsável entra pelo mesmo `POST /api/v1/auth/login`; `GET /api/v1/auth/me` passa a informar `responsibleId`;
- o responsável vê o quadro inteiro, os indicadores, os responsáveis e as secretarias, e cria, edita, transiciona e exclui apenas os projetos em que é responsável; responsáveis, secretarias e credenciais ficam só com o ADMIN;
- a regra de posse fica na camada de aplicação (`Actor` informado aos casos de uso), reutilizada por REST e GraphQL;
- o e-mail de login do responsável é copiado para `app_users` e sincronizado na mesma transação quando o ADMIN altera o e-mail do responsável; o índice único de login impede colisão com o e-mail do ADMIN (409);
- erros inesperados passam a responder 500 com `code = INTERNAL_ERROR` e `incidentId`, sem detalhes internos; rotas inexistentes respondem 404 em vez de 403.

### F3-L3 — Observabilidade

O F3-L3 adiciona observabilidade ao backend sem alterar regras de negócio:

- Spring Boot Actuator expõe somente `health` e `prometheus`; os demais endpoints ficam indisponíveis. `health` é público e responde só `UP`/`DOWN`, sem componentes nem detalhes;
- `/actuator/prometheus` usa uma cadeia de segurança própria, stateless, com HTTP Basic e a credencial técnica `ROLE_METRICS` (`APP_METRICS_USERNAME`, padrão `prometheus`, e `APP_METRICS_PASSWORD`). Sessão, ADMIN e responsável não leem métricas. Sem senha configurada, o endpoint fica fechado; senha abaixo de 16 caracteres ou acima de 72 bytes impede a subida;
- métricas com a tag `application="facilit-kanban"` e histograma de `http.server.requests` (latência p95), JVM e pool HikariCP;
- logs estruturados em JSON no formato ECS (`@timestamp`, `log.level`, `message`, `ecs.version`) no Docker Compose; a execução local via Maven mantém o log legível;
- `compose.observability.yaml` adiciona Prometheus v3.14.0 e Grafana 13.1.3, ligados só em `127.0.0.1`, com datasource e painel provisionados em `observability/`. A senha de métricas chega ao Prometheus como secret do Compose; nenhuma senha tem valor padrão.

### F5-L1 — Regras sempre corretas

O F5-L1 corrige o principal desvio da v1.0.0 em relação ao desafio: status, dias de atraso e % de tempo restante eram calculados só ao gravar e ficavam congelados com a passagem dos dias.

- migration V6 com `schedule_calculated_on` (data do último cálculo), preenchida com a data UTC da última gravação nos projetos existentes;
- `ProjectScheduleRefresher` recalcula, em lotes de 500, os projetos não concluídos calculados antes de hoje; roda antes de toda leitura e na subida, e por agendamento à meia-noite (`APP_SCHEDULE_REFRESH_CRON`);
- a transição parte do status de hoje, e não do gravado (um projeto gravado como Em andamento e já vencido não passa mais por Em andamento → Atrasado);
- "hoje" passa a ser a data no fuso `America/Sao_Paulo` (`APP_TIME_ZONE`);
- datas realizadas posteriores a hoje são recusadas;
- decisões registradas no [ADR 0002](docs/adr/0002-status-sempre-atual.md) e em `docs/evidence/F5-L1/`.

### F5-L2 — Contrato de erro, confirmações, Swagger e logs

- regras de negócio passam a responder 422 com código próprio (`BUSINESS_RULE_VIOLATION`, `TRANSITION_BLOCKED`, `CONFIRMATION_REQUIRED`); corrida com restrição do banco e atualização concorrente passam a 409 `CONFLICT` em vez de 500;
- mensagens da API em pt-BR, com orientação específica em cada linha bloqueável da tabela de transição; Bean Validation em pt-BR (`spring.web.locale=pt_BR`);
- confirmação obrigatória (`confirm: true`) nas transições que apagam data registrada, em REST, GraphQL e na UI (diálogo com a mensagem do servidor);
- erros documentados em todas as operações do Swagger, com exemplos (`ApiErrorDocumentation`);
- logs de negócio sem dados pessoais;
- coleção Postman atualizada (422 e confirmação).

### F5-L3 — Camadas de teste completas e transação por caso de uso

- porta `TransactionRunner` na aplicação e `SpringTransactionRunner` na infraestrutura; criar, editar, transicionar e excluir rodam inteiros numa transação, e os logs de negócio saem depois do commit;
- migration V7 com a coluna `version` em `projects` e `@Version` na entidade; atualização concorrente responde 409 `CONFLICT` (REST e GraphQL);
- testes de controller com o service simulado: 4 classes `@WebMvcTest` e 3 `@GraphQlTest`;
- `ProjectServiceMockitoTest` verifica interações (bloqueio não grava, recálculo antes da leitura, ordem das chamadas);
- testes de integração novos: `ProjectPersistenceAdapterIT` (`@DataJpaTest`), `TransactionIT` e `StatusTransitionApiIT` (a tabela inteira pela API);
- agente do Mockito configurado explicitamente no Surefire e no Failsafe, como pede a documentação do Mockito para Java 21 ou mais novo.


### F5-L4 — Etapa 3, BDD e cobertura

- indicadores adicionais por secretaria, responsável e janela de prazo em REST e GraphQL;
- `ProjectIndicatorsIT` cobre os contratos e validações da Etapa 3;
- Cucumber/JUnit Platform formaliza as 12 linhas da tabela de transição em Gherkin pt-BR;
- JaCoCo integrado ao `mvn clean verify`, sem exclusões artificiais, com mínimo global de 95% de linhas; a medição de fechamento atingiu 95,61%.

### F5-P1 — Swagger com exemplos e busca literal (patch 2.0.1)

- exemplos em todos os parâmetros, corpos e respostas de sucesso das 26 operações REST, com os mesmos ids fictícios entre recursos (`ApiExamples`); o exemplo dos itens de listas entra por `ApiListExampleDocumentation`;
- `OpenApiContractIT` recusa operação sem exemplo e aponta o campo;
- a busca por texto trata `%`, `_` e `\` como caracteres comuns (`LIKE … ESCAPE`), com teste de integração no PostgreSQL.
