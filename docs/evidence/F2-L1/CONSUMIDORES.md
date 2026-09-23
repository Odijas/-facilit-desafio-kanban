# F2-L1 — CONSUMIDORES MAPEADOS

Data: 2026-09-22

Busca executada no candidato antes de alterar o comportamento público:

```text
$ grep -RInE '(/api/v1/(projects|responsibles)|/graphql|/api/v1/health)' frontend backend/src/test backend/src/main/java
frontend/src/api/health.ts:6:  const response = await fetch("/api/v1/health", {
backend/src/test/java/br/com/facilit/kanban/delivery/rest/HealthRestControllerTest.java:25:        mockMvc.perform(get("/api/v1/health"))
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:52:                url("/api/v1/projects?page=0&size=20"), JsonNode.class);
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:85:                url("/api/v1/responsibles"),
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:105:                url("/api/v1/responsibles"),
backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java:115:                url("/api/v1/responsibles/" + responsibleId),
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:80:                "/api/v1/responsibles",
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:93:                "/api/v1/responsibles",
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:102:                "/api/v1/responsibles",
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:112:                "/api/v1/responsibles/" + UUID.randomUUID(), JsonNode.class);
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:117:        delete("/api/v1/responsibles/" + responsibleId, HttpStatus.NO_CONTENT);
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:124:                "/api/v1/responsibles",
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:134:                "/api/v1/projects",
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:152:                "/api/v1/projects?status=IN_PROGRESS&page=0&size=20", JsonNode.class);
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:173:        delete("/api/v1/projects/" + projectId, HttpStatus.NO_CONTENT);
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:174:        delete("/api/v1/responsibles/" + responsibleId, HttpStatus.NO_CONTENT);
backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java:196:                "/graphql",
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleRestController.java:30:@RequestMapping("/api/v1/responsibles")
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleRestController.java:86:        return ResponseEntity.created(URI.create("/api/v1/responsibles/" + created.id())).body(response);
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:32:@RequestMapping("/api/v1/projects")
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:96:        return ResponseEntity.created(URI.create("/api/v1/projects/" + created.id())).body(response);
backend/src/main/java/br/com/facilit/kanban/delivery/rest/HealthRestController.java:10:@RequestMapping("/api/v1/health")
backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java:69:                        .requestMatchers(HttpMethod.GET, "/api/v1/health", "/api/v1/auth/csrf")
backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java:80:                        .requestMatchers("/api/v1/**", "/graphql")
```

Busca dos contratos/símbolos de segurança:

```text
$ grep -RInE '(SecurityConfiguration|AuthRestController|ApiErrorCode|AuthenticationException)' backend/src/main backend/src/test
backend/src/main/java/br/com/facilit/kanban/delivery/common/ApiErrorCode.java:3:public enum ApiErrorCode {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:5:import br.com.facilit.kanban.delivery.common.ApiErrorCode;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:11:import org.springframework.security.core.AuthenticationException;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:23:        return problem(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, exception.getMessage());
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:28:        return problem(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, exception.getMessage());
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:31:    @ExceptionHandler(AuthenticationException.class)
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:32:    ProblemDetail handleAuthentication(AuthenticationException exception) {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:35:                ApiErrorCode.UNAUTHORIZED,
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:41:        return problem(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_REQUEST, exception.getMessage());
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:48:                ApiErrorCode.VALIDATION_ERROR,
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:64:                ApiErrorCode.INVALID_REQUEST,
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:70:        return problem(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_REQUEST, "Malformed request body");
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:73:    private static ProblemDetail problem(HttpStatus status, ApiErrorCode code, String detail) {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:5:import br.com.facilit.kanban.delivery.common.ApiErrorCode;
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:25:                .extensions(code(ApiErrorCode.RESOURCE_NOT_FOUND))
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:36:                .extensions(code(ApiErrorCode.CONFLICT))
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:47:                .extensions(code(ApiErrorCode.INVALID_REQUEST))
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:58:                .extensions(code(ApiErrorCode.INVALID_REQUEST))
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:69:                .extensions(code(ApiErrorCode.VALIDATION_ERROR))
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:73:    private static Map<String, Object> code(ApiErrorCode code) {
backend/src/main/java/br/com/facilit/kanban/delivery/auth/AuthRestController.java:27:public class AuthRestController {
backend/src/main/java/br/com/facilit/kanban/delivery/auth/AuthRestController.java:33:    public AuthRestController(
backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java:3:import br.com.facilit.kanban.delivery.common.ApiErrorCode;
backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java:27:public class SecurityConfiguration {
backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java:90:                                        ApiErrorCode.UNAUTHORIZED,
backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java:97:                                        ApiErrorCode.FORBIDDEN,
backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java:114:            ApiErrorCode code,
```

Impacto mapeado:

- `frontend/src/api/health.ts` continua válido porque health permanece público.
- Todos os consumidores de negócio de `KanbanApiIT` foram adaptados para login, sessão e CSRF.
- O slice `HealthRestControllerTest` desabilita filtros deliberadamente porque testa somente o controller; segurança é coberta no teste de integração real.
- GraphQL passou de público para autenticado; consumidor frontend de GraphQL ainda não existe na baseline F1.
