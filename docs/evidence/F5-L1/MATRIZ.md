# F5-L1 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-24

Itens da análise de aderência (`docs/governance/PLANO-CONFORMIDADE-F5.md`, §4) fechados por este lote:

| # | Requisito (PDF) | Implementação | Teste | Evidência | Estado |
|---|---|---|---|---|---|
| 13 | Definição dos status vale para "hoje" | V6 `schedule_calculated_on`; `ProjectScheduleRefresher`; `ProjectService` recalcula antes das leituras; `ProjectScheduleRefreshJob` | `ProjectScheduleRefresherTest.recalculatesInProgressProjectAsOverdueAfterPlannedEndPasses`, `recalculatesNotStartedProjectAsOverdueAfterPlannedStartPasses`; `ScheduleFreshnessIT.readsRecalculateProjectsCalculatedOnAnEarlierDayWithoutTouchingUpdatedAt` | gate `F5_L1_API_GREEN` (projeto envelhecido 10 dias → OVERDUE) | CANDIDATE |
| 17 | % de tempo restante de hoje | idem | mesmos testes (50% → 0%) | gate `F5_L1_API_GREEN` | CANDIDATE |
| 18 | Dias de atraso de hoje | idem | mesmos testes (0 → 8 dias) | gate `F5_L1_API_GREEN` | CANDIDATE |
| 27 | Listagem por status do Kanban | recálculo antes de `listByStatus`/`search` | `ProjectScheduleRefresherTest.listingsByStatusAndIndicatorsFollowTheCurrentDate`; `ScheduleFreshnessIT.listingsByStatusFiltersAndIndicatorsUseTodaysStatus` | gate `F5_L1_API_GREEN` | CANDIDATE |
| E3a/E3b | Indicadores (média de atraso e contagem por status) de hoje | recálculo antes de `indicators` | mesmos testes | gate `F5_L1_API_GREEN` | CANDIDATE (a Etapa 3 segue no F5-L4) |
| 15 | Tabela de transição sobre o status real | `ProjectStatusTransition` usa o status calculado para hoje | `ProjectStatusTransitionTest` (19), `ProjectScheduleRefresherTest.transitionStartsFromTodaysStatus` | gate: Em andamento desatualizado → Atrasado recusado | CANDIDATE |
| — | "Hoje" no fuso de negócio | `ApplicationBeans.applicationClock` (`APP_TIME_ZONE`) | `ProjectScheduleRefresherTest.usesTheBusinessTimeZoneToDecideWhatTodayIs` | gate: `actualStart` = data de São Paulo | CANDIDATE |
| 32 | Validação de entrada: data realizada no futuro | `ProjectDates.requireActualDatesNotAfter`, chamado em `create`/`update` | `ProjectDatesTest` (4), `ProjectServiceTest.rejectsActualDatesAfterTodayOnCreateAndUpdate` | gate: POST com início realizado amanhã → 400 | CANDIDATE |
| 39 | README coerente e interpretações documentadas | `README.md` (Regras de negócio, Interpretações do enunciado, Limitações), ADR 0002 | checagem estática do gate | `F5_L1_STATIC_GREEN` | CANDIDATE |
| — | Migração segura de banco da v1.0.0 | V6 com preenchimento pela data UTC da última gravação, `NOT NULL`, índice | `ScheduleCalculationDateMigrationIT` | `F5_L1_BACKEND_GREEN`; gate com banco novo (migrations 1–6) | CANDIDATE |
