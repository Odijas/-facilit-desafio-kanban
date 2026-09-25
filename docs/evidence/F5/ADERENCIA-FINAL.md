# Aderência final ao desafio: Facilit Kanban (release/2.0.0)

> **Estado deste documento:** análise do snapshot `release/2.0.0` @ `59051d0`, antes dos lotes de correção. As lacunas 1 e 3 a 8 foram tratadas nos lotes F5-C1 a F5-C3 (`docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md`, evidências em `docs/evidence/F5-C1/` a `F5-C3/`). A lacuna 2 (release não publicada) se fecha com o freeze e a release do F5-L5. A lacuna 9 (histórico reconstruído) ficou declarada no README. O resultado final está em `docs/evidence/F5/AUDITORIA.md`.

- **Data:** 2026-09-24.
- **Snapshot:** `facilit-desafio-kanban-auditoria-final-20260924-212402.tar.gz`, com SHA-256 `43051359…abac17fd718f`, 404 arquivos e nenhum indevido.
- **Estado do repositório no snapshot:** branch `release/2.0.0`, HEAD `59051d0`, árvore limpa. A última tag é `v1.0.0`.
- **Fonte dos requisitos:** o PDF "Desafio Técnico Backend — Kanban", lido inteiro.
- **Método:** duas análises independentes sobre o mesmo pacote.
  1. **Auditor independente.** Não viu esta análise, a de 23/09 nem as matrizes do projeto. Ele:
     - compilou o domínio com javac;
     - rodou um harness sobre todas as combinações de 4 datas (nulo e hoje ±3 dias);
     - executou 6.280 transições (4 origens × 4 destinos × com e sem confirmação);
     - encontrou **0 violações** da tabela e das definições de status.
  2. **Esta análise.** Evidência lida no código (`arquivo:linha`). A execução vem das saídas `SAIDA-*.txt` rodadas pelo usuário.
- **Pontuação:** Atende = 1, Parcial = 0,5, Não atende = 0.
- **Legenda:**
  - **⚠** marca onde as duas análises divergiram.
  - "Base 23/09" é a classificação em `ADERENCIA-DESAFIO.md`.

## Resultado

| Grupo | Base 23/09 (v1.0.0) | Esta análise | Auditor independente |
|---|---|---|---|
| Obrigatórios | 85% (34/40) | **95%** (38/40) | 94,9% (49 itens, outra granularidade) |
| Diferenciais e Etapa 3 (opcional) | 81% (10,5/13) | **96%** (12,5/13) | 93,3% (15 itens) |

**O que as duas análises concluem juntas:**

- Os itens que o plano F5 prometia fechar estão no código e têm teste. São eles:
  - status, dias de atraso e % de tempo restante de hoje;
  - transição partindo do status de hoje;
  - 422 com código próprio e confirmação obrigatória;
  - erros documentados no Swagger;
  - logs de negócio;
  - controllers testados com mocks;
  - transação por caso de uso e `@Version`;
  - as 12 linhas da tabela pela API;
  - Etapa 3;
  - BDD.
- As regras estão corretas contra o enunciado: o harness do auditor não achou nenhuma divergência.

**Os percentuais valem para a `release/2.0.0`, que ainda não foi publicada.** Não há tag `v2.0.0`, o freeze do F5-L5 está pendente (`docs/evidence/F5/GATE.md`) e a `main` continua na v1.0.0. Se o avaliador abrir o repositório agora, vê a v1.0.0, que tinha **85%** e o defeito do status congelado.

## Obrigatórios (mesma numeração da base de 23/09)

