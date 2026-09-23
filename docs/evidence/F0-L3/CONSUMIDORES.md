# F0-L3 — CONSUMIDORES MAPEADOS

Data: 2026-09-21.

[EXECUTADO: `grep -R -n --exclude-dir=target --exclude-dir=.git -E '\b(Project|ProjectDates|ProjectScheduleCalculator|ProjectScheduleMetrics|ProjectStatus)\b' backend/src` · 2026-09-21] Os consumidores encontrados são:

```text
Project.java
ProjectDates.java
ProjectScheduleCalculator.java
ProjectScheduleMetrics.java
ProjectStatus.java
ApplicationBeans.java
ProjectScheduleCalculatorTest.java
ProjectTest.java
```

[VERIFICADO: busca acima · 2026-09-21] `Project` e `ProjectDates` alteram contrato do domínio criado no F0-L2; não existem consumidores de produção além do próprio domínio/configuração. Os consumidores de teste novos foram lidos integralmente.

[VERIFICADO: `backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java`:1-20 · 2026-09-21] `ProjectScheduleCalculator` é exposto no composition root para reutilização pelos casos de uso da F1, sem acoplar o domínio ao Spring.
