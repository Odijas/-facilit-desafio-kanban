# Plano de correção: patch release 2.0.2

- **Data:** 2026-09-25 (9h45, horário de Recife). O prazo da entrega é hoje.
- **Governança:** `PROMPT-EXECUTIVO-BASE-v1.1`, `PROMPT-EXECUTIVO-KANBAN-v1.0`, `PLANO-CONFORMIDADE-F5` e o Gitflow de hotfix usado na 2.0.1 (`docs/evidence/F5-P3/RELEASE.md`).
- **Origem:** `ADERENCIA-V2.0.1.md`, auditoria da tag `v2.0.1` (snapshot `6b237833…c868f`), com duas análises.
- **Objetivo:**
  - corrigir **todos os erros** encontrados (a entrega afirma ou mostra algo que não é verdade);
  - remover o **código morto** de listagem de projetos, débito técnico incluído a pedido do usuário (2026-09-25, 9h47);
  - sem mudar regra de negócio nem contrato.
  - As demais lacunas (histórico, TDD, métricas por linha na API e CD) ficam fora, porque não são erros.
- **Nota esperada:** continua 98,75% nos obrigatórios e 96% nos diferenciais. A correção tira as ressalvas dos itens #39 (README) e #40 (Swagger).
- **Entrega mínima garantida:** a `v2.0.1` publicada continua válida até a tag `v2.0.2` existir.

## 1. Erros a corrigir

| # | Erro | Evidência | Correção |
|---|---|---|---|
| E1 | O README cita 4 testes com nome errado. Todos os outros 17 nomes de teste citados existem (conferido por script). | `README.md:97-98`. Os nomes reais têm prefixo: `line05DoesNotLetStaleInProgressBypassTheInProgressToOverdueBlock`, `line02BlocksNotStartedToOverdueBeforePlannedStart`, `line02BlocksNotStartedToOverdueOnPlannedStartBecauseDatesStillClassifyAsNotStarted` e `line02TreatsStaleNotStartedAsOverdueOnceThePlannedStartHasPassed`. | Corrigir os 4 nomes. O gate passa a conferir **todos** os testes citados no README, para o erro não voltar. |
| E2 | A linha do `PUT` no README cita um teste que não prova a remoção de data realizada sem confirmação. | `README.md:101`. `createsReadsListsUpdatesAndDeletesProjectWithRecalculatedMetrics` só confere o recálculo no `update`. | Teste novo em `ProjectServiceTest`: um `update` que remove o início realizado e confere o recálculo, sem confirmação. O README passa a citá-lo. |
| E3 | Exemplos de erro do Swagger fora do contexto da operação. | `ApiErrorDocumentation.java`: o 404 diz "Projeto não encontrado" em qualquer `{id}`; o 400 de corpo aponta `name` em qualquer corpo; o 400 de consulta fala de tamanho de página até em `/deadlines`; o 403 fala de projetos em qualquer rota. | Exemplo por operação, com os textos que a API devolve de verdade. **404:** "Projeto / Responsável / Secretaria não encontrado(a)" pelo recurso do path, e credencial no `DELETE …/credentials`. **400 de corpo:** um campo obrigatório do schema daquele corpo. **400 de consulta:** `withinDays` em `/deadlines`, `size` nas listagens. **403:** texto do filtro ("Acesso negado.") nas rotas só de ADMIN, e o texto da regra do responsável nas escritas de projeto. |
| E4 | Dica de bloqueio imprime "null" quando faltam datas previstas. | `ProjectStatusTransition.mismatchMessage`, linha 2 (A iniciar → Atrasado); pelo mesmo motivo, também na linha 4. | A dica passa a pedir a data que falta ("informe o início previsto (plannedStart)…") em vez de imprimir `null`. |
| E5 | Na linha 11, a dica manda "manter o início realizado preenchido" quando ele está vazio. | Mesmo método, ramo Concluído → Em andamento. | A dica sai do status recalculado. Sem início realizado: pede para informar o `actualStart`. Com início realizado e término vencido: pede para ajustar o `plannedEnd`. |

Os textos exatos das mensagens reais (404 e 403) são conferidos no código antes de escrever.

## 1a. Débito técnico incluído: código morto

