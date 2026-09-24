# F5-L3 — FONTES RAG

Data: 2026-09-24

1. Documento do desafio (PDF):
   - Etapa 1: "Testes unitários (JUnit) para services e controllers".
   - Etapa 2: "Testes unitários cobrindo cada linha da tabela de transição (incluindo confirmações obrigatórias)".
   - Requisitos não funcionais: "Testes: unidade, integração (repositórios, transações) e API (end-to-end/Postman)".
2. Governança: `docs/governance/PLANO-CONFORMIDADE-F5.md` (D5, lote F5-L3, itens 24, 29 e 37 da matriz) e `PROMPT-EXECUTIVO-BASE-v1.1.md`.
3. Spring Framework 6.2.19 (código-fonte da tag `v6.2.19` no GitHub):
   - `TransactionTemplate.execute(TransactionCallback)` e `TransactionOperations.executeWithoutResult(Consumer)`;
   - `HibernateJpaDialect`: `StaleObjectStateException`/`StaleStateException` → `ObjectOptimisticLockingFailureException`;
   - `@MockitoBean` (`org.springframework.test.context.bean.override.mockito`), desde 6.2.
4. Spring Boot 3.5.16 (código-fonte da tag `v3.5.16` no GitHub):
   - `@MockBean` depreciado desde 3.4.0, para remoção no 4.0.0;
   - imports do `@AutoConfigureDataJpa` (Flyway, `JdbcTemplate`, `ServiceConnectionAutoConfiguration`);
   - `GraphQlTypeExcludeFilter` inclui `@ControllerAdvice` no `@GraphQlTest`.
5. Mockito 5.17.0 (código-fonte da tag `v5.17.0`, Javadoc de `Mockito`, seção 0.3): agente explícito a partir do Java 21, com o exemplo Maven `maven-dependency-plugin:properties` + `-javaagent:${org.mockito:mockito-core:jar}`.
6. PostgreSQL: `uuid` é comparado byte a byte, sem sinal; `java.util.UUID.compareTo` compara os dois `long` com sinal. Conclusão usada no teste de ordenação (`ProjectPersistenceAdapterIT`).
7. APIs usadas sem execução neste ambiente (Maven Central bloqueado), a confirmar pelo gate local: `@WebMvcTest`, `@GraphQlTest`, `GraphQlTester`, `@DataJpaTest`, `TestEntityManager`, `@WithMockUser`, Testcontainers 1.21.4.
