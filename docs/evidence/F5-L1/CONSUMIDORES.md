# F5-L1 — CONSUMIDORES

Data: 2026-09-24

- `ProjectRepository` (porta):
  - `save(Project)` passa a `save(Project, LocalDate)`;
  - novos `findStaleSchedules` e `updateSchedules`.
  - Implementações: `ProjectPersistenceAdapter` (produção) e `InMemoryProjectRepository` (testes).
  - Chamadores de `save`: só `ProjectService` (create, update, transition) e `ResponsibleServiceTest` (montagem de cenário) `[VERIFICADO: grep projectRepository.save|\.save(]`.
- `ProjectService`: assinatura pública igual, mais `refreshSchedules()`.
  - Consumidores: `ProjectRestController`, `ProjectIndicatorsRestController`, `ProjectGraphQlController` e `ProjectScheduleRefreshJob` (novo).
  - As leituras passam a recalcular antes, sem mudança de contrato.
- `ProjectStatusTransition`: a origem passa a ser o status calculado para hoje. Consumidores: `ProjectService.transition` e `ProjectStatusTransitionTest`.
  - Dois testes antigos partiam de status gravado incoerente com as datas; foram substituídos por testes de status desatualizado.
- `ProjectJpaEntity.replace`: novo parâmetro `scheduleCalculatedOn`. Único chamador: `ProjectPersistenceAdapter.save`.
- `projects.schedule_calculated_on` (V6, `NOT NULL`):
  - Todo `INSERT` passa pela entidade JPA (`ddl-auto: validate`), que sempre preenche a coluna.
  - `ScheduleCalculationDateMigrationIT` insere linhas antes da V6, com o schema V5.
- `Clock` (bean `applicationClock`): agora no fuso de negócio.
  - Consumidores:
    - `ProjectService`, o único que deriva "hoje" (`LocalDate.now(clock)`);
    - `ResponsibleService`, `ResponsibleCredentialService`, `SecretariatService` e `SecurityAdminBootstrap`, que usam só `clock.instant()`, sem efeito do fuso `[VERIFICADO: grep clock.|LocalDate.now]`;
    - `KanbanApiIT` e `SecurityApiIT` (novos consumidores do bean).
- `application.yml`: novas chaves `app.time-zone` e `app.schedule-refresh.cron`, lidas por `ApplicationBeans` e `ProjectScheduleRefreshJob`.
- `compose.yaml` / `.env.example`: `APP_TIME_ZONE` com padrão `America/Sao_Paulo`. `.env` antigo sem a variável continua válido (`${APP_TIME_ZONE:-…}`).
- Coleção Postman: variável `today` em UTC−3 (usada em criação e na asserção de `actualStart`).
- Frontend e esquema GraphQL: sem alteração. Os campos de resposta são os mesmos; `schedule_calculated_on` não é exposto.
- Gate F4 (`docs/evidence/F4/VERIFICACAO-USUARIO.md`): histórico. Usa `date -u` e espera migrations `1 2 3 4 5`, então não se aplica mais à árvore do F5 (o gate deste lote o substitui).