| # | Requisito (PDF) | Base | Agora | Evidência / motivo |
|---|---|---|---|---|
| 1 | Java 8+, JUnit, Git | A | A | Java 25, JUnit 5 + Cucumber (`backend/pom.xml`) |
| 2 | Docker Compose com aplicação e banco | A | **P** | `compose.yaml` ok. `backend/Dockerfile:6` roda `mvn -DskipITs verify`, e o `jacoco:check` de 95% de linhas está ligado a `verify` (`pom.xml:204-217`). Sem os IT, a cobertura não chega a 95%: a `infrastructure` tem cerca de 31% das linhas de código e é exercida quase só por IT. **`docker compose up --build` deve quebrar no build.** [INFERÊNCIA: não executado] O gate do F5-L4 não teve etapa Docker, e o CI não constrói a imagem. |
| 3 | API REST | A | A | `delivery/rest/*Controller.java` |
| 4 | Repositório público no GitHub | A | A | `F4/SAIDA-RELEASE.txt`; ver "Estado da entrega" |
| 5 | README (visão, decisões, Docker, testes, Swagger, pastas, limitações) | A | A | seções presentes. O "Como rodar" depende do item 2. |
| 6 | AI_USAGE.md (4 itens da Etapa 4) | A | A | `AI_USAGE.md:14-120`. Ressalva: não menciona a F5. |
| 7 | Coleção Postman/Insomnia | A | A | 24 requisições com 422 e confirmação. Faltam os 3 endpoints de indicadores do F5-L4. |
| 8 | Diagramas | A | A | mermaid no README e nos ADRs 0001/0002 |
| 9 | Histórico de commits granular | P | P | F5 com commits granulares; F2-L1 a F3-L4 reconstruídos por lote (`F3-L4/RECONSTRUCAO-HISTORICO.md`) |
| 10–12 | Campos de Projeto e Responsável; N responsáveis | A | A | `Project.java`, `Responsible.java`, `V1` |
| 13 | Definição dos status | P | **A** | recálculo antes das leituras, na subida e à meia-noite, no fuso de negócio; `ScheduleFreshnessIT`; harness sem divergências |
| 14 | Editar datas recalcula o status | A | A | `ProjectService.update` |
| 15 | 12 transições | A | A | `ProjectStatusTransition`; origem = status de hoje; 6.280 transições sem violação |
| 16 | Recalcular e bloquear com dicas | A | A | mensagem específica por linha, 422 `TRANSITION_BLOCKED` |
| 17 | % de tempo restante | P | **A** | fórmula correta e valor de hoje |
| 18 | Dias de atraso | P | **A** | idem |
| 19–23 | CRUD, recálculo, e-mail único, auditoria, Swagger publicado | A | A | — |
| 24 | JUnit de services **e controllers** com **mocks** | P | **A** | 4 classes `@WebMvcTest` + 3 `@GraphQlTest` com `@MockitoBean`; `ProjectServiceMockitoTest` |
| 25 | Testes de status com erro e bloqueio | A | A | `ProjectStatusTransitionTest` (24) |
| 26 | Endpoint de transição | A | A | `PATCH /api/v1/projects/{id}/status` com `confirm` |
| 27 | Listagem por status | P | **A** | recálculo antes de `listByStatus` |
| 28 | Mensagens de erro claras | A | A | pt-BR, com o campo a ajustar |
| 29 | Testes linha a linha: felizes, bloqueios, confirmações **e cálculo de status/métricas** | P | **P** | Felizes, bloqueios e confirmações cobertos em três camadas: domínio, API (`StatusTransitionApiIT`) e BDD (`transicoes.feature`). Falta conferir as **métricas** por linha: `delayDays` só aparece na linha 12, e `remainingTimePercentage` em nenhuma. |
| 30 | Clean Code, SOLID, padrões | A | A | camadas, portas e composition root |
| 31 | Erros padronizados | P | **A** | 422 por tipo de regra; 409 para integridade e concorrência |
| 32 | Validação de entrada | P | **P** | Data realizada futura: corrigida. Novo motivo: **nenhum `@Size`/`@Max`** no código, então nome, cargo, e-mail, texto do filtro e `responsibleIds` não têm limite de tamanho (colunas `TEXT`). |
| 33 | Logs adequados | P | **A** | `BusinessLog` sem dado pessoal; conferido no log ECS real (F5-L2) |
| 34 | Paginação e índices | A | A | — |
| 35 | Decisões documentadas | A | A | README e ADRs 0001/0002 |
| 36 | Testes de unidade | A | A | — |
| 37 | Integração: repositórios e transações | P | **A** | `ProjectPersistenceAdapterIT` e `TransactionIT` (rollback, edição concorrente) |
| 38 | Testes de API | A | A | `StatusTransitionApiIT`, `ProjectIndicatorsIT` e os anteriores |
| 39 | README coerente com o código | A | A | Ressalvas menores: "Como rodar" (item 2); cita o plano e o L5 como pendentes; a coleção "percorre indicadores" só em parte. |
| 40 | Swagger com exemplos, schemas e **mensagens de erro** | P | **A** ⚠ | Todas as operações documentam erros com exemplo (`ApiErrorDocumentation`, `OpenApiContractIT`). **O "Try it out" do Swagger UI não funciona nas operações autenticadas, nem no login.** O CSRF é exigido (`SecurityConfiguration.java:61-64`) e `springdoc.swagger-ui.csrf.enabled` não está ligado (`application.yml:37-43`). [INFERÊNCIA: não executado] O auditor marcou Parcial; mantenho Atende porque o texto pede conteúdo documentado, e trato o Try it out como a lacuna 3. |

**Contagem:** 36 Atende + 4 Parciais (2, 9, 29 e 32) = **38/40 = 95%**.

## Diferenciais e Etapa 3

