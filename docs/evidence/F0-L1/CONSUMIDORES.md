# F0-L1 — CONSUMIDORES MAPEADOS

## `HealthQuery`

Comando:

```bash
grep -R -n --exclude-dir=target --exclude-dir=.git 'HealthQuery' backend/src
```

Saída real:

```text
backend/src/main/java/br/com/facilit/kanban/application/health/HealthQuery.java:3:public final class HealthQuery {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/HealthGraphQlController.java:3:import br.com.facilit.kanban.application.health.HealthQuery;
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/HealthGraphQlController.java:11:    private final HealthQuery healthQuery;
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/HealthGraphQlController.java:13:    public HealthGraphQlController(HealthQuery healthQuery) {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/HealthRestController.java:3:import br.com.facilit.kanban.application.health.HealthQuery;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/HealthRestController.java:13:    private final HealthQuery healthQuery;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/HealthRestController.java:15:    public HealthRestController(HealthQuery healthQuery) {
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:3:import br.com.facilit.kanban.application.health.HealthQuery;
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:11:    HealthQuery healthQuery() {
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:12:        return new HealthQuery();
backend/src/test/java/br/com/facilit/kanban/application/health/HealthQueryTest.java:7:class HealthQueryTest {
backend/src/test/java/br/com/facilit/kanban/application/health/HealthQueryTest.java:11:        HealthStatus result = new HealthQuery().execute();
```

Consumidores de comportamento adicionais, lidos integralmente: `HealthRestControllerTest` e `HealthGraphQlControllerTest` exercitam os adaptadores que delegam ao caso de uso.

## `getHealthStatus`

Comando:

```bash
grep -R -n --exclude-dir=node_modules --exclude-dir=dist 'getHealthStatus' frontend/src
```

Saída real:

```text
frontend/src/api/health.ts:5:export async function getHealthStatus(): Promise<HealthStatus> {
frontend/src/App.tsx:3:import { getHealthStatus } from "./api/health";
frontend/src/App.tsx:8:    queryFn: getHealthStatus,
frontend/src/App.test.tsx:5:import { getHealthStatus } from "./api/health";
frontend/src/App.test.tsx:8:  getHealthStatus: vi.fn(),
frontend/src/App.test.tsx:11:const mockedGetHealthStatus = vi.mocked(getHealthStatus);
```
