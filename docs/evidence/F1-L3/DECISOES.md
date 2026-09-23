# F1-L3 — DECISÕES

Data: 2026-09-22

## D01 — Erros REST com RFC 9457/ProblemDetail + código estável

O handler existente foi preservado e especializado com `ApiErrorCode`. REST retorna `ProblemDetail` com `code`; validação inclui violações por campo. Não foi criado envelope paralelo.

## D02 — Mesmo vocabulário de erro em GraphQL

GraphQL mantém o mecanismo nativo de `errors`, adicionando `extensions.code` com os mesmos códigos sem forçar formato HTTP sobre GraphQL.

## D03 — Validação na fronteira

REST usa Bean Validation nos records de request. GraphQL usa `@Argument @Valid` e constraints nos records de input, conforme Spring GraphQL 1.4.6.

## D04 — Swagger/OpenAPI por springdoc 2.8.17

Spring Boot 3.5.x é compatível com a linha springdoc 2.8.x; 2.8.17 existe no Maven Central. `/api-docs` e `/swagger-ui.html` são configurados explicitamente.

## D05 — Integração real em PostgreSQL descartável

`KanbanApiIT` usa Testcontainers 1.21.4 + `@ServiceConnection` e PostgreSQL 18.6, exercitando migrations, API REST, GraphQL, erros e OpenAPI. O teste não é opcional no gate: sem Docker, `mvn verify` deve falhar.

## D06 — Failsafe separa testes de integração

`*IT` roda no Maven Failsafe em `integration-test`/`verify`. O Dockerfile usa `-DskipITs` porque o build da imagem não possui socket Docker; testes unitários continuam executados e os ITs continuam compilados. O gate local executa `mvn clean verify` sem `skipITs`.

## D07 — Índices seguem queries reais

A V3 substitui o índice simples de status por `(status, name, id)` e cria `(name, id)` para listagens paginadas ordenadas já existentes. Não foram antecipados índices dos filtros avançados ainda fora do lote.
