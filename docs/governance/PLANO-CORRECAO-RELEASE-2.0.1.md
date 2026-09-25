# Plano de correção: patch release 2.0.1

- **Data:** 2026-09-25 (7h, horário de Recife). O prazo da entrega é hoje, 25/09/2026.
- **Governança:** `PROMPT-EXECUTIVO-BASE-v1.1`, `PROMPT-EXECUTIVO-KANBAN-v1.0`, `PLANO-CONFORMIDADE-F5` e o roteiro de release de `docs/evidence/F5/RELEASE.md`, adaptado para hotfix.
- **Origem:** `ADERENCIA-V2.0.0.md`, auditoria da tag `v2.0.0`: snapshot `941276…2177`, `main` @ `3dac1ea`, com duas análises.
- **Objetivo:** levar os obrigatórios de 97,5% para **98,75% (39,5/40)**.
  - O item #40 (Swagger com exemplos) passa de Parcial para Atende.
  - O item #9 (histórico) fica como está e declarado: é o único Parcial, por decisão do usuário.
  - Os diferenciais continuam em 96% (12,5/13). Só são tocados onde o texto está errado.
- **Entrega mínima garantida:** a `v2.0.0` publicada continua válida até a tag `v2.0.1` existir.

## 1. Princípios

- **Três lotes curtos**, com o ritual de sempre:
  - pacote `.tar.gz` + SHA-256;
  - `VERIFICACAO-USUARIO.md` com o gate;
  - GREEN antes do commit;
  - Conventional Commits;
  - evidências em `docs/evidence/F5-P<n>/`.
- **Gitflow de hotfix.**
  - `hotfix/2.0.1` nasce da `main` (`v2.0.0`).
  - Cada lote nasce de `hotfix/2.0.1` numa branch `bugfix/2.0.1-p<n>-<tema>` e volta com `merge --no-ff`, com push nos dois remotos.
  - No fim:
    1. merge `--no-ff` na `main`;
    2. tag anotada `v2.0.1`;
    3. back-merge `--no-ff` na `develop`.
  - A `develop` tem 2 commits só de documentação depois da release (`4fd706f` e `4697211`), em arquivos que o hotfix não toca. `[INFERÊNCIA]` Sem conflito esperado; o gate confere.
- **Os irreversíveis ficam com o usuário:** merges na `main`/`develop`, a tag e os pushes. Nada de force-push.
- **Não entram:**
  - frontend e migrations;
  - job novo no CI;
  - GraphQL: o item #40 é só Swagger;
  - dependência nova.
- **Fora de escopo, porque não mudam a nota e só adicionam risco no dia do prazo:**
  - dicas estáticas de `mismatchMessage`;
  - código morto (`ProjectService.list`/`listByStatus`);
  - lost update (já documentado).

## 2. Lotes

### F5-P1 — Swagger com exemplos completos (#40)

**RED** `[VERIFICADO: auditV2]`:
- Das 26 operações REST, só os creates de Projeto e Responsável têm `@ExampleObject` de request/resposta.
- `@Schema(example)` existe só em `ProjectRequest`, `ProjectStatusRequest` e `ResponsibleRequest`.
- Nenhum dos 13 `@RequestParam` tem `@Parameter(example)`.
- Não têm exemplo:
  - `SecretariatRequest`, `ResponsibleCredentialsRequest`;
  - as 7 respostas: `ProjectResponse`, `ResponsibleResponse`, `SecretariatResponse`, `PageResponse`, `ProjectIndicatorsResponse`, `ProjectGroupIndicatorResponse` e `ProjectDeadlinesResponse`;
  - os records de `AuthRestController` (`LoginRequest`, `CsrfResponse`, `AuthResponse`);
  - `HealthStatus`.

