# Aderência ao desafio: Facilit Kanban `v2.0.1`

- **Data:** 2026-09-25.
- **Snapshot:** `facilit-desafio-kanban-v2.0.1-auditoria.tar.gz` (`git archive v2.0.1`), com SHA-256 `6b237833d82cc050e3c05674d0fa9d4561baa42e7c2eaf96c90fb74869ac868f`, 475 arquivos e nenhum indevido (sem `.env`, chaves, `node_modules` ou `target`).
- **Estado publicado:**
  - tag anotada `v2.0.1` = `main` @ `83850ff`, merge da `hotfix/2.0.1` @ `b2eee6d`;
  - `develop` @ `b2653dd` contém a hotfix;
  - freeze GREEN (`docs/evidence/F5-P3/SAIDA-GATE.txt`);
  - verificação pública GREEN, com o CI da `main` na run 36133984092.
- **Conteúdo da tag:** confere com os pacotes F5-P1 rev2, F5-P2 e F5-P3 mais o registro do freeze. A diferença para a `v2.0.0` são 63 arquivos: 27 alterados e 36 novos (código, testes, plano e evidências dos lotes F5-P).
- **Fonte dos requisitos:** o PDF "Desafio Técnico Backend — Kanban", lido inteiro.
- **Método:** duas análises sobre o mesmo pacote.
  1. **Agente independente.** Não leu os relatórios de aderência anteriores, as matrizes, a auditoria nem os planos. Ele:
     - compilou as classes reais do domínio com javac e rodou um harness sobre 338 combinações de datas e 2.560 transições;
     - comparou o resultado com uma reimplementação feita só a partir do texto do PDF: **0 divergências** em dias de atraso e % restante; nas zonas que o PDF não define, as escolhas do código são as declaradas no README;
     - contou os testes no código (174 unitários/BDD e 51 de integração, iguais ao freeze) e as 26 operações REST.
  2. **Esta análise.** Conferiu no código cada afirmação nova do agente e classificou na mesma numeração de 40 obrigatórios e 13 diferenciais usada desde 23/09.
- **Pontuação:** Atende = 1, Parcial = 0,5, Não atende = 0.
- **Nada foi aplicado.** Este relatório não altera o repositório.

## Resultado

| Grupo | 23/09 (`v1.0.0`) | RC (24/09) | `v2.0.0` | **`v2.0.1` (esta análise)** | Agente independente |
|---|---|---|---|---|---|
| Obrigatórios | 85% (34/40) | 95% (38/40) | 97,5% (39/40) | **98,75% (39,5/40)** | 99,3% (71,5/72, outra granularidade) |
| Diferenciais e Etapa 3 | 81% (10,5/13) | 96% (12,5/13) | 96% (12,5/13) | **96% (12,5/13)** | 97,5% (19,5/20) |

**A meta do plano 2.0.1 foi atingida:** 39,5/40, com o #9 (histórico) como único Parcial, mantido e declarado por decisão do usuário. As duas análises chegam ao mesmo único Parcial nos obrigatórios e ao mesmo único Parcial nos diferenciais (TDD).

## Obrigatórios

Só as linhas que mudaram desde a `v2.0.0`, ou que ganharam ressalva nova, trazem detalhe. As demais continuam Atende, com a evidência de `ADERENCIA-V2.0.0.md`.

| # | Requisito (PDF) | v2.0.0 | **v2.0.1** | Evidência / motivo |
|---|---|---|---|---|
| 1–8 | Java, JUnit, Git, Docker, REST, repositório público, README, AI_USAGE, coleção, diagramas | A | A | Freeze F5-P3 GREEN; repositório público com as tags v1.0.0, v2.0.0 e v2.0.1 |
| 9 | Histórico de commits granular | P | **P** | F2-L1 a F3-L4 reconstruídos por lote, e o README declara isso. Do F4 em diante, commits granulares; 86 em Conventional Commits no freeze. Decisão do usuário: não reescrever histórico publicado. |
| 10–38 | Campos, status, 12 transições, métricas, CRUD, testes, erros, validação, logs, paginação, integração, API | A | A | Oráculo do agente sem divergência; 174 unitários/BDD e 51 de integração, 0 falhas; JaCoCo 95,89% |
| 39 | README coerente com o código | A | A | Frases desatualizadas da `v2.0.0` removidas (F5-P2). **Ressalva nova** (lacuna 2): na tabela de interpretações, 4 nomes de teste estão sem o prefixo `lineNN`, e a linha do `PUT` cita um teste que confere o recálculo, mas não a remoção de data sem confirmação. |
| 40 | Swagger com exemplos de requests/responses, schemas e mensagens de erro | P | **A** | Exemplo em todo parâmetro, corpo e resposta de sucesso das 26 operações. `OpenApiContractIT` exige isso, e o freeze aplicou a mesma regra ao `/api-docs` real. Erros com exemplo em todas as operações. **Ressalva** (lacuna 3): alguns exemplos de erro são genéricos e não correspondem ao recurso. |