| # | Débito | Evidência (`v2.0.1`) | Solução |
|---|---|---|---|
| D1 | `ProjectService.list` e `ProjectService.listByStatus` não têm chamador no código de produção. REST e GraphQL listam projetos por `ProjectService.search`, com o filtro de status. | `grep` em `backend/src/main`: nenhuma chamada. Só testes chamam os métodos: `ProjectServiceTest`, `ProjectServiceMockitoTest`, `ProjectScheduleRefresherTest` e `ScheduleFreshnessIT`. | **1. Remover a cadeia inteira**, que só existe para esses dois métodos: `ProjectService.list` e `listByStatus`; `ProjectRepository.findAll(PageQuery)` e `findByStatus`; as implementações em `ProjectPersistenceAdapter`; `ProjectJpaRepository.findByStatus`; e os métodos equivalentes do dublê `InMemoryProjectRepository`. |
| | | | **2. Migrar os testes que usavam os métodos para `ProjectService.search`** (e `ProjectPersistenceAdapter.search` no IT de persistência), com o filtro de status. É o caminho que a API usa, então os testes passam a provar o caminho real: recálculo antes da leitura, paginação e ordenação. As contagens de testes não caem: nenhum teste é removido, só o método chamado muda. |
| | | | **3. Manter índices e migrations:** o índice de `status` continua servindo ao `search`. |
| | | | **4. Atualizar o ADR:** `docs/adr/0002-status-sempre-atual.md:31` cita `list` e `listByStatus` entre os pontos de recálculo e passa a citar só `get`, `search` e os indicadores. |

- `[VERIFICADO]` `ProjectService.search` faz o mesmo `refreshSchedules()` antes da leitura (`ProjectService.java:109-113`). A remoção não tira nenhuma garantia de "status sempre atual".

## 2. Lotes

Mesmo ritual da 2.0.1:
- pacote `.tar.gz` + SHA-256;
- `VERIFICACAO-USUARIO.md` com gate;
- GREEN antes do commit;
- Conventional Commits;
- `bugfix/2.0.2-*` → `merge --no-ff` na `hotfix/2.0.2`;
- nada de force-push.

Os irreversíveis ficam com você: merges, tag e pushes.

**Regra de fechamento de lote (decisão do usuário, 2026-09-25, 9h48).** Ao final de cada lote, depois do gate GREEN e **antes de iniciar o próximo**, o Gitflow semântico do lote é feito por inteiro:
1. commits em Conventional Commits, granulares por assunto, cada um compilando sozinho;
2. `git status` vazio;
3. `merge --no-ff` da `bugfix/2.0.2-*` na `hotfix/2.0.2`;
4. push da `hotfix/2.0.2` e da `bugfix/2.0.2-*` no GitHub (obrigatório) e no GitLab (espelho, não bloqueia).

O pacote do lote seguinte só é entregue depois da confirmação desse fechamento. O preparo e o gate do próximo lote recusam a base se o lote anterior não estiver commitado, mesclado e publicado:
- o commit da `hotfix/2.0.2` é igual ao do `origin`;
- as evidências e os arquivos do lote anterior estão presentes no commit base;
- a árvore está limpa.

O roteiro de fechamento vai no `VERIFICACAO-USUARIO.md` de cada lote, como seção própria, logo depois do gate.

### F5-Q1 — Código e testes (E2 a E5 e D1)

| Entrega | Arquivo | Teste |
|---|---|---|
| Versão 2.0.2 | `backend/pom.xml`, `backend/Dockerfile`, `frontend/package.json` | build com `kanban-2.0.2.jar` |
| Exemplos de erro por operação (E3) | `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ApiErrorDocumentation.java` | `OpenApiContractIT`, teste novo: o 404 cita o recurso do path; o 400 de corpo aponta um campo que existe no schema do corpo; o 400 de `/deadlines` cita `withinDays`; nenhum 403 de rota de secretaria ou responsável fala de projeto |
| Dicas sem `null` e linha 11 coerente (E4, E5) | `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java` | `ProjectStatusTransitionTest`: linha 2 sem datas previstas; linha 11 recalculando A iniciar e recalculando Atrasado; nenhuma mensagem de bloqueio contém "null" |
| Prova do `PUT` (E2) | `backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java` | teste novo de `update` removendo o início realizado |
| Código morto (D1) | `application/project/ProjectService.java`, `application/project/ProjectRepository.java`, `infrastructure/persistence/project/ProjectPersistenceAdapter.java` e `ProjectJpaRepository.java` (em `backend/src/main/java/br/com/facilit/kanban/`); nos testes, `InMemoryProjectRepository`, `ProjectServiceTest`, `ProjectServiceMockitoTest`, `ProjectScheduleRefresherTest`, `ScheduleFreshnessIT` e `ProjectPersistenceAdapterIT` | os mesmos testes, migrados para `search`; o gate confere que nenhum dos métodos removidos existe mais no código |
| Plano | `docs/governance/PLANO-CORRECAO-RELEASE-2.0.2.md` | — |

