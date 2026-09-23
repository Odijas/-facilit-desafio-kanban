# F0-L3 — FONTES RAG

Data: 2026-09-21.

- [VERIFICADO: `Desafio Técnico Backend - Kanban.pdf`, páginas 1–3 · 2026-09-21] O desafio define os quatro estados, as condições de atraso/conclusão, a fórmula-base de tempo restante, dias de atraso e exige recálculo de status/métricas ao criar ou editar Projeto.
- [VERIFICADO: `docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md`:149-169 · 2026-09-21] O F0-L3 exige cálculo automático de status, percentual de tempo restante, dias de atraso, consistência temporal, testes unitários do domínio, persistência mínima e primeira baseline da F0.
- [VERIFICADO: Oracle Java SE 25 `ChronoUnit` · 2026-09-21] `ChronoUnit.DAYS.between` calcula a quantidade entre dois temporais e fornece a unidade de dias usada pelo motor: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/temporal/ChronoUnit.html
- [VERIFICADO: Oracle Java SE 25 `LocalDate` · 2026-09-21] `LocalDate` representa data sem horário/fuso e é a base temporal do domínio: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/LocalDate.html
- [VERIFICADO: Spring Boot 3.5 SQL Databases · 2026-09-21] O starter JPA integra Hibernate, Spring Data JPA e Spring ORM; classes de entidade são descobertas por entity scanning: https://docs.spring.io/spring-boot/3.5/reference/data/sql.html
- [VERIFICADO: Spring Boot 3.5 Application Properties · 2026-09-21] `spring.flyway.locations` usa por padrão `classpath:db/migration`, compatível com a configuração vigente: https://docs.spring.io/spring-boot/3.5/appendix/application-properties/
- [VERIFICADO: Redgate Flyway — Versioned migrations · 2026-09-21] Migrations versionadas são aplicadas em ordem uma única vez e registradas na tabela de histórico: https://documentation.red-gate.com/fd/versioned-migrations-273973333.html
- [VERIFICADO: PostgreSQL 18 — Constraints · 2026-09-21] PostgreSQL suporta `CHECK`, `UNIQUE`, `PRIMARY KEY` e `FOREIGN KEY`; `UNIQUE` cria índice B-tree automaticamente: https://www.postgresql.org/docs/18/ddl-constraints.html
