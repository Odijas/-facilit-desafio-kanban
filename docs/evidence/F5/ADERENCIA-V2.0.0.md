# Aderência ao desafio: Facilit Kanban `v2.0.0`

- **Data:** 2026-09-24.
- **Snapshot:** `facilit-desafio-kanban-v2.0.0-auditoria-20260924-225307.tar.gz` (`git archive v2.0.0`), com SHA-256 `941276281d0aa62e16b8b5099273f4798249789360d5b86e465a9f7386de2177`, 439 arquivos e nenhum indevido.
- **Estado publicado:** tag `v2.0.0` = `main` @ `3dac1ea`; `develop` contém a release; CI da `main` verde (run 36082635036); GitHub público e GitLab sincronizados.
- **Fonte dos requisitos:** o PDF "Desafio Técnico Backend — Kanban", lido inteiro.
- **Método:** duas análises sobre o mesmo pacote.
  1. **Agente independente**, sem acesso a esta análise nem às anteriores. Ele:
     - rodou um oráculo de status sobre 338 combinações de datas (**0 divergências**);
     - rodou a matriz completa de transições (origem × destino × confirmação), sem violação da tabela.
  2. **Esta análise**, que conferiu cada afirmação do agente no código (`arquivo:linha`) e refutou duas.
- **Pontuação:** Atende = 1, Parcial = 0,5, Não atende = 0. A numeração é a mesma de `ADERENCIA-DESAFIO.md` (23/09) e `ADERENCIA-FINAL.md` (release candidate).
- **Nada foi aplicado.** Este relatório não altera o repositório.

## Resultado

| Grupo | 23/09 (`v1.0.0`) | RC (`ADERENCIA-FINAL`) | **`v2.0.0` (esta análise)** | Agente independente |
|---|---|---|---|---|
| Obrigatórios | 85% (34/40) | 95% (38/40) | **97,5% (39/40)** | 97,7% (grupos A+C+D, outra granularidade) |
| Diferenciais e Etapa 3 | 81% (10,5/13) | 96% (12,5/13) | **96% (12,5/13)** | 91,7% (grupo B, 12 itens) |

**O que mudou desde o RC:** os itens 2 (Docker), 29 (métricas linha a linha) e 32 (limites de entrada) passaram a Atende, pelos lotes F5-C1 e F5-C2. O item 40 caiu de Atende para Parcial: com o Try it out corrigido, o que resta é a falta de exemplos de resposta, e aqui adotei a leitura do agente.

## Obrigatórios

Só as linhas que mudaram desde o RC, ou que têm ressalva nova, trazem detalhe. As demais continuam Atende com a evidência de `ADERENCIA-FINAL.md`.

| # | Requisito (PDF) | RC | **v2.0.0** | Evidência / motivo |
|---|---|---|---|---|
| 1 | Java 8+, JUnit, Git | A | A | Java 25, JUnit 5 e Cucumber |
| 2 | Docker Compose com aplicação e banco | P | **A** | `backend/Dockerfile`: `-DskipITs -Djacoco.skip=true verify`. O freeze fez `up --build` do zero, e o CI constrói a imagem. |
| 3 | API REST | A | A | — |
| 4 | Repositório público no GitHub | A | A | `F5/VERIFICACAO-RELEASE.md` GREEN |
| 5 | README (visão, decisões, Docker, testes, Swagger, pastas, limitações) | A | A | Tem o roteiro Swagger/GraphiQL com CSRF. Ressalvas de texto: lacuna 3. |
| 6 | AI_USAGE.md | A | A | Seção F5 honesta. Ressalva em `AI_USAGE.md:82` (lacuna 3). |
| 7 | Coleção Postman/Insomnia | A | A | 27 requisições; newman com 52 asserções e 0 falhas |
| 8 | Diagramas | A | A | — |
| 9 | Histórico de commits granular | P | **P** | Do F4 em diante é granular. F2-L1 a F3-L4 foram reconstruídos por lote, e o README declara isso (`README.md:288`). |
| 10–28 | Campos, status, transições, métricas, CRUD, Swagger publicado, testes de controller e status | A | A | O oráculo do agente confirmou: status 0/338 divergências; transições sem violação. |
| 29 | Testes linha a linha, incluindo cálculo de status/métricas | P | **A** | `transicoes.feature` com colunas `atraso` e `percentual` nas 12 linhas |
| 30–31 | Clean Code, erros padronizados | A | A | Ressalvas menores: lacunas 4 e 5. |
| 32 | Validação de entrada | P | **A** | `InputLimits`: `@Size` em REST e GraphQL; filtro com até 100 → 400 |
| 33–38 | Logs, paginação/índices, decisões, testes de unidade, integração e API | A | A | 174 testes unitários/BDD, 49 IT, JaCoCo 95,96% no freeze |
| 39 | README coerente com o código | A | A | Frases desatualizadas dentro da tag (lacuna 3); não contradizem o código em comportamento. |
| 40 | Swagger com exemplos de requests/responses, schemas e mensagens de erro | A | **P** | Schemas e erros documentados em todas as operações (`OpenApiContractIT`); Try it out funciona com CSRF. **Exemplos explícitos só em parte:** `@Schema(example)` só em `ProjectRequest`, `ProjectStatusRequest` e `ResponsibleRequest`; `@ExampleObject` de request/resposta só no create de `ProjectRestController` e `ResponsibleRestController`. `SecretariatRequest`, os records de resposta e as demais operações usam o exemplo gerado pelo schema. |

