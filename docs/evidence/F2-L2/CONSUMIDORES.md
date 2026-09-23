# F2-L2 — CONSUMIDORES MAPEADOS

Data: 2026-09-22

Busca reexecutada após a correção:

```text
$ grep -RInE '/api/v1/auth/(csrf|login|me|logout)|getCurrentUser|login\(|logout\(' frontend/src backend/src/test backend/src/main/java
frontend/src/App.tsx:21:  getCurrentUser,
frontend/src/App.tsx:78:    }) => login(loginEmail, loginPassword),
frontend/src/App.tsx:247:    queryFn: getCurrentUser,
frontend/src/App.test.tsx:6:import { AuthApiError, getCurrentUser, login, logout } from "./api/auth";
frontend/src/App.test.tsx:22:    getCurrentUser: vi.fn(),
frontend/src/App.test.tsx:32:const mockedGetCurrentUser = vi.mocked(getCurrentUser);
frontend/src/api/auth.test.ts:2:import { getCurrentUser, login } from "./auth";
frontend/src/api/auth.test.ts:47:      const user = await login("admin@example.invalid", "secret-value");
frontend/src/api/auth.test.ts:52:        "/api/v1/auth/login",
frontend/src/api/auth.test.ts:75:    await expect(getCurrentUser()).rejects.toMatchObject({ status: 401 });
frontend/src/api/auth.ts:77:  const response = await fetch("/api/v1/auth/csrf", {
frontend/src/api/auth.ts:98:export async function getCurrentUser(): Promise<AuthUser> {
frontend/src/api/auth.ts:99:  const response = await fetch("/api/v1/auth/me", {
frontend/src/api/auth.ts:116:export async function login(email: string, password: string): Promise<AuthUser> {
frontend/src/api/auth.ts:118:  const response = await fetch("/api/v1/auth/login", {
frontend/src/api/auth.ts:141:export async function logout(): Promise<void> {
frontend/src/api/auth.ts:143:  const response = await fetch("/api/v1/auth/logout", {
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:69:        AuthenticatedSession session = login("security-admin@example.invalid", "security-test-password");
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:74:                url("/api/v1/auth/me"),
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:131:        AuthenticatedSession session = login("security-admin@example.invalid", "security-test-password");
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:140:                url("/api/v1/auth/logout"),
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:149:                url("/api/v1/auth/me"),
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:156:    private AuthenticatedSession login(String email, String password) {
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:178:                url("/api/v1/auth/login"),
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:190:                url("/api/v1/auth/csrf"),
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:239:            authenticatedSession = login("integration-admin@example.invalid", "integration-test-password");
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:244:    private AuthenticatedSession login(String email, String password) {
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:246:                url("/api/v1/auth/csrf"), JsonNode.class);
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:254:                url("/api/v1/auth/login"),
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:264:                url("/api/v1/auth/csrf"),
backend/src/main/java/br/com/facilit/kanban/delivery/auth/AuthRestController.java:49:    public AuthResponse login(
backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java:69:                        .requestMatchers(HttpMethod.GET, "/api/v1/health", "/api/v1/auth/csrf")
backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java:71:                        .requestMatchers("/api/v1/auth/login")
backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java:99:                .logout(logout -> logout
backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java:100:                        .logoutUrl("/api/v1/auth/logout")
```

`[VERIFICADO]` Nenhum consumidor backend foi alterado. A correção ficou restrita ao frontend/testes/documentação do F2-L2.
