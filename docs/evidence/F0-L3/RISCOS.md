# F0-L3 — RISCOS E LACUNAS

Data: 2026-09-21.

- [VERIFICADO: desafio, página 2 · 2026-09-21] A igualdade `plannedEnd == today` não recebe classificação textual explícita para projeto iniciado; a decisão adotada está documentada em `DECISOES.md` e coberta por teste.
- [VERIFICADO: desafio, página 3 · 2026-09-21] O texto chama a métrica de percentual, mas a fórmula escrita termina em dias restantes; a conversão percentual adotada está documentada em `DECISOES.md`.
- [VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java`:34-38 · 2026-09-21] Projeto iniciado sem término previsto não pode ser classificado pelas regras fornecidas e é rejeitado explicitamente.
- [VERIFICADO: `backend/src/main/resources/db/migration/V1__create_kanban_core.sql`:9-20 · 2026-09-21] Unicidade do e-mail é exata no banco. Normalização/formato de e-mail permanece para F1-L1, onde o CRUD e a validação de entrada serão implementados.
- [VERIFICADO: escopo F1-L1 · 2026-09-21] Entidades/repositórios JPA ainda não existem; a migration apenas estabelece o schema. Eles entram junto dos casos de uso CRUD para evitar infraestrutura sem consumidor.
- [EXECUTADO: Maven/Java 25 no ambiente do usuário · 2026-09-21] Mockito emitiu aviso de autoanexação do inline mock maker/Byte Buddy em JDK futuro. Não houve falha na suíte atual; registrar para tratamento antes do freeze se ainda presente.
- [EXECUTADO: gate F0-L3 · 2026-09-21] A execução real da migration V1, integridade mínima, regressão REST/GraphQL/frontend e Docker foram confirmadas. Não permanecem lacunas de execução bloqueantes para a F0.
