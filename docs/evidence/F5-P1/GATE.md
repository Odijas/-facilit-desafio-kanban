# F5-P1 — GATE

Data: 2026-09-25

Estado: **CANDIDATE**, aguardando o gate local.

Escopo: lote F5-P1 de `docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md`, na `hotfix/2.0.1`. Cobre o Swagger com exemplos completos (#40), o filtro de texto literal (decisão 1) e a versão 2.0.1.

Critérios:

- `F5_P1_PRECONDITIONS_GREEN`:
  - branch `bugfix/2.0.1-*` cujo commit base tem a mesma árvore da tag `v2.0.0`;
  - arquivos alterados iguais à lista do pacote (33);
  - no frontend, só o `package.json`.
- `F5_P1_BACKEND_GREEN`:
  - `mvn clean verify` sem falha, sem `[deprecation]` e sem autoanexo do Mockito;
  - `kanban-2.0.1.jar` gerado;
  - unitários/BDD: 25 relatórios e 174 testes; integração: 12 relatórios e 51 testes; sem falha, erro ou teste ignorado;
  - `OpenApiContractIT` com 5 testes e `ProjectPersistenceAdapterIT` com 6;
  - BDD com 12 cenários e 48 passos aprovados;
  - JaCoCo ≥ 95%.
- `F5_P1_STATIC_GREEN`:
  - espaços e linha final nos arquivos do lote e `git diff --check`;
  - workflow, README, CHANGELOG, AI_USAGE e `resources` iguais aos da v2.0.0;
  - versão 2.0.1 nos três arquivos, e o `package.json` só com essa linha mudada;
  - `LIKE` com `ESCAPE`;
  - teste de exemplos presente;
  - camada de aplicação sem `io.swagger`;
  - plano com as duas decisões aprovadas.
- `F5_P1_DOCKER_GREEN`: `up --build` com banco novo e imagem `kanban-2.0.1.jar`, migrations 1 a 7.
- `F5_P1_API_GREEN` (17 verificações):
  - `/api-docs` com 26 operações e exemplo em todo parâmetro, corpo e resposta de sucesso;
  - `GET /auth/csrf` sem parâmetro;
  - `maxItems`/`maxLength` do F5-C2 mantidos;
  - Swagger UI ainda envia o CSRF;
  - buscas `100%`, `%`, `_`, `lote_a` e `meta` (REST) e `_` (GraphQL) com os nomes exatos esperados.
- Marcador final `=== F5-P1 GREEN ===` e `Resultado: exit code 0`.
- **Manual (seção 3):** exemplos visíveis no Swagger UI, marcado como executado pelo usuário.
- **Depois do push:** CI da `hotfix/2.0.1` conferido no gate do F5-P2 (`F5_P1_CI_GREEN`).
