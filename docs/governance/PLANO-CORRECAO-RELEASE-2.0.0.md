# Plano de correção antes da release 2.0.0

- **Data:** 2026-09-24 (21h40, horário de Recife). O prazo da entrega é 25/09/2026.
- **Governança:** `PROMPT-EXECUTIVO-BASE-v1.1`, `PROMPT-EXECUTIVO-KANBAN-v1.0`, `PLANO-CONFORMIDADE-F5` e o roteiro `docs/evidence/F5/RELEASE.md`.
- **Origem:** `ADERENCIA-FINAL.md`. As lacunas vêm de duas análises independentes sobre o snapshot `43051359…abac17fd718f` (`release/2.0.0` @ `59051d0`).
- **Objetivo:** publicar a 2.0.0 com a aplicação subindo pelo README, com o Swagger utilizável e sem as lacunas baratas.

## 1. Princípios

- **Três lotes curtos, mais o freeze e a release que já estão preparados.** Cada lote segue o ritual de sempre: pacote `.tar.gz` + SHA-256, `VERIFICACAO-USUARIO.md` com gate, GREEN antes do commit, Conventional Commits e evidências em `docs/evidence/<LOTE>/`.
- **Gitflow de release.** Correção de release entra na própria `release/2.0.0`, não na `develop`.
  - Cada lote nasce de `release/2.0.0` numa branch `bugfix/2.0.0-<lote>`.
  - O lote volta para `release/2.0.0` com `merge --no-ff` e push nos dois remotos.
  - A `develop` recebe tudo no back-merge do fim da release (`docs/evidence/F5/RELEASE.md`).
- **Separação por risco.** O C1 corrige o que impede o avaliador de usar o sistema e tem que ficar GREEN de qualquer jeito. Mudança de contrato (C2) e documentação (C3) ficam em lotes separados, para uma falha não segurar a correção crítica.
- **Sem mudança no frontend, sem migration nova e sem job novo no CI.** O gate do freeze exige exatamente os jobs `frontend`, `backend` e `repository`, e as migrations V1 a V7.
- **O gate de freeze do F5-L5 continua valendo.** Cada lote roda aqui, no ensaio, a parte estática desse gate (política do workflow, Conventional Commits, conteúdo exigido de README e AI_USAGE) antes da entrega, para não quebrá-lo.

## 2. Lotes

### F5-C1 — Entrega executável (P0, obrigatório)

Fecha as lacunas 1 e 3 da auditoria e protege a lacuna 2.

| Entrega | Arquivo | Detalhe |
|---|---|---|
| Build da imagem sem o limite de cobertura | `backend/Dockerfile` (linha 6) | `RUN mvn -B -ntp -DskipITs -Djacoco.skip=true verify`. Os unitários continuam rodando na imagem, como na v1.0.0. O limite de 95% vale onde há IT: CI e gates. `kanban-2.0.0.jar` não muda (pré-condição do freeze). |
| O CI passa a construir a imagem | `.github/workflows/ci.yml` | passo `docker build` no job `backend` já existente, com `run:`, sem action nova. É o que teria pego o defeito. |
| Swagger UI envia o CSRF | `backend/src/main/resources/application.yml` | `springdoc.swagger-ui.csrf.enabled: true`. `[VERIFICADO: springdoc 2.8.17, SwaggerUiConfigProperties.Csrf]` a propriedade existe e tem nome de cookie e de cabeçalho próprios. `[VERIFICADO: SecurityConfiguration.java:51-52]` o cookie `XSRF-TOKEN` é legível por JS (`withHttpOnlyFalse`, path `/`). `[NÃO VERIFICADO]` que os padrões do springdoc sejam `XSRF-TOKEN`/`X-XSRF-TOKEN`; confiro no código antes de escrever. |
| Roteiro do Swagger | `README.md` | Seção "Como rodar": no Swagger, chamar `GET /api/v1/auth/csrf`, depois `POST /api/v1/auth/login`, e seguir. |
| Teste | `backend/src/test/java/br/com/facilit/kanban/integration/OpenApiContractIT.java` | `/api-docs/swagger-config` com o CSRF ligado. `[NÃO VERIFICADO]` o caminho exato da configuração; confiro na execução. |

**Gate `F5-C1`:**
- **Pré-condições:** branch `bugfix/2.0.0-*` sobre `release/2.0.0` e arquivos iguais aos do pacote.
- **Backend:** `mvn clean verify` com JaCoCo ≥ 95%.
- **Docker:** `docker compose build --no-cache backend` a partir do zero (a prova da lacuna 1), depois `up` com banco novo, migrations 1 a 7 e health.
- **Swagger, no script:** CSRF ligado na configuração do Swagger, e o fluxo CSRF → login → `GET /api/v1/projects` com o cabeçalho que o Swagger envia.
- **Swagger, manual (1 minuto):** fazer login pelo próprio Swagger UI, em um passo marcado como executado pelo usuário.
- **Parte estática do freeze:** actionlint e política do workflow.
- **CI:** push, com os 3 jobs verdes e o passo `docker build`.

**Estimativa:** 45–60 min, incluindo o CI.

### F5-C2 — Rigor de testes e validação (P1)

Fecha as lacunas 4 e 5 (itens 29 e 32).

