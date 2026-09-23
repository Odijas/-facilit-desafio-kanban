# F0-L2 — FONTES RAG

Data: 2026-09-21.

- [VERIFICADO: `Desafio Técnico Backend - Kanban.pdf`, páginas 1–3 · 2026-09-21] O modelo exigido contém Projeto, Responsável, múltiplos responsáveis por projeto, datas previstas/realizadas, status e auditoria mínima `createdAt`/`updatedAt`.
- [VERIFICADO: `docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md`:137-147 · 2026-09-21] O escopo do F0-L2 é Projeto, Responsável, relacionamento, estrutura inicial de Secretaria, datas, auditoria mínima e regras fundamentais do domínio.
- [VERIFICADO: Oracle Java SE 25 `UUID` · 2026-09-21] `java.util.UUID` representa identificadores UUID imutáveis de 128 bits: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/UUID.html
- [VERIFICADO: Oracle Java SE 25 `LocalDate` · 2026-09-21] `LocalDate` representa uma data sem horário/fuso e é imutável/thread-safe: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/LocalDate.html
- [VERIFICADO: Oracle Java SE 25 `Instant` · 2026-09-21] `Instant` representa um ponto na linha do tempo e é adequado para timestamps de auditoria: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/Instant.html
- [VERIFICADO: Oracle Java SE 25 `Set` · 2026-09-21] `Set.copyOf` devolve conjunto não modificável e não aceita elementos nulos: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Set.html
