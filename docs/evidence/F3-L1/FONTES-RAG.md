# F3-L1 — FONTES RAG

Data: 2026-09-22

Ordem aplicada conforme BASE:

1. Código e contratos instalados/versionados no projeto: Spring Boot 3.5.16, Spring Data JPA gerenciado pelo Boot, Spring GraphQL, React 19.3.0, TanStack Query 5.102.8, MUI 9.4.0, TypeScript 5.9.3 e Biome 2.5.14.
2. Documentação oficial consultada:
   - Spring Data JPA — query methods e `@Query`: https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
   - Spring Data JPA — projections: https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html
   - Spring for GraphQL — annotated controllers: https://docs.spring.io/spring-graphql/reference/controllers.html
   - Biome — manual installation/CLI: https://biomejs.dev/guides/manual-installation/
3. Código real e migrations do projeto.
4. Saída real dos comandos estáticos executados neste ambiente e gate runtime do usuário.

Não foram usados blogs, fóruns ou APIs inferidas de memória como fonte de implementação.

## Rev2 — 2026-09-22

- Tipos instalados de `@tanstack/query-core` 5.102.8 (`keepPreviousData`, `placeholderData`, `QueryObserverPlaceholderResult`).
- Saída real de Biome 2.5.14, TypeScript 5.9.3, Vitest 4.1.9 e Vite 8.3.0 executados sobre o rev2 (Node 22.22.2).

## Rev3 — 2026-09-22

- https://docs.spring.io/spring-data/jpa/reference/3.5/jpa/specifications.html (versão 3.5.13 exibida na página).
- https://raw.githubusercontent.com/spring-projects/spring-data-jpa/3.5.13/spring-data-jpa/src/main/java/org/springframework/data/jpa/repository/support/SimpleJpaRepository.java (`getCountQuery`, `findAll(Specification, Pageable)`, `applySpecificationToCriteria`).
- Versão de spring-data-jpa confirmada pela saída real do gate do usuário (`spring-data-jpa-3.5.13.jar`).

## Rev4 — 2026-09-22

- Schema GraphQL real do projeto (`health.graphqls`, `kanban.graphqls`).
- graphql-js 16.14.2 (npm) executado localmente para validar os documentos do gate.
- Saída real do gate do usuário (mensagem `VariableTypeMismatch` do graphql-java).
