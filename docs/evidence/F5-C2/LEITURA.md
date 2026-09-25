# F5-C2 — LIVRO-RAZÃO DE LEITURA

Data: 2026-09-24

## Lidos integralmente

- `backend/src/test/resources/features/transicoes.feature`
- `backend/src/test/java/br/com/facilit/kanban/bdd/StatusTransitionSteps.java` e `StatusTransitionsBddTest.java`
- `backend/src/main/java/br/com/facilit/kanban/application/project/ProjectFilter.java`
- `backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRequest.java`, `ResponsibleRequest.java`, `SecretariatRequest.java`, `ResponsibleCredentialsRequest.java`
- `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleMetrics.java` (tipos de `delayDays` e `remainingTimePercentage`)

## Lidos em parte

- `ProjectRestController.java`: `list` (construção do `ProjectFilter`).
- `ProjectGraphQlController.java`, `ResponsibleGraphQlController.java`, `SecretariatGraphQlController.java`: consultas com filtro e records de input.
- `RestExceptionHandler.java`: 400 para `IllegalArgumentException` e `MethodArgumentNotValidException` (ordenação das violações por campo).
- `GraphQlErrorHandler.java`: `IllegalArgumentException` e `ConstraintViolationException`.
- `resources/graphql/kanban.graphqls`: `Query.projects`.
- Testes de controller alterados: preparação, estilo e testes de validação existentes.
- `README.md`: seção de API e estado dos lotes.

## Conferidos depois da edição automatizada

- Cada substituição por script foi conferida com uma única ocorrência. Os imports dos 3 controllers GraphQL foram reordenados e relidos.
- `domain` + `application` + `InputLimits` compilados com `-Xlint:all -Werror` (ver EXECUCAO).