**Contagem:** 39 Atende + 1 Parcial (#9) = **39,5/40 = 98,75%**.

## Diferenciais e Etapa 3

| # | Item | v2.0.0 | **v2.0.1** | Evidência / motivo |
|---|---|---|---|---|
| D1–D3 | IA (ADR), UI com drag-and-drop, Secretaria | A | A | — |
| D4 | Filtros avançados | A | A | Busca por texto passa a tratar `%`, `_` e `\` como caracteres comuns (F5-P1). `ProjectPersistenceAdapterIT` e a busca `text=_` no freeze. |
| D5–D6 | GraphQL, observabilidade e logs estruturados | A | A | Freeze: Prometheus, Grafana e 65 registros ECS |
| D7 | CI/CD (GitHub Actions com build, testes e análise) | A | A | 3 jobs e `docker build`. Sem CD e sem SAST. O parêntese do PDF define o item; as duas análises dão Atende. |
| D8 | BDD e TDD | P | **P** | BDD em Cucumber pt-BR ✓; TDD declarado, sem como verificar pelo histórico |
| D9 | Autenticação do responsável | A | A | Freeze: 13 verificações de acesso legítimo e de ataque |
| E3a–E3d | Etapa 3 | A | A | — |

**Contagem:** 12 Atende + 1 Parcial = **12,5/13 = 96%**.

## Achados do agente e como foram resolvidos

| Achado | Conferência nesta análise | Resolução |
|---|---|---|
| README cita 4 testes que não existem com esse nome | **Confirmado.** Os nomes reais têm prefixo: `line02BlocksNotStartedToOverdueBeforePlannedStart`, `line02BlocksNotStartedToOverdueOnPlannedStart…`, `line02TreatsStaleNotStartedAsOverdue…` e `line05DoesNotLetStaleInProgressBypass…`. Já estava assim na `v2.0.0`; os gates conferiram só os testes das linhas novas. | Lacuna 2 |
| A linha do `PUT` cita um teste que não remove data realizada | **Confirmado.** `createsReadsListsUpdatesAndDeletesProjectWithRecalculatedMetrics` prova o recálculo no `update`, mas não a remoção de data sem confirmação. Esta linha foi escrita no F5-P2. | Lacuna 2 |
| Exemplos de erro genéricos no Swagger | **Confirmado** em `ApiErrorDocumentation.java`: o 404 diz "Projeto não encontrado" em qualquer `{id}`; o 400 de corpo aponta o campo `name` em qualquer corpo; o 400 de query fala de tamanho de página; o 403 fala de projetos. | Lacuna 3 (não muda a nota: as mensagens existem, com código e formato reais) |
| Dicas com `null` e "mantenha o início realizado" com o campo vazio | Consistente com a leitura de `ProjectStatusTransition.mismatchMessage` (dicas por par origem/destino, já registradas na auditoria da `v2.0.0`) | Lacuna 4 |
| Métricas não conferidas na API por linha | Consistente: o BDD confere as métricas nas 12 linhas no domínio, e a API IT só confere `delayDays` na linha 12 | Lacuna 5 |
| README chama o item de "Diferencial — CI/CD" | **Não é inconsistência:** é o nome do item no PDF. O CHANGELOG corrigiu a descrição da 1.0.0, que afirmava CD. | Sem ação |
| "Não há credencial padrão", e o `.env.example` traz `POSTGRES_PASSWORD=change-me-local-only` | Confirmado, mas é marcador de preenchimento, recusado pelo gate de segredos só fora do `.env.example`. A senha do admin não tem padrão. | Menor; ajuste de texto opcional |
| `SAIDA-RELEASE.txt` citado no README e ausente na tag | É intencional e está declarado na mesma linha ("na `develop`"): a prova pós-release não move a tag | Sem ação |

## Lacunas remanescentes (nada foi aplicado)

Nenhuma muda a nota dos obrigatórios, exceto a 1. Uma correção agora exigiria uma nova patch release (2.0.2), com freeze e tag.

| # | Lacuna | Impacto | Menor correção | Esforço |
|---|---|---|---|---|
| 1 | Histórico F2-L1 a F3-L4 reconstruído (#9, D8) | Baixo; declarado | Nenhuma no histórico, por decisão do usuário. Opcional: versionar a saída de `git log --stat` mapeada por lote, para o avaliador conferir sem `.git`. | — / PP |
| 2 | README: 4 nomes de teste sem o prefixo `lineNN`; linha do `PUT` sem teste que prove a remoção de data | Baixo | Corrigir os 4 nomes. Acrescentar em `ProjectServiceTest` um caso de `update` que remove o início realizado e confere o recálculo sem confirmação. | PP |
| 3 | Exemplos de erro genéricos no Swagger | Baixo | Em `ApiErrorDocumentation.document`, escolher o texto do 404, o campo do 400 e o texto do 403 pelo path e pelo schema do corpo | P |
| 4 | Dicas de bloqueio com datas nulas e texto fixo por par | Baixo | Montar a dica a partir das datas que de fato bloqueiam, tratando nulos | P |
| 5 | Métricas não conferidas na API por linha | Muito baixo | Asserts de `delayDays` e `remainingTimePercentage` nas linhas de sucesso de `StatusTransitionApiIT` | PP |
| 6 | CD e SAST | Muito baixo (as duas análises dão Atende) | Job disparado por tag publicando a imagem no GHCR | P |
| 7 | Código morto (`ProjectService.list`/`listByStatus`); lost update entre leitura e escrita (documentado) | Muito baixo | Remover os métodos e seus testes | PP |

**Esforço:** PP = minutos · P = até ~1 h.
