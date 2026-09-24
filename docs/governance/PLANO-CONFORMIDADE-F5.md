# F5 — Plano de conformidade 100% ao desafio

- **Data:** 2026-09-23.
- **Base:** release `v1.0.0` (main `a3497d8`).
- **Prazo de entrega:** 25/09/2026.
- **Governança:**
  - PROMPT-EXECUTIVO-BASE v1.1, depois KANBAN v1.0, depois `docs/governance/REPLANEJAMENTO-F3.md`;
  - este plano reabre o desenvolvimento após o freeze do F4 e só vale com aprovação.
- **Fonte:**
  - PDF do desafio;
  - análise de aderência de 2026-09-23 (85% dos obrigatórios; 81% dos diferenciais e da Etapa 3);
  - relatório do auditor independente.

## 1. Diagnóstico

Uma causa raiz explica metade das lacunas obrigatórias. Status, dias de atraso e % restante são gravados ao salvar, e nada os recalcula com a passagem dos dias. Isso afeta:

- a definição de status;
- as métricas;
- a listagem por status;
- os indicadores;
- a própria transição, que parte do status gravado.

As outras lacunas são pontuais:

- erros de negócio genéricos;
- testes de controller sem mocks;
- "confirmações obrigatórias" não modeladas;
- Swagger sem erros documentados;
- falta de logs de negócio;
- falta de transação no caso de uso;
- datas realizadas no futuro aceitas;
- README sem as interpretações do enunciado.

