# F5-L3 — CONSUMIDORES

Data: 2026-09-24

- **Construtores dos serviços** (`ProjectService`, `ResponsibleService`, `ResponsibleCredentialService`, `SecretariatService`) ganharam `TransactionRunner` antes do `Clock`. Chamadores:
  - produção: só `ApplicationBeans` (composition root) `[VERIFICADO: grep "new ProjectService(\|new ResponsibleService(\|new ResponsibleCredentialService(\|new SecretariatService("]`;
  - testes: `ProjectServiceTest`, `ProjectScheduleRefresherTest`, `ResponsibleServiceTest`, `ResponsibleCredentialServiceTest`, `SecretariatServiceTest` (com `DirectTransactionRunner`) e `ProjectServiceMockitoTest` (com o `TransactionRunner` simulado).
- **Assinaturas públicas dos casos de uso:** não mudaram. Controllers REST e GraphQL, o job de recálculo e o bootstrap de segurança chamam os mesmos métodos.
- **Ordem dos logs de negócio:** os eventos de sucesso continuam com o mesmo texto; saem depois do commit. O gate do F5-L2 conferia o texto e a presença dos eventos, não o instante.
- **`projects.version` (V7):**
  - `ProjectJpaEntity` mapeia a coluna; o adaptador não a expõe para a aplicação.
  - O recálculo diário usa `update` JPQL (em massa), que não incrementa a versão; ele só grava status e métricas calculados das datas, então não conflita com edições.
  - SQL direto nos testes (`ScheduleFreshnessIT`, `ScheduleCalculationDateMigrationIT`) não informa a coluna e recebe o `DEFAULT 0`.
  - `KanbanApiIT` confere as migrations com `>= 4`; o gate passa a exigir `1 2 3 4 5 6 7`.
- **Tratadores de 409:** `RestExceptionHandler.handleOptimisticLock` e `GraphQlErrorHandler.handleOptimisticLock` recebem a classe base `OptimisticLockingFailureException`. `RestExceptionHandlerTest` continua passando `ObjectOptimisticLockingFailureException` (subclasse).
- **`backend/pom.xml`:** o `argLine` do Surefire e do Failsafe passa a carregar o agente do Mockito. Não havia `argLine` antes. O CI (`mvn -B -ntp clean verify`) usa o mesmo `pom.xml`.
- **Frontend, coleção Postman, esquema GraphQL e OpenAPI:** sem mudança. O 409 de escrita já estava documentado em todas as operações de escrita (F5-L2).
