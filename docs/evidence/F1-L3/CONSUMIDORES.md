# F1-L3 — CONSUMIDORES MAPEADOS

Data: 2026-09-22

Buscas executadas no worktree do candidato:

```text
### error contract consumers
backend/src/main/java/br/com/facilit/kanban/delivery/common/ApiErrorCode.java:3:public enum ApiErrorCode {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:5:import br.com.facilit.kanban.delivery.common.ApiErrorCode;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:18:public class RestExceptionHandler {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:22:        return problem(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, exception.getMessage());
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:27:        return problem(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, exception.getMessage());
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:32:        return problem(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_REQUEST, exception.getMessage());
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:39:                ApiErrorCode.VALIDATION_ERROR,
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:55:                ApiErrorCode.INVALID_REQUEST,
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:61:        return problem(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_REQUEST, "Malformed request body");
backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java:64:    private static ProblemDetail problem(HttpStatus status, ApiErrorCode code, String detail) {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:5:import br.com.facilit.kanban.delivery.common.ApiErrorCode;
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:16:public class GraphQlErrorHandler {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:25:                .extensions(code(ApiErrorCode.RESOURCE_NOT_FOUND))
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:36:                .extensions(code(ApiErrorCode.CONFLICT))
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:47:                .extensions(code(ApiErrorCode.INVALID_REQUEST))
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:58:                .extensions(code(ApiErrorCode.INVALID_REQUEST))
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:69:                .extensions(code(ApiErrorCode.VALIDATION_ERROR))
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java:73:    private static Map<String, Object> code(ApiErrorCode code) {
### REST/GraphQL controller references
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleRestController.java:32:public class ResponsibleRestController {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleRestController.java:36:    public ResponsibleRestController(ResponsibleService service) {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:34:public class ProjectRestController {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:38:    public ProjectRestController(ProjectService service) {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ResponsibleGraphQlController.java:19:public class ResponsibleGraphQlController {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ResponsibleGraphQlController.java:23:    public ResponsibleGraphQlController(ResponsibleService service) {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:23:public class ProjectGraphQlController {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:27:    public ProjectGraphQlController(ProjectService service) {
### list ordering/filter persistence
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/responsible/ResponsiblePersistenceAdapter.java:34:        PageRequest pageable = PageRequest.of(
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/responsible/ResponsiblePersistenceAdapter.java:37:                Sort.by("name").ascending().and(Sort.by("id").ascending()));
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:56:    public PageResult<Project> findByStatus(ProjectStatus status, PageQuery pageQuery) {
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:57:        return toPageResult(repository.findByStatus(status, pageable(pageQuery)));
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:113:        return PageRequest.of(
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:116:                Sort.by("name").ascending().and(Sort.by("id").ascending()));
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaRepository.java:25:    Page<ProjectJpaEntity> findByStatus(ProjectStatus status, Pageable pageable);
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:73:        return projectRepository.findByStatus(status, pageQuery);
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectRepository.java:16:    PageResult<Project> findByStatus(ProjectStatus status, PageQuery pageQuery);
backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java:43:    public PageResult<Project> findByStatus(ProjectStatus status, PageQuery pageQuery) {
```

Não foi alterada assinatura de `ProjectService`/`ResponsibleService` neste lote. As alterações observáveis estão nas fronteiras REST/GraphQL, contratos de erro, documentação OpenAPI e estratégia de teste/índices.
