# F0-L2 — CONSUMIDORES MAPEADOS

Data: 2026-09-21.

[EXECUTADO: `grep -R -n --exclude-dir=target --exclude-dir=.git -E '\b(Project|ProjectStatus|ProjectDates|Responsible|Secretariat|AuditMetadata)\b' backend/src` · 2026-09-21] Resultado: os novos tipos aparecem apenas nas próprias definições/imports de domínio; não existem consumidores preexistentes de produção ou testes.

```text
backend/src/main/java/br/com/facilit/kanban/domain/project/Project.java
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectDates.java
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatus.java
backend/src/main/java/br/com/facilit/kanban/domain/common/AuditMetadata.java
backend/src/main/java/br/com/facilit/kanban/domain/secretariat/Secretariat.java
backend/src/main/java/br/com/facilit/kanban/domain/responsible/Responsible.java
```

[VERIFICADO: busca acima · 2026-09-21] Não houve alteração de assinatura pública existente nem comportamento observável dos adaptadores REST/GraphQL do F0-L1.
