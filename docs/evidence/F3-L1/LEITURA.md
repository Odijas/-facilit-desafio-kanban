# F3-L1 — LIVRO-RAZÃO DE LEITURA

Data: 2026-09-22

## Integral

- `docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md`
- `docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md`
- `README.md`
- `backend/pom.xml`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/migration/V1__create_kanban_core.sql`
- `backend/src/main/resources/db/migration/V3__optimize_api_list_indexes.sql`
- `backend/src/main/resources/graphql/kanban.graphqls`
- domínio, portas, serviços, adapters JPA e controllers diretamente afetados de Projeto, Responsável e Secretaria;
- `ApplicationBeans.java` e tratamento de erros existente;
- repositórios in-memory, testes de serviço e `KanbanApiIT.java` afetados;
- `frontend/package.json`, `biome.json`, `tsconfig*.json`, `vite.config.ts`;
- `frontend/src/api/kanban.ts` e seus testes;
- `features/dashboard`, `features/kanban`, `features/indicators`, `features/secretariats` e testes diretamente afetados;
- evidências F2-L3 e seu gate final.

## Consumidores

Mapeados integralmente por busca textual após as alterações. Saída preservada em `CONSUMIDORES.md`.

## Não abertos

Arquivos não envolvidos no comportamento F3-L1 e já cobertos pelo baseline GREEN permaneceram fora do escopo de mudança. Nenhum consumidor encontrado pelas buscas registradas ficou sem leitura deliberadamente.

## Rev2 — 2026-09-22

Integral: `KanbanBoard.tsx`, `KanbanBoard.test.tsx`, `ProjectFiltersBar.tsx`, `SecretariatPanel.tsx`, `SecretariatPanel.test.tsx`, `VERIFICACAO-USUARIO.md`, `V4__create_security_users.sql`, `kanban.graphqls`, `application.yml`, `ApiErrorCode.java`, documentos de governança, desafio (PDF) e demais evidências F3-L1.

Por diff integral: arquivos divergentes entre candidate e snapshot local (listados em EXECUCAO.md).

Tipos instalados: `@tanstack/query-core@5.102.8/build/modern/hydration-Bjs0MSgg.d.ts` (keepPreviousData, QueryObserverPlaceholderResult) e `@tanstack/react-query/build/modern/index.d.ts` (reexport de query-core).

Não abertos neste rev2: arquivos backend de infraestrutura/entrega não alterados pelo rev2 (conteúdo idêntico ao candidate).

## Rev3 — 2026-09-22

Integral: `ProjectJpaRepository.java`, `ProjectPersistenceAdapter.java`, `ProjectFilter.java`, `PageQuery.java`, `RestExceptionHandler.java`, `SecurityConfiguration.java`.

Amostrado: `ProjectJpaEntity.java` [AMOSTRAGEM: linhas 1–80, mapeamentos], `ResponsibleJpaEntity.java` [AMOSTRAGEM: campos], `ProjectService.java` [AMOSTRAGEM: linhas 60–90], `ProjectRestController.java` [AMOSTRAGEM: linhas 105–135], `ProjectGraphQlController.java` [AMOSTRAGEM: linhas 38–66], `KanbanApiIT.java` [AMOSTRAGEM: linhas 140–156 e 170–289], `PageResponse.java` [AMOSTRAGEM: campo totalElements].

Fontes externas: documentação oficial Spring Data JPA 3.5 (Specifications) e fonte `SimpleJpaRepository.java` na tag 3.5.13.

## Rev4 — 2026-09-22

Integral: `health.graphqls`, `kanban.graphqls`, `VERIFICACAO-USUARIO.md`. Amostrado: `SecretariatGraphQlController.java` [AMOSTRAGEM: linhas 12–56], `SecretariatService.java` [AMOSTRAGEM: linhas 55–70], métodos DELETE de `ProjectRestController`, `ResponsibleRestController` e `SecretariatRestController` (204), mapeamentos REST de secretarias e indicadores.
