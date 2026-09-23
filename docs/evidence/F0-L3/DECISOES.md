# F0-L3 — DECISÕES

Data: 2026-09-21.

- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java`:9-81 · 2026-09-21] O motor recebe `today` explicitamente; nenhum `LocalDate.now()` fica escondido no domínio, mantendo cálculo determinístico e testável.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java`:20-43 · 2026-09-21] A precedência implementada é: Concluído → Atrasado → Em andamento → A iniciar. Isso resolve a sobreposição textual entre “A iniciar” e “Atrasado” quando o início previsto já passou sem início realizado.
- [VERIFICADO: `Desafio Técnico Backend - Kanban.pdf`, página 2 · 2026-09-21] O texto usa `Término Previsto > hoje` para Em andamento e `< hoje` para Atrasado, deixando `Término Previsto == hoje` sem classificação explícita quando já houve início realizado.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java`:34-40 · 2026-09-21] O caso `Término Previsto == hoje`, com início realizado e sem término realizado, é classificado como Em andamento; atraso só começa depois da data prevista. A decisão está explicitada e coberta por teste.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java`:55-81 · 2026-09-21] Como o desafio nomeia a métrica como percentual, mas descreve `restante = total - usado`, o motor converte `restante / total * 100`, arredonda para inteiro e limita naturalmente a faixa de 0 a 100.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectDates.java`:11-18 · 2026-09-21] Intervalos planejados ou realizados invertidos são rejeitados no value object de datas.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/project/Project.java`:8-41 · 2026-09-21] Projeto recebe um único `ProjectScheduleMetrics`, evitando passar status, atraso e percentual como três valores independentes.
- [VERIFICADO: `backend/src/main/resources/db/migration/V1__create_kanban_core.sql`:1-58 · 2026-09-21] A persistência mínima desta fase é deliberadamente o schema versionado com integridade relacional e índices; entidades/repositórios JPA ficam para F1-L1, quando surgem os casos de uso CRUD. Isso evita introduzir adaptadores sem consumidor funcional.
