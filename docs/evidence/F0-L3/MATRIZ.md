# F0-L3 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-21.

| Requisito | Implementação | Teste/verificação | Evidência |
|---|---|---|---|
| Status automático | `ProjectScheduleCalculator` | cenários dos quatro estados | `[EXECUTADO]` probe + JUnit; suíte backend GREEN |
| Dias de atraso | `ProjectScheduleCalculator.calculateDelayDays` | atraso, concluído e sem atraso | `[EXECUTADO]` probe + JUnit |
| Percentual restante | `ProjectScheduleCalculator.calculateRemainingTimePercentage` | 100%, 50%, 0%, ausência de datas e duração zero | `[EXECUTADO]` probe + JUnit |
| Consistência de datas | `ProjectDates` | intervalos planejados/realizados invertidos | `[EXECUTADO]` probe + JUnit |
| Métricas coesas | `ProjectScheduleMetrics` + `Project` | limites e exposição pelo aggregate | `[EXECUTADO]` probe + JUnit |
| Testes unitários do domínio | `ProjectScheduleCalculatorTest`, `ProjectTest` | `mvn -B -ntp clean verify` | `[EXECUTADO]` 18 testes totais, 0 falhas/erros |
| Persistência mínima | `V1__create_kanban_core.sql` | Flyway em PostgreSQL 18.6 | `[EXECUTADO]` `1:true`; quatro tabelas core |
| Integridade relacional | PK/FK/UNIQUE/CHECK + índices | introspecção + caminhos legítimo/ataque | `[EXECUTADO]` 5 índices; legítimo aceito; métricas inválidas bloqueadas |
| Regressão REST/GraphQL | fundação existente | health REST/GraphQL em Docker | `[EXECUTADO]` REST `UP`; GraphQL `UP` |
| Regressão frontend | fundação existente | lint/typecheck/test/build + HTTP | `[EXECUTADO]` 2 testes, build GREEN e HTTP 200 |
