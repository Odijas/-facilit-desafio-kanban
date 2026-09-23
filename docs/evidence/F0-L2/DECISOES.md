# F0-L2 — DECISÕES

Data: 2026-09-21.

- [VERIFICADO: `docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md`:148-160 · 2026-09-21] O domínio permanece puro, sem Spring/JPA/Bean Validation.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/project/Project.java`:1-34 · 2026-09-21] Projeto usa UUID, nome, status, conjunto imutável de responsáveis, datas e auditoria.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/responsible/Responsible.java`:1-30 · 2026-09-21] Responsável contém UUID, nome, email, cargo, vínculo opcional com Secretaria e auditoria.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/secretariat/Secretariat.java`:1-19 · 2026-09-21] Secretaria nasce apenas com identificador, nome e auditoria; CRUD continua reservado ao diferencial posterior.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectDates.java`:1-10 · 2026-09-21] As quatro datas foram agrupadas em um value object sem regras temporais nesta rodada.
- [VERIFICADO: `docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md`:149-159 · 2026-09-21] Cálculo de status, métricas, consistência temporal, testes unitários persistentes do domínio e persistência ficam no F0-L3.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/common/AuditMetadata.java`:1-17 · 2026-09-21] Auditoria rejeita `updatedAt` anterior a `createdAt`.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/project/Project.java`:16-33 · 2026-09-21] Projeto exige ao menos um responsável, conforme “um ou mais responsáveis” do desafio, e protege a coleção com cópia imutável.
- [VERIFICADO: `docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md`:153-158 · 2026-09-21] Nenhum novo arquivo de teste foi adicionado, pois testes unitários persistentes do domínio estão explicitamente previstos no F0-L3 e o BASE proíbe criar novos testes sem pedido explícito.