| Entrega | Arquivo | Detalhe |
|---|---|---|
| Versão 2.0.1 | `backend/pom.xml`, `backend/Dockerfile` (`kanban-2.0.1.jar`), `frontend/package.json` | Commit próprio: `build(hotfix): define a versão 2.0.1`. |
| Exemplos nos schemas | Todos os records de request/resposta listados acima, em `backend/src/main/java/br/com/facilit/kanban/delivery/rest/` e `delivery/auth/AuthRestController.java`; `HealthStatus` na camada `application` | `@Schema(example = …)` em cada campo escalar, com os mesmos dados fictícios dos exemplos já existentes (UUIDs `2000…`/`3000…`, "Maria Silva", "Implantação do portal"). A senha usa um valor ilustrativo e continua `WRITE_ONLY`. O Swagger monta o exemplo de toda operação a partir do schema, então listas e páginas herdam o exemplo dos itens. |
| Exemplos nos parâmetros | `ProjectRestController`, `ResponsibleRestController`, `SecretariatRestController`, `ProjectIndicatorsRestController` | `@Parameter(example = …)` nos 13 `@RequestParam` e nos `{id}`. |
| Teste de contrato | `backend/src/test/java/br/com/facilit/kanban/integration/OpenApiContractIT.java` | Teste novo que percorre `/api-docs` e exige exemplo, direto ou via `$ref`, em três lugares: todo parâmetro, todo `requestBody` e toda resposta 2xx com corpo. Falha com a lista do que faltar. |
| Filtro de texto literal (decisão 1, §4) | `backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:252` + IT | `LIKE … ESCAPE '\'`; `%`, `_` e `\` do texto viram literais. |
| Evidência | `docs/evidence/F5-P1/` | Livro-razão, fontes, matriz, `EXECUCAO.md` e `VERIFICACAO-USUARIO.md`. |

- `[NÃO VERIFICADO]` Três pontos a confirmar no código antes de escrever:
  - se o springdoc 2.8.17 herda o `@Schema(example)` de componentes de record em `PageResponse<T>` genérico;
  - como o enum de status aparece no schema;
  - onde `HealthStatus` está.
- Se o genérico não herdar, o exemplo da página entra como `@ExampleObject` na operação de listagem. O teste pega qualquer falta.

**Gate `F5-P1`:**
- **Pré-condições:** branch `bugfix/2.0.1-p1-swagger` sobre `hotfix/2.0.1`; arquivos iguais aos do pacote; frontend só com a troca da versão.
- **Backend:** `mvn clean verify` com as contagens novas exatas (medidas no ensaio, não estimadas), JaCoCo ≥ 95% e nenhum `[deprecation]`.
- **Docker:** `docker compose up --build` do zero com `kanban-2.0.1.jar`, banco novo, migrations V1–V7 e health.
- **API real:** um script lê `/api-docs` e aplica a mesma regra do IT, independente do Java. Resultado esperado: 26 operações, 0 faltas.
- **Filtro, na API real:** crie dois projetos, "Meta 100%" e "Meta 1000". A busca `text=100%` devolve só o primeiro, e `text=_` não devolve nenhum dos dois.
- **Swagger UI, manual (1 min):** abrir `GET /api/v1/projects` e `GET /api/v1/indicators/projects/deadlines` e ver o exemplo da resposta. Passo marcado como executado pelo usuário.
- **Parte estática do freeze:** política do workflow e Conventional Commits.
- **CI:** push, com os 3 jobs verdes.

**Estimativa:** 60–90 min, com o CI.

### F5-P2 — Documentação coerente com a tag (#39 e textos errados dos diferenciais)

| Entrega | Arquivo | Detalhe |
|---|---|---|
| Estado da entrega | `README.md:309`, `:314` | O F4 passa a dizer "GREEN". O F5-L5 diz "release `v2.0.0` publicada", no lugar de "a tag só é criada após o freeze GREEN". Linha nova para o F5-P (`v2.0.1`). |
| Paginação | `README.md:213` | "em todas as listagens" → "nas listagens de projetos, responsáveis e secretarias". Indicadores agrupados e prazos são agregados e não paginam. |
| Origem da auditoria | `README.md:286` | "auditoria independente" → "análise própria confrontada com um agente independente". Cita também `ADERENCIA-V2.0.0.md` (ver §4). |
| Relatório de aderência (decisão 2, §4) | `docs/evidence/F5/ADERENCIA-V2.0.0.md` | Cópia fiel do relatório entregue em 24/09. |
| Interpretações | `README.md`, tabela de interpretações | Duas linhas. **(a)** A tabela de transições governa `PATCH /status`; o `PUT` edita datas e recalcula o status sem confirmação. **(b)** Sem término previsto, a transição responde 422 `BUSINESS_RULE_VIOLATION`, porque é regra de dado, não da tabela. |
| Diferencial com texto errado | `AI_USAGE.md:82` | "o desafio pede GitHub Actions" → "o desafio cita GitHub Actions como diferencial". |
| Diferencial com texto errado | `CHANGELOG.md:39` | "CI/CD" → "CI", porque não há deploy. |
| CHANGELOG | `CHANGELOG.md` | `[2.0.1] — 2026-09-25`: `Fixed` com os exemplos do Swagger, `Docs` com as correções acima. |
| Evidência | `docs/evidence/F5-P2/` | Idem. |

**Gate `F5-P2`:**
- Nenhuma das frases antigas presente, e as novas presentes.
- Links relativos resolvem.
- Texto exigido pelo gate de freeze presente em README, AI_USAGE e CHANGELOG.
- Nenhum arquivo de código alterado.
- Conventional Commits e CI verde.

**Estimativa:** 30–45 min.

### F5-P3 — Freeze, release `v2.0.1` e auditoria final

1. **Gate de freeze** (`docs/evidence/F5-P3/VERIFICACAO-USUARIO.md`): adaptação do freeze da 2.0.0 para a branch `hotfix/2.0.1` e a versão `2.0.1` em pom, Dockerfile e package.json. Cobre:
   - `mvn clean verify` completo;
   - `docker compose up --build` do zero;
   - newman da coleção (27 requisições, 0 falhas);
   - a regra de exemplos do P1 contra a API real;
   - CI.
2. **Roteiro** (`docs/evidence/F5-P3/RELEASE.md`):
   1. merge `--no-ff` de `hotfix/2.0.1` na `main`;
   2. tag anotada `v2.0.1`;
   3. back-merge `--no-ff` na `develop`;
   4. push no GitHub e depois no GitLab (espelho, não bloqueante).
3. **Verificação pública** (`VERIFICACAO-RELEASE.md`), com a correção do `curl | grep` já aplicada. Confere:
   - refs e tag no GitHub;
   - CI da `main` verde;
   - README da tag com o estado da release;
   - `develop` contendo a `main`.
4. **Auditoria final:** `git archive v2.0.1`, com a mesma dupla de análises. **Critério de sucesso: 39,5/40 obrigatórios, com o #9 como único Parcial.**
5. **Registro do relatório:** `docs/evidence/F5/ADERENCIA-V2.0.1.md` vai para a `develop` num commit `docs(F5): …`, como a `SAIDA-RELEASE`. A tag não muda.

**Estimativa:** 60–75 min, com a espera do CI.

## 3. Ordem e linha de corte (25/09, Recife)

| Ordem | Lote | Condição para seguir |
|---|---|---|
| 1 | F5-P1 | GREEN. É o único lote que muda a nota. |
| 2 | F5-P2 | GREEN |
| 3 | F5-P3 | `=== F5-P3 GREEN ===`, depois tag e verificação pública |

**Linha de corte:** se a tag `v2.0.1` não puder ser publicada com folga antes do prazo, o hotfix para onde estiver. A `v2.0.0` segue como entrega, e nada fica pela metade na `main`.

## 4. Decisões do usuário (2026-09-25, 7h50)

1. **Escapar `%` e `_` no filtro de texto, dentro do F5-P1.** **Aprovado.**
   - `ProjectPersistenceAdapter.java:252`: `LIKE` com `ESCAPE '\'`, escapando também a própria `\` do texto.
   - IT com `%` e `_` literais no texto, que não podem casar com outros projetos.
   - A branch continua `bugfix/2.0.1-p1-swagger`, e a correção vai num commit próprio: `fix(filtros): …`.
   - A entrada `Fixed` da `[2.0.1]` no CHANGELOG cita as duas correções.
   - Toca o diferencial D4 (filtros) só porque o comportamento estava errado.
2. **Versionar os relatórios de aderência em `docs/evidence/F5/`.** **Aprovado.**
   - `ADERENCIA-V2.0.0.md` entra no F5-P2.
   - O relatório final da 2.0.1, `ADERENCIA-V2.0.1.md`, entra no F5-P3.
