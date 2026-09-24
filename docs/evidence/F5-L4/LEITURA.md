# F5-L4 — LIVRO-RAZÃO DE LEITURA

Data: 2026-09-24
Base: `f786e7ce058d16a1f7ad4b3968c09aa9e8bc67d7`

## Lidos integralmente antes da alteração

- `backend/pom.xml`
- `.github/workflows/ci.yml`
- `backend/src/main/java/br/com/facilit/kanban/application/project/ProjectIndicators.java`
- `backend/src/main/java/br/com/facilit/kanban/application/project/ProjectStatusIndicator.java`
- `backend/src/main/java/br/com/facilit/kanban/application/project/ProjectRepository.java`
- `backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java`
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsResponse.java`
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsRestController.java`
- `backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java`
- `backend/src/main/resources/graphql/kanban.graphqls`
- `backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaRepository.java`
- `backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java`
- `backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaEntity.java`
- `backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/responsible/ResponsibleJpaEntity.java`
- `backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java`
- `backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceMockitoTest.java`
- `backend/src/test/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsRestControllerTest.java`
- `backend/src/test/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlControllerTest.java`
- `backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java`
- `backend/src/test/java/br/com/facilit/kanban/integration/ApiSession.java`
- `backend/src/main/resources/db/migration/V1__create_kanban_core.sql`
- `backend/src/main/resources/db/migration/V3__optimize_api_list_indexes.sql`
- `docs/governance/PLANO-CONFORMIDADE-F5.md`

## Consumidores mapeados

Foi executada busca de `ProjectIndicators`, `indicators()`, `projectIndicators`, `ProjectRepository`,
`maven-surefire-plugin`, `maven-failsafe-plugin` e `argLine` em produção e testes antes da edição.

## Verificação local disponível neste ambiente

`[EXECUTADO]` `javac -Xlint:all -Werror` nos três novos records de indicadores e `ProjectStatus`: GREEN.
`[EXECUTADO]` parse XML de `backend/pom.xml`: GREEN.

`[DESCONHECIDO]` Maven não existe no ambiente de preparação (`mvn: command not found`), portanto compilação,
Cucumber, Testcontainers e JaCoCo serão comprovados apenas pelo gate do usuário.

## Releitura após a composição

Os 31 arquivos do lote foram relidos integralmente após a composição da candidata, incluindo os arquivos de
produção, testes, schema GraphQL, `backend/pom.xml`, `README.md` e todas as evidências F5-L4. A releitura
corrigiu no `README.md` o estado já encerrado do F5-L3 para GREEN; nenhuma evidência de F5-L1/F5-L2/F5-L3
foi alterada.


## Cobertura — leitura adicional antes do fechamento

Lidos integralmente após a baseline de cobertura:

- `backend/src/test/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlControllerTest.java`
- `backend/src/test/java/br/com/facilit/kanban/delivery/graphql/ResponsibleGraphQlControllerTest.java`
- `backend/src/test/java/br/com/facilit/kanban/delivery/graphql/SecretariatGraphQlControllerTest.java`
- `backend/src/test/java/br/com/facilit/kanban/integration/ResponsiblePersistenceAdapterIT.java`
- `backend/src/test/java/br/com/facilit/kanban/integration/SecretariatPersistenceAdapterIT.java`
- `backend/pom.xml`
- `backend/target/site/jacoco/jacoco.xml`
- `backend/target/site/jacoco/jacoco.csv`

Os relatórios JaCoCo foram usados somente para localizar lacunas de teste; não houve exclusão de classe nem alteração de produção motivada por cobertura.