| # | Item | Base | Agora | Evidência / motivo |
|---|---|---|---|---|
| D1 | Camada de IA (ADR) | A | A | `docs/adr/0001-…` |
| D2 | UI com drag-and-drop | A | A | `frontend/src/features/kanban`; diálogo de confirmação |
| D3 | CRUD de Secretaria | A | A | — |
| D4 | Filtros avançados | A | A | — |
| D5 | GraphQL (Etapas 1, 2 e 3) | A | A | `kanban.graphqls`, inclusive indicadores novos |
| D6 | Observabilidade e logs estruturados | A | A | — |
| D7 | CI (build, testes e análise) | A | A ⚠ | Biome, tsc strict, `-Werror` e JaCoCo 95%. Sem SAST, sem CD e sem build da imagem Docker. O auditor marcou Parcial. |
| D8 | BDD e TDD | P | P | BDD em Cucumber pt-BR (12 linhas) ✓; TDD sem evidência no histórico |
| D9 | Autenticação do responsável | A | A | — |
| E3a | Média de atraso por status | P | **A** | sobre o status de hoje |
| E3b | Contagem por status | P | **A** | idem |
| E3c | Novos endpoints | P | **A** | `/by-secretariat`, `/by-responsible`, `/deadlines?withinDays` (1–90), mais GraphQL |
| E3d | Testes de sucesso e erro | P | **A** | `ProjectIndicatorsRestControllerTest` (6, com 400 e 500) e `ProjectIndicatorsIT` |

**Contagem:** 12 Atende + 1 Parcial = **12,5/13 = 96%**.

## Divergências entre as duas análises

| Item | Auditor | Esta análise | Resolução |
|---|---|---|---|
| Swagger (#40 / D7 do auditor) | Parcial | Atende + lacuna | O conteúdo pedido existe e é testado. O Try it out quebrado é real e está na lacuna 3; muda a nota, não a correção. |
| CI (D7) | Parcial | Atende | "Análise" existe (lint, `-Werror`, cobertura). Faltam SAST e build da imagem; o build da imagem teria pego a lacuna 1. |
| 422 `BUSINESS_RULE_VIOLATION` sem término previsto nas linhas 1, 11 e 12 | inconsistência menor | decisão do F5-L2 | Término previsto obrigatório é regra de dados, não de tabela. Fica como ponto de entrevista. |

## Lacunas por impacto (nada foi aplicado)

| # | Lacuna | Impacto | Menor correção | Esforço |
|---|---|---|---|---|
| 1 | Build Docker do backend deve falhar no JaCoCo (item 2) | **Crítico**: o avaliador não sobe a aplicação pelo README. O freeze do F5-L5 vai acusar no `up --build`. | `backend/Dockerfile:6`: `RUN mvn -B -ntp -DskipTests -Djacoco.skip=true package` (os testes já rodam no CI e no gate). Opcional: job de CI com `docker build backend`. | PP |
| 2 | Release 2.0.0 não publicada | **Crítico**: a `main` é a v1.0.0 (85%, status congelado) | Depois da lacuna 1: rodar o freeze do F5-L5, merge `--no-ff` na `main`, tag `v2.0.0`, back-merge e CI verde, antes do prazo de 25/09 | P |
| 3 | Swagger UI sem CSRF | Alto: o avaliador costuma testar pelo Swagger | `application.yml` → `springdoc.swagger-ui.csrf.enabled: true` e uma linha no README ("chame `GET /api/v1/auth/csrf` e depois o login") | PP |
| 4 | Métricas por linha da tabela (#29) | Médio | Colunas `atraso` e `percentual` no `Exemplos` de `transicoes.feature` e passo que as confere | P |
| 5 | Sem limite de tamanho nas entradas (#32) | Médio-baixo | `@Size(max=…)` em `name`, `position`, `email` (320), `text` e `responsibleIds` (REST e GraphQL) | P |
| 6 | Coleção sem os indicadores novos (#7) | Baixo | 3 requisições na pasta de indicadores | PP |
| 7 | AI_USAGE sem a F5 | Baixo | Um parágrafo: a auditoria independente achou o status congelado da v1.0.0, e o plano F5 corrigiu. É a melhor "correção da IA" do projeto. | PP |
| 8 | README com texto de trabalho (plano, L5 pendente) | Baixo | Atualizar no fechamento da release | PP |
| 9 | Histórico reconstruído (#9, D8) | Baixo | Não reescrever; uma linha no README declarando a reconstrução de F2-L1 a F3-L4 | PP |

**Esforço:** PP = minutos · P = até ~1 h.

**Sugestão de ordem:** as lacunas 1 e 3 (e, se couber, 4 a 8) vão num commit de correção na própria `release/2.0.0`, antes do freeze. Depois vem a lacuna 2. A 9 fica como está.