**Correção da análise anterior (#9, commits):**

- O histórico reconstruído tem commits por área em cada lote (`feat(backend)`, `test(backend)`, `feat(frontend)`, `build`, `docs`) em Conventional Commits [VERIFICADO: `RECONSTRUCAO-HISTORICO.md:96-112`].
- Isso é granular. Falta só o tipo `refactor`, que o F5 vai produzir naturalmente.
- Reclassifico #9 como **Atende** após o F5, **sem reescrever o histórico publicado**.

## 2. Decisões de desenho

### D1 — Status sempre atual (causa raiz)

- **Migration `V6`:**
  - coluna `schedule_calculated_on DATE NOT NULL`, com a data de hoje para as linhas existentes;
  - índice parcial `WHERE status <> 'COMPLETED'`.
- **`ProjectScheduleRefresher` (aplicação):**
  - `refreshIfStale()` busca projetos não concluídos com `schedule_calculated_on < hoje`;
  - recalcula em lotes de 500 e grava status, atraso, % e `schedule_calculated_on`.
  - **Não altera `updatedAt`**, porque recálculo do sistema não é edição do usuário (decisão registrada no ADR 0002).
- **Quando roda:**
  - no início de `get`, `list`, `listByStatus`, `search`, `indicators` e `transition`;
  - a verificação é uma consulta indexada e barata;
  - há também um `@Scheduled` à meia-noite, só para aquecer.
- **Segurança da estratégia:** o estado fica no banco, não em memória. Por isso é seguro com várias instâncias e com reinícios, e é idempotente.
- **Projetos concluídos:** não mudam com o tempo (término realizado → Concluído, 0, 0), então ficam fora do recálculo.
- **Transição:** calcula o status de origem pelas datas e hoje, em vez de ler o gravado. Isso fecha o achado Em andamento → Atrasado.
- **Alternativa descartada:** status calculado em SQL a cada consulta. Perde o índice por status, duplica a regra em SQL e em Java e custa mais.

### D2 — Fuso

`Clock.system(ZoneId.of(${APP_TIME_ZONE:America/Sao_Paulo}))`. Hoje "hoje" é UTC: a partir das 21h locais, já é o dia seguinte.

### D3 — Modelo de erros

| HTTP | `code` | Quando |
|---|---|---|
| 400 | `INVALID_REQUEST` | sintaxe, Bean Validation, parâmetro inválido (não muda) |
| 422 | `TRANSITION_BLOCKED` | transição bloqueada pela tabela, com dicas |
| 422 | `CONFIRMATION_REQUIRED` | transição exige confirmação (D4) |
| 422 | `BUSINESS_RULE_VIOLATION` | outras regras de domínio (ordem das datas, data realizada futura, término previsto obrigatório) |
| 409 | `CONFLICT` | unicidade, recurso em uso, `DataIntegrityViolationException`, `ObjectOptimisticLockingFailureException` (hoje a corrida vira 500) |

**Exceções e mensagens:**

- As exceções de domínio substituem `IllegalArgumentException` nas regras de negócio.
- As mensagens passam a ser em **pt-BR**, com dica específica em todas as linhas da tabela, inclusive as linhas 1 e 4 e a borda das linhas 6 e 9.
- O GraphQL usa os mesmos códigos em `extensions.code`.

**Impacto no frontend e na coleção:**

- O frontend só testa 401 [VERIFICADO: `frontend/src/api/auth.ts:136`, `LoginPage.tsx:41`]; ele exibe a mensagem.
- A coleção Postman e o smoke por `curl` mudam.

### D4 — Confirmações obrigatórias

**Interpretação (a documentar no README):** confirmação é exigida quando a ação automática **apaga uma data já registrada**, porque a informação se perde. Isso vale para as linhas:

- 4: Em andamento → A iniciar;
- 11: Concluído → Em andamento;
- 12: Concluído → Atrasado.

**Contrato:**

- REST: `PATCH /api/v1/projects/{id}/status` com `{ "status": …, "confirm": true }`.
- GraphQL: `transitionProject(id, status, confirm: Boolean = false)`.
- Sem `confirm`, a resposta é 422 `CONFIRMATION_REQUIRED` com o efeito descrito (qual data será apagada).
- As outras linhas ignoram o campo.

**UI:** ao receber `CONFIRMATION_REQUIRED`, abre um diálogo com a mensagem do servidor e reenvia com `confirm: true`. A regra fica no servidor, não duplicada no cliente.

### D5 — Transação e concorrência

- **Porta `TransactionRunner` na aplicação:** a implementação na infraestrutura usa `TransactionTemplate`. Cada caso de uso de escrita roda inteiro numa transação. A aplicação continua sem Spring.
- **`V6` também:** coluna `version BIGINT NOT NULL DEFAULT 0` em `projects`, com `@Version` na entidade. Dois updates simultâneos resultam em 409.

### D6 — Versão

As mudanças D3 e D4 quebram o contrato da API: o status HTTP e o código dos erros de transição mudam, e `confirm` passa a ser exigido nas linhas 4, 11 e 12.

Em SemVer, isso é **`2.0.0`**. A alternativa seria manter 400 e não exigir confirmação, e aí não atinge 100%.

Precisa da sua decisão (pergunta 2).

## 3. Lotes

Cada lote segue o ritual de sempre:

- pacote `tar.gz` + SHA-256;
- `VERIFICACAO-USUARIO.md` com marcadores;
- branch `feature/f5-lN-…` saindo de `develop`, merge `--no-ff`;
- commits granulares (`test`, `feat`/`fix`, `refactor`, `docs`).

O RED do TDD fica registrado em `EXECUCAO.md`, não em commit, para não quebrar o CI da branch.

### F5-L1 — Regras sempre corretas (P0, obrigatório)

| Entrega | Arquivos principais (em `backend/src/main/java/br/com/facilit/kanban/`, salvo indicação) |
|---|---|
| D1 recálculo | `application/project/ProjectScheduleRefresher.java` (novo), `application/project/ProjectService.java`, `infrastructure/persistence/project/ProjectPersistenceAdapter.java`, `ProjectJpaRepository.java`, `ProjectJpaEntity.java`, `infrastructure/config/ScheduleRefreshJob.java` (novo), `backend/src/main/resources/db/migration/V6__schedule_freshness_and_version.sql` (novo) |
| Transição sobre status recalculado | `domain/project/ProjectStatusTransition.java` |
| D2 fuso | `infrastructure/config/ApplicationBeans.java`, `backend/src/main/resources/application.yml`, `compose.yaml`, `.env.example` |
| Rejeitar data realizada futura | `domain/project/ProjectDates.java` ou `ProjectService` (validação com `today`) |
| Interpretações do enunciado | `README.md` (nova seção "Interpretações do enunciado"; corrige a linha 77), `docs/adr/0002-status-sempre-atual.md` (novo) |

**Testes que fecham o lote:**

- `ProjectScheduleRefresherTest`, com relógio fixo que avança 10 dias: um projeto Em andamento com 0 dias e 67% passa a Atrasado, com 8 dias e 0%;
- `ProjectStatusTransitionTest`: o caso gravado como Em andamento mas vencido;
- `ProjectDatesTest` / `ProjectServiceTest`: data realizada futura → erro;
- `ScheduleFreshnessIT` (Testcontainers, `MutableClock` de teste):
  - GET, listagem por status e indicadores mudam após o avanço do relógio;
  - `updatedAt` não muda;
  - projeto concluído intocado.

**Fecha:** #13, #17, #18, #27, #32, #39; parte de E3a e E3b.

### F5-L2 — Contrato de erro, confirmações, Swagger e logs (P0, obrigatório)

| Entrega | Arquivos principais |
|---|---|
| D3 exceções e códigos | `domain/common/BusinessRuleException.java`, `domain/project/TransitionBlockedException.java`, `domain/project/ConfirmationRequiredException.java` (novos); `delivery/rest/RestExceptionHandler.java`; `delivery/graphql/GraphQlErrorHandler.java`; mensagens pt-BR em `ProjectStatusTransition.java` e `ProjectScheduleCalculator.java` |
| D4 confirmação | `ProjectStatusTransition.java`, `ProjectService.transition`, `delivery/rest/ProjectStatusRequest.java`, `delivery/rest/ProjectRestController.java`, `backend/src/main/resources/graphql/kanban.graphqls`, `delivery/graphql/ProjectGraphQlController.java`; frontend `frontend/src/features/kanban/` (diálogo) e `frontend/src/api/` |
| Swagger com erros | `@ApiResponses` com exemplos de ProblemDetail em **todos** os endpoints de `delivery/rest/*RestController.java` |
| Logs de negócio | `log.info` em criar, editar, transicionar, excluir e no recálculo (resumo), com id do projeto, de→para e id do ator, **sem e-mail nem nome**; no serviço, via porta `AuditLog` ou SLF4J no adapter de aplicação |
| Coleção e smoke | `docs/api/facilit-kanban.postman_collection.json`; cenários `curl` do gate |

**Testes que fecham o lote:**

- `ProjectStatusTransitionTest`: 12 linhas × (sucesso, bloqueio quando previsto, confirmação exigida nas linhas 4, 11 e 12), com a mensagem pt-BR verificada;
- `RestExceptionHandlerTest`: 422, 409 (`DataIntegrityViolationException`, lock otimista);
- `OpenApiContractIT`: **todo** endpoint tem resposta 4xx com schema ProblemDetail e exemplo;
- `BusinessLogIT` ou teste com `OutputCaptureExtension`: o log de transição contém o id e não contém o e-mail;
- Vitest: o diálogo de confirmação reenviando com `confirm: true`.

**Fecha:** #28, #29 (parte de confirmação), #31, #33, #40.

### F5-L3 — Camadas de teste completas (P0, obrigatório)

| Entrega | Arquivos (em `backend/src/test/java/br/com/facilit/kanban/`) |
|---|---|
| Controllers com mocks | `delivery/rest/ProjectRestControllerTest`, `ResponsibleRestControllerTest`, `SecretariatRestControllerTest`, `ProjectIndicatorsRestControllerTest` (`@WebMvcTest` + `@MockitoBean` do service; 200/201/204, 400, 403, 404, 409, 422); `delivery/graphql/ProjectGraphQlControllerTest`, `ResponsibleGraphQlControllerTest`, `SecretariatGraphQlControllerTest` (`@GraphQlTest` + `@MockitoBean`) |
| Service com Mockito | `application/project/ProjectServiceMockitoTest` (por exemplo: bloqueio não chama `save`; o recálculo roda antes da leitura) |
| D5 transação | `application/common/TransactionRunner.java` (novo), `infrastructure/config/SpringTransactionRunner.java` (novo), `ApplicationBeans.java`; `@Version` em `ProjectJpaEntity` |
| Repositório e transação | `integration/ProjectPersistenceAdapterIT` (`@DataJpaTest` + Testcontainers: `findByStatus`, `summarizeByStatus`, filtros, recálculo em lote); `integration/TransactionIT` (rollback de caso de uso que falha no meio; update concorrente → 409) |
| Tabela pela API | `integration/StatusTransitionApiIT`: as 12 linhas pela API REST e 3 pela GraphQL, incluindo bloqueio e confirmação |

**Fecha:** #24, #29, #37.

### F5-L4 — Etapa 3 e diferenciais restantes (P1, se houver folga)

| Entrega | Detalhe |
|---|---|
| Novos endpoints de indicadores | `GET /api/v1/indicators/projects/by-secretariat`, `GET /api/v1/indicators/projects/by-responsible`, `GET /api/v1/indicators/projects/deadlines?withinDays=7` (1–90, fora da faixa → 400), mais as queries GraphQL equivalentes |
| Testes da Etapa 3 | médias diferentes de zero, casos de erro, `ProjectIndicatorsIT` |
| BDD | `backend/src/test/resources/features/transicoes.feature` (`# language: pt`, Esquema do Cenário com as 12 linhas) + passos em `backend/src/test/java/br/com/facilit/kanban/bdd/`; Cucumber via JUnit Platform Suite. A versão só entra se não estiver depreciada e suportar Java 25 [NÃO VERIFICADO ainda] |
| Cobertura no CI | JaCoCo com relatório e limite mínimo medido antes de fixar; suporte a Java 25 [NÃO VERIFICADO ainda] |

**Fecha:** E3a–E3d, D8 e reforça D7.

### F5-L5 — Release e entrega (P0)

- `CHANGELOG.md` (novo), com as quebras de contrato;
- versão em `backend/pom.xml`, `backend/Dockerfile` e `frontend/package.json`;
- `docs/evidence/F5/` (GATE, MATRIZ, EXECUCAO, AUDITORIA atualizada com 100%);
- gate de freeze completo (o do F4 adaptado), `release/<versão>` → `main` + tag + back-merge, CI verde.

## 4. Matriz: lacuna → lote → critério de aceite

| # | Requisito | Hoje | Lote | Aceite objetivo |
|---|---|---|---|---|
| 9 | Commits granulares | Parcial → **Atende** (reclassificado) | todos | commits `test`/`fix`/`refactor`/`docs` separados em cada lote F5 |
| 13 | Definição dos status | Parcial | L1 | `ScheduleFreshnessIT` verde |
| 17 | % restante | Parcial | L1 | idem |
| 18 | Dias de atraso | Parcial | L1 | idem |
| 24 | Testes de controller com mocks | Parcial | L3 | 7 classes `@WebMvcTest`/`@GraphQlTest` com `@MockitoBean` |
| 27 | Listagem por status | Parcial | L1 | `ScheduleFreshnessIT` (listagem) |
| 28 | Mensagens claras | Atende (auditor: Parcial) | L2 | mensagem pt-BR específica testada nas 12 linhas |
| 29 | Testes linha a linha com confirmações | Parcial | L2 + L3 | `ProjectStatusTransitionTest` + `StatusTransitionApiIT` |
| 31 | Erros padronizados | Parcial | L2 | `RestExceptionHandlerTest` (422/409) |
| 32 | Validação de entrada | Parcial | L1 | teste de data realizada futura |
| 33 | Logs adequados | Parcial | L2 | teste de log de negócio sem PII |
| 37 | Integração: repositórios e transações | Parcial | L3 | `ProjectPersistenceAdapterIT` + `TransactionIT` |
| 39 | README coerente | Atende com ressalva | L1 | seção de interpretações; linha 77 corrigida |
| 40 | Swagger com erros | Parcial | L2 | `OpenApiContractIT` |
| E3a–d, D8 | Etapa 3 e BDD | Parcial | L4 | testes listados no L4 |

**Resultado esperado:**

- Com L1 a L3 e L5: **obrigatórios em 100%** (40/40).
- Com L4: diferenciais e Etapa 3 em 100% (13/13).
- Único ponto sem garantia: "análise" no CI além de lint e `-Xlint -Werror`, que depende de JaCoCo suportar Java 25.

## 5. Cronograma (horário de Recife)

| Quando | Lote | Estimativa |
|---|---|---|
| 23/09 noite | aprovação + F5-L1 | 4–5 h |
| 24/09 manhã | F5-L2 | 5 h |
| 24/09 tarde | F5-L3 | 5 h |
| 24/09 noite | F5-L4 (opcional) | 4 h |
| 25/09 manhã | F5-L5 release e entrega | 1,5 h |

**Linha de corte:** se o L3 não estiver GREEN até 24/09 às 22h, o L4 sai do escopo. Em 25/09 às 12h, lança-se o que estiver GREEN. A `v1.0.0` continua válida como entrega mínima.

## 6. Riscos

| Risco | Mitigação |
|---|---|
| Prazo (≈ 20 h de trabalho em 1,5 dia) | ordem P0 primeiro; linha de corte; cada lote é entregável sozinho |
| Quebra de contrato (D3/D4) | versão 2.0.0, CHANGELOG, frontend, coleção e smoke atualizados no mesmo lote |
| Corrida entre recálculo e edição do usuário | recálculo idempotente; em conflito de `@Version`, o recálculo pula a linha (a edição já recalcula) |
| `V6` em banco com dados | `DEFAULT` + backfill na própria migration; gate com banco novo **e** com banco da v1.0.0 |
| Dependência nova depreciada ou sem Java 25 (Cucumber, JaCoCo) | verificar antes de adotar; se falhar, o item fica declarado como limitação e não é forçado |