| Entrega | Arquivo | Detalhe |
|---|---|---|
| Métricas linha a linha | `backend/src/test/resources/features/transicoes.feature` e passos em `backend/src/test/java/br/com/facilit/kanban/bdd/` | Colunas `atraso` e `percentual` nos `Exemplos`, com um passo que confere `delayDays` e `remainingTimePercentage` nas linhas de sucesso (nas bloqueadas, confere que nada mudou). Os valores esperados saem do cenário de datas de cada linha. |
| Limite de tamanho nas entradas | DTOs REST em `backend/src/main/java/br/com/facilit/kanban/delivery/rest/` (`ProjectRequest`, `ResponsibleRequest`, `SecretariatRequest`, filtro de texto) e os inputs GraphQL equivalentes em `delivery/graphql/` | `@Size`: nome e cargo ≤ 200; e-mail ≤ 254; texto do filtro ≤ 100; `responsibleIds` entre 1 e 50 (aprovado, ver §4). Resposta 400 `VALIDATION_ERROR` em pt-BR. Sem migration: o limite fica na borda, e o banco continua `TEXT`. `[NÃO VERIFICADO]` como o GraphQL valida os inputs hoje; leio `delivery/graphql` antes de decidir onde a anotação entra. |
| Testes | `ProjectRestControllerTest`, `ResponsibleRestControllerTest`, `SecretariatRestControllerTest` e um teste GraphQL | Tamanho excedido → 400 com o campo na lista `violations`. |

**Gate `F5-C2`:**
- **Backend:** `mvn clean verify` com as contagens novas exatas, BDD com 12 cenários, JaCoCo ≥ 95% e nenhum `[deprecation]`.
- **API real, via Docker:** nome com 201 caracteres → 400; e-mail com 255 → 400; um caso no limite → 201.
- **Parte estática do freeze.**

**Estimativa:** 60–90 min.

**Linha de corte:** se o C2 não ficar GREEN até 25/09 às 12h, ele sai da release. As duas lacunas vão para "Limitações" no README, e a nota fica como hoje (95%).

### F5-C3 — Documentação de entrega (P1)

Fecha as lacunas 6 a 9 e as inconsistências entre documentação e código.

| Entrega | Arquivo | Detalhe |
|---|---|---|
| Coleção | `docs/api/facilit-kanban.postman_collection.json` | `by-secretariat`, `by-responsible`, `deadlines?withinDays=7` e `withinDays=0` → 400 |
| AI_USAGE | `AI_USAGE.md` | Seção F5, de forma honesta. A auditoria independente de 23/09 achou o status congelado da v1.0.0. O plano F5 veio da análise, com a decisão de SemVer 2.0.0 tomada por você. Inclui um trecho do plano como exemplo de spec. |
| README | `README.md` | Tira o texto de trabalho (L5 pendente, plano em andamento). Cita o ADR 0002 na estrutura de pastas. Corrige "GraphQL espelha o REST" (as páginas GraphQL não têm `totalElements`). Declara em uma linha que o histórico de F2-L1 a F3-L4 foi reconstruído por lote. Atualiza as Limitações. |
| CHANGELOG | `CHANGELOG.md` | Entradas `Fixed` do C1 e do C2 na `[2.0.0]`. A data continua 2026-09-25, a data em que a tag vai ser criada. |
| Auditoria | `docs/evidence/F5/AUDITORIA.md` | Atualizada com o resultado final e as duas análises. `ADERENCIA-FINAL.md` versionado em `docs/evidence/F5/` (aprovado, ver §4). |
| Roteiro da release | `docs/evidence/F5/GATE.md` e `RELEASE.md` | A condição "a release contém somente versionamento/documentação" passa a aceitar os lotes C1 a C3, listados. |

**Gate `F5-C3`:**
- JSON da coleção válido, com as 3 requisições novas.
- Links relativos resolvem.
- Nenhuma referência a "pendente" ou a lote em andamento no README.
- Texto exigido pelo gate de freeze presente em README, AI_USAGE e AUDITORIA (rodo essa parte do gate no ensaio).
- Conventional Commits.

**Estimativa:** 30–45 min.

### F5-L5 — Freeze e release (já preparado)

Execução do que já está versionado, sem mudança de escopo:

1. `docs/evidence/F5/VERIFICACAO-USUARIO.md`: freeze completo, com Docker `up --build` do zero. Agora ele exercita a correção do C1.
2. `docs/evidence/F5/RELEASE.md`:
   1. merge `--no-ff` na `main`;
   2. tag anotada `v2.0.0`;
   3. back-merge na `develop`;
   4. push nos dois remotos;
   5. `VERIFICACAO-RELEASE.md`, com CI da `main` e página pública.

**Estimativa:** 45–60 min, com a espera do CI.

## 3. Ordem e cronograma

| Quando (25/09, Recife) | Lote | Condição para seguir |
|---|---|---|
| até 10h | F5-C1 | GREEN obrigatório; sem ele não há release |
| até 12h | F5-C2 | se não ficar GREEN, sai da release (linha de corte) |
| até 13h | F5-C3 | GREEN |
| até 15h | F5-L5, freeze | `=== F5-L5 GREEN ===` |
| até 16h | Release: `main`, tag `v2.0.0`, `develop` | CI da `main` verde e repositório público conferido |

Se o freeze falhar, a correção entra num lote de revisão (`-rev2`) do próprio freeze. A `v1.0.0` continua sendo a entrega mínima até a tag `v2.0.0` existir.

## 4. Decisões do usuário (2026-09-24 21h42)

1. **Imagem do backend:** manter os testes unitários no build da imagem: `RUN mvn -B -ntp -DskipITs -Djacoco.skip=true verify`. **Aprovado.**
2. **Limites de tamanho do C2:** nome e cargo 200, e-mail 254, texto do filtro 100, `responsibleIds` de 1 a 50. **Aprovado.**
3. **`ADERENCIA-FINAL.md`:** versionado em `docs/evidence/F5/`, no lote F5-C3. **Aprovado.**
