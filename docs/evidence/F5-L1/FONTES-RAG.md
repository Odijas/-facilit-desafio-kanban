# F5-L1 — FONTES RAG

Data: 2026-09-24

1. Documento do desafio (PDF, 5 páginas, lido inteiro), com estes trechos:
   - definições de status ("Término previsto é maior que a data de hoje", "menor que a data de hoje");
   - Obs. de recálculo ao editar datas;
   - Nota da tabela de transição;
   - cálculos de % e de atraso;
   - Etapa 1 (auditoria mínima `createdAt`, `updatedAt`);
   - Etapa 2 (listagem por status);
   - Etapa 3 (indicadores por status).
2. Governança: `docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md`, `PROMPT-EXECUTIVO-KANBAN-v1.0.md`, `REPLANEJAMENTO-F3.md` e `PLANO-CONFORMIDADE-F5.md` (lote F5-L1).
3. Código do projeto na v1.0.0 (snapshot `655549c2-…-auditoria-20260923-170540.tar.gz`, SHA-256 `bb7c202f…c5727`), base de todas as alterações.
4. Relatório do auditor independente (2026-09-23): oráculo de 676 combinações e seção "Status congelado no tempo".
5. APIs da plataforma, só da JDK e sem dependência nova:
   - `java.time.Clock.system(ZoneId)`;
   - `ZoneId.of` (lança `DateTimeException` para fuso inválido);
   - `System.Logger` (JEP 264). No Spring Boot, esse log chega ao Logback pela ponte JUL→SLF4J do `spring-boot-starter-logging`.
6. APIs Spring já usadas no projeto ou padrão do Spring Framework 6.2 / Spring Data JPA 3.5, todas gerenciadas pelo Spring Boot 3.5.16:
   - `@Scheduled(cron, zone)` com placeholders;
   - `@EnableScheduling`;
   - `@EventListener(ApplicationReadyEvent.class)`;
   - `@Modifying @Query` de atualização em JPQL;
   - projeção por interface com `Pageable` num `@Query` que retorna `List`.
   - `[NÃO VERIFICADO neste ambiente: Maven Central bloqueado]` compilação e execução contra os jars reais. Resolve: `F5_L1_BACKEND_GREEN`.
7. PostgreSQL 18:
   - `timestamptz AT TIME ZONE 'UTC'` devolve `timestamp`, e o cast `::date` dá a data UTC;
   - `date - integer` → `date`;
   - regra do planejador para índice parcial (o predicado do índice precisa ser implicado pela cláusula `WHERE` do plano).
8. Decreto nº 9.772/2019: extingue o horário de verão no Brasil (base de UTC−3 fixo na coleção Postman).
