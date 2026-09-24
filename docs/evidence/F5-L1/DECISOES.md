# F5-L1 — DECISÕES

Data: 2026-09-24

Escopo: `docs/governance/PLANO-CONFORMIDADE-F5.md`, lote F5-L1 (regras sempre corretas), decisões D1 e D2 e rejeição de data realizada futura. O usuário pediu a execução do L1 em 2026-09-24 ("execute l1 do plano"). A decisão de versão (D6) fica para o F5-L2, porque o L1 não muda contrato de API.

## Decisões do lote

- `[VERIFICADO: ADR 0002]` Status, dias de atraso e % continuam gravados. Isso preserva paginação, filtro por status via índice e indicadores em SQL. A migration V6 cria `schedule_calculated_on` e, antes de toda leitura, os projetos não concluídos calculados antes de hoje são recalculados (`ProjectScheduleRefresher`).
- `[VERIFICADO]` o recálculo grava com `UPDATE … WHERE schedule_calculated_on < hoje`. Uma gravação concorrente mais nova prevalece, e repetir não muda nada. O estado fica no banco, e não em memória, então funciona com várias instâncias e depois de reinícios.
- `[VERIFICADO]` projeto concluído fica fora do recálculo. Com término realizado, o resultado é sempre Concluído, 0 dia e 0%.
- `[VERIFICADO]` o recálculo **não** altera `updatedAt`. `updatedAt` é a auditoria mínima de edição do usuário (Etapa 1 do desafio), e a data do cálculo tem coluna própria.
- `[VERIFICADO]` preenchimento da V6 com `(updated_at AT TIME ZONE 'UTC')::date`. Na v1.0.0 o cálculo ocorria só ao gravar, com `Clock.systemUTC()` e o mesmo instante de `updatedAt` (`ProjectService` da v1.0.0). Esse é o dia exato usado no cálculo.
- `[VERIFICADO]` a transição classifica a origem pelas datas e por hoje (`ProjectStatusTransition`), e não pelo status gravado. Assim a tabela vale mesmo se o chamador não recalcular.
- `[VERIFICADO]` "hoje" = `Clock.system(ZoneId.of(app.time-zone))`, com `APP_TIME_ZONE` e padrão `America/Sao_Paulo`. Um fuso inválido lança `DateTimeException` na criação do bean e impede a subida.
- `[VERIFICADO]` agendamento: `ProjectScheduleRefreshJob` na subida (`ApplicationReadyEvent`) e à meia-noite (`@Scheduled(cron = app.schedule-refresh.cron, zone = app.time-zone)`).
  - A falha é registrada e não derruba a aplicação, porque as leituras recalculam de novo.
  - O log registra só o gatilho, a contagem e o tipo do erro.
- `[VERIFICADO]` datas realizadas depois de hoje são recusadas em `create`/`update` (`ProjectDates.requireActualDatesNotAfter`).
  - A regra **não** está no calculador, para não quebrar o recálculo de linhas antigas.
  - A transição só grava "hoje".
- `[VERIFICADO]` mensagens novas em pt-BR (data realizada futura; log do recálculo). As mensagens em inglês existentes ficam para o F5-L2, que troca o contrato de erro.
- `[VERIFICADO]` exemplos do Swagger de `ProjectRequest` ajustados (datas realizadas no passado), porque os anteriores (término realizado 2026-10-09) passariam a ser recusados.
- `[VERIFICADO]` coleção Postman: "hoje" = UTC−3. O Brasil não tem horário de verão desde 2019 (Decreto nº 9.772/2019), então America/Sao_Paulo é UTC−3 fixo.
- `[VERIFICADO]` ITs existentes (`KanbanApiIT`, `SecurityApiIT`) passam a obter "hoje" do `Clock` da aplicação. Antes usavam `ZoneOffset.UTC` e falhariam das 21h à meia-noite com o fuso novo.

## Desvios em relação ao plano (registrados, sem mudar o objetivo)

| Plano | Feito | Motivo |
|---|---|---|
| V6 com `schedule_calculated_on` **e** `version` | V6 só com `schedule_calculated_on`; `version` fica numa V7 no F5-L3 | uma migration por lote e por assunto; `@Version` pertence a D5 (L3) |
| índice parcial `WHERE status <> 'COMPLETED'` | índice composto `(status, schedule_calculated_on)` com `status IN (…)` | com parâmetro de consulta (plano genérico), o PostgreSQL não prova a condição do índice parcial |
| `ScheduleFreshnessIT` com `MutableClock` como bean | IT com PostgreSQL real que recua as datas e a data do cálculo no banco; o relógio que avança fica no teste de aplicação (`ProjectScheduleRefresherTest`) | não substitui o `Clock` da aplicação no contexto Spring e prova o relógio real com o fuso real |
| `ProjectScheduleRefresher` como bean | criado dentro do `ProjectService` (como `ProjectStatusTransition`) e exposto por `ProjectService.refreshSchedules()` | nenhum construtor muda; os testes existentes seguem iguais |
| `ScheduleRefreshJob` | `ProjectScheduleRefreshJob` + `SchedulingConfiguration` (`@EnableScheduling`) | nome explícito |
