# F5-P1 — LIVRO-RAZÃO DE LEITURA

Data: 2026-09-25

Base: snapshot da tag `v2.0.0` (`git archive`, SHA-256 `941276…2177`, 439 arquivos), igual à `main` @ `3dac1ea`.

## Lidos integralmente

- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/`:
  - requests: `ProjectRequest`, `ProjectStatusRequest`, `ResponsibleRequest`, `SecretariatRequest`, `ResponsibleCredentialsRequest`;
  - respostas: `ProjectResponse`, `ResponsibleResponse`, `SecretariatResponse`, `PageResponse`, `ProjectIndicatorsResponse`, `ProjectGroupIndicatorResponse`, `ProjectDeadlinesResponse`;
  - controllers: `ProjectRestController`, `ResponsibleRestController`, `SecretariatRestController`, `ProjectIndicatorsRestController`, `HealthRestController`.
- `backend/src/main/java/br/com/facilit/kanban/delivery/auth/AuthRestController.java`.
- `backend/src/main/java/br/com/facilit/kanban/delivery/common/InputLimits.java` (modelo da classe de constantes).
- `backend/src/test/java/br/com/facilit/kanban/integration/OpenApiContractIT.java`.
- `backend/src/test/java/br/com/facilit/kanban/integration/ProjectPersistenceAdapterIT.java`.
- `backend/src/main/java/br/com/facilit/kanban/infrastructure/config/OpenApiConfiguration.java`.
- `backend/Dockerfile` e `frontend/package.json` (versão).
- `docs/evidence/F5-C2/*` (formato das evidências e do gate).

## Lidos em parte

- `ProjectPersistenceAdapter.java`: `matching` (predicado `LIKE`), `pageable` e constantes.
- `ApiErrorDocumentation.java`: o `OpenApiCustomizer` documenta só as respostas de erro.
- `application.yml`: bloco `springdoc` (OpenAPI 3.0 padrão, `override-with-generic-response: false`, CSRF no Swagger UI).
- `backend/pom.xml`: versão do projeto, `-Xlint:all -Werror` e regra do JaCoCo.
- `application/health/HealthStatus.java` e `HealthQuery.java` (`status = "UP"`).
- `AuthenticatedActorResolver.java`: nomes das autoridades (`ROLE_ADMIN`, `ROLE_RESPONSIBLE`).
- `KanbanApiIT.java`: forma da consulta GraphQL `projects(... text: ...)`.
- `resources/graphql/kanban.graphqls`: `Query.projects`.
- `README.md`: seções de execução, Swagger e API (sem alteração neste lote).
- `docs/evidence/F5/SAIDA-GATE.txt`: contagens da v2.0.0 (25/174 e 12/49; JaCoCo 95,96%).

## Conferidos depois da edição automatizada

- Cada substituição por script exigiu o número exato de ocorrências (`assert count == n`).
- As 25 anotações `@Parameter` foram contadas depois da edição: 13 `@RequestParam` e 12 `{id}`.
- A constante `LIKE_ESCAPE` saiu, na primeira edição, entre o Javadoc de outra constante e a própria constante; corrigido e relido.