**Contagem:** 38 Atende + 2 Parciais (9 e 40) = **39/40 = 97,5%**.

## Diferenciais e Etapa 3

| # | Item | RC | **v2.0.0** | Evidência / motivo |
|---|---|---|---|---|
| D1–D6 | IA (ADR), UI drag-and-drop, Secretaria, filtros, GraphQL, observabilidade | A | A | — |
| D7 | CI/CD (GitHub Actions com build, testes e análise) | A | **A** ⚠ | 3 jobs com build, testes, JaCoCo 95%, Biome, tsc strict, `-Werror` e agora `docker build`. Sem SAST e sem deploy. |
| D8 | BDD e TDD | P | **P** | BDD em Cucumber pt-BR ✓; o TDD não aparece no histórico. |
| D9 | Autenticação | A | A | — |
| E3a–E3d | Etapa 3: indicadores, endpoints novos e testes | A | A | Coleção com os 4 indicadores novos |

**Contagem:** 12 Atende + 1 Parcial = **12,5/13 = 96%**.

## Divergências entre as análises

| Ponto | Agente | Esta análise | Resolução |
|---|---|---|---|
| CI/CD (D7) | Parcial (sem CD) | Atende | O parêntese do PDF (seção de diferenciais) define o item como "build, testes e análise", e os três estão presentes. Falta de CD fica como ressalva. |
| Swagger (#40) | Parcial | **Parcial** (adotado) | Conferido no código: os exemplos explícitos cobrem só parte das operações. |
| "A `develop` não foi verificada" | lacuna | **refutado** | `F5/VERIFICACAO-RELEASE.md`, linhas 39 e 46, conferem a `develop` contendo a release; saída GREEN. |
| "`SAIDA-GATE` aponta `9eca274`, e a tag é `3dac1ea`" | inconsistência | **refutado** | `3dac1ea` é o merge de `0342594` (`9eca274` + só o arquivo de saída), e o gate exige a árvore da `main` igual à da release. |
| `ProjectService.list`/`listByStatus` sem uso no `main` | código morto | **confirmado em parte** | O grep só achou chamadas de `list` para Secretaria e Responsável no código de produção; esses dois métodos só são chamados por testes. |

## Lacunas remanescentes (nada foi aplicado)

Qualquer correção exige uma **patch release `2.0.1`** (branch `hotfix/2.0.1` a partir da `main`, freeze e tag nova). A `v2.0.0` publicada não muda.

| # | Lacuna | Impacto | Menor correção | Esforço |
|---|---|---|---|---|
| 1 | Histórico F2-L1 a F3-L4 reconstruído (#9, D8) | Baixo; declarado | Nenhuma. Reescrever histórico publicado seria pior. Fica como ponto de entrevista. | — |
| 2 | Exemplos de resposta no Swagger (#40) | Médio: é o único obrigatório corrigível | `@Schema(example = …)` nos records de resposta e em `SecretariatRequest`, mais um teste em `OpenApiContractIT` exigindo `example` nos schemas; opcional: `docs/api/openapi.json` exportado | P |
| 3 | Documentação desatualizada ou imprecisa **dentro da tag** | Baixo; afeta a leitura do avaliador | `README.md:314` ("a tag só é criada após o freeze GREEN") → release publicada; `README.md:309` F4 sem "GREEN"; `README.md:213` "em todas as listagens" → listagens de projetos, responsáveis e secretarias (prazos e agrupados não paginam); `README.md:286` "auditoria independente" → "análise própria + agente independente"; `AI_USAGE.md:82` "o desafio pede GitHub Actions" → "o desafio cita GitHub Actions como diferencial"; `CHANGELOG.md:39` "CI/CD" → "CI" | PP |
| 4 | Sem término previsto nas linhas 1, 11 e 12: 422 `BUSINESS_RULE_VIOLATION` em vez de `TRANSITION_BLOCKED` | Baixo | Decisão do F5-L2 (regra de dados). Documentar na tabela de interpretações do README, ou trocar o código do erro. | PP |
| 5 | Dicas estáticas por par origem/destino em `mismatchMessage` (`ProjectStatusTransition.java:173` e `:179`) | Baixo | As dicas mandam ajustar as duas datas mesmo quando só uma causa o bloqueio. Montar a dica a partir da data que de fato bloqueia, com teste por linha. | P |
| 6 | `PUT` muda datas sem passar pelas confirmações da tabela | Baixo; é interpretação | Uma linha na tabela de interpretações do README: a tabela governa `PATCH /status`; a edição recalcula. | PP |
| 7 | Lost update entre leitura e escrita | Baixo; já documentado | Nenhuma além do `@Version` existente | — |
| 8 | Código morto (`ProjectService.list`/`listByStatus`) e `LIKE` sem escapar `%`/`_` (`ProjectPersistenceAdapter.java:252`) | Muito baixo | Remover os métodos e seus testes; escapar os curingas com `ESCAPE '\'` e um IT | P |

**Esforço:** PP = minutos · P = até ~1 h.

**Efeito na nota:** só a lacuna 2 muda a nota: os obrigatórios iriam de 97,5% para **98,75%** (39,5/40), e o que sobraria seria o histórico. A lacuna 3 melhora a leitura, mas não muda a nota. Nenhuma das lacunas afeta o funcionamento da `v2.0.0` publicada.