- **Gate:**
  - `mvn clean verify` com as contagens novas exatas;
  - Docker com banco novo;
  - na API real:
    - os exemplos de erro corretos no `/api-docs`;
    - a mensagem real do 422 da linha 2 sem datas previstas, sem "null";
    - um 404 real de secretaria com o mesmo texto do exemplo.
- **Fechamento (antes do F5-Q2):** Gitflow do F5-Q1 na `hotfix/2.0.2`.
  - Commits:
    - `build(hotfix): define a versão 2.0.2`;
    - `fix(api): …` (exemplos de erro por operação);
    - `fix(dominio): …` (dicas de bloqueio);
    - `refactor(projetos): remove a listagem de projetos sem uso …`;
    - `test(projetos): …` (prova do `PUT` e migração dos testes para `search`);
    - `docs(F5-Q1): …`.
  - Depois: merge `--no-ff` e push nos dois remotos.
- **Estimativa:** 75–105 min, com o D1.

### F5-Q2 — Documentação (E1, E2) e promoção do F5-Q1

- **ADR 0002:** a lista de pontos de recálculo sem `list` e `listByStatus` (D1).
- **README:**
  - os 4 nomes corrigidos;
  - a linha do `PUT` citando o teste novo;
  - a linha do F5-Q1.
- **CHANGELOG** `[2.0.2]`, com `Fixed` (E1 a E5) e `Removed` (D1: métodos de listagem sem uso).
- **AI_USAGE:** os erros pegos pela auditoria da `v2.0.1`: nomes de teste errados desde a F5-L1, a linha do `PUT` escrita no F5-P2 e os exemplos de erro genéricos.
- **Evidências:** promoção do F5-Q1 e `docs/evidence/F5/ADERENCIA-V2.0.1.md` versionado (ver §4).
- **Gate:**
  - todo teste citado no README e no AI_USAGE existe no código;
  - frases e nomes antigos ausentes;
  - links resolvidos;
  - Conventional Commits;
  - CI da `hotfix/2.0.2` com o F5-Q1.
- **Fechamento (antes do F5-Q3):** Gitflow do F5-Q2 na `hotfix/2.0.2`.
  - Commits: `docs(readme)`, `docs(adr)`, `docs(changelog)`, `docs(ai-usage)`, `docs(F5)` e `docs(F5-Q2)`.
  - Depois: merge `--no-ff` e push nos dois remotos.
- **Estimativa:** 30 min.

### F5-Q3 — Freeze, release `v2.0.2` e auditoria final

- Mesmo roteiro do F5-P3:
  - freeze na `hotfix/2.0.2` (o da 2.0.1 com versão, contagens e as regras novas de E1 e E3);
  - registro da saída;
  - merge `--no-ff` na `main` e tag anotada `v2.0.2`;
  - back-merge na `develop`;
  - verificação pública e registro na `develop`.
- **Fechamento do lote e da release:**
  - commits `docs(F5-Q2)` (promoção) e `docs(F5-Q3)` (preparo) na `hotfix/2.0.2`;
  - registro da saída do freeze;
  - merge `--no-ff` na `main`, tag anotada `v2.0.2` e back-merge na `develop`;
  - push de `main`, `develop`, `hotfix/2.0.2` e `v2.0.2` nos dois remotos;
  - registro da verificação pública na `develop`.
- `git archive v2.0.2` para a auditoria final, com as mesmas duas análises. **Critério:** nenhum dos erros E1 a E5 presente, e a mesma nota.
- **Estimativa:** 45–60 min, com o CI.

## 3. Linha de corte

Se a tag `v2.0.2` não puder sair com folga antes do prazo, o hotfix para onde estiver. A `v2.0.1` segue como entrega, e nada fica pela metade na `main`.

## 4. Decisão do usuário (sim/não)

1. **`ADERENCIA-V2.0.1.md` entra no F5-Q2 (na `hotfix/2.0.2`), e não num commit avulso na `develop` agora.** Recomendo sim: fica na tag `v2.0.2` e chega à `develop` pelo back-merge. Se você já o commitou na `develop`, o F5-Q2 não o inclui, para não haver dois commits do mesmo arquivo.
