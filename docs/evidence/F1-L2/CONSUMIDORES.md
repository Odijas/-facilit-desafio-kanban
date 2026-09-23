# F1-L2 — CONSUMIDORES MAPEADOS

Data: 2026-09-22

Busca executada antes/depois da alteração pública em `ProjectRepository`, `ProjectService` e contratos REST/GraphQL.

```text
### ProjectRepository consumers
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:3:import br.com.facilit.kanban.application.project.ProjectRepository;
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:36:            ProjectRepository projectRepository,
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:47:            ProjectRepository projectRepository,
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:6:import br.com.facilit.kanban.application.project.ProjectRepository;
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:29:public class ProjectPersistenceAdapter implements ProjectRepository {
backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleService.java:7:import br.com.facilit.kanban.application.project.ProjectRepository;
backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleService.java:19:    private final ProjectRepository projectRepository;
backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleService.java:25:            ProjectRepository projectRepository,
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:24:    private final ProjectRepository projectRepository;
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:31:            ProjectRepository projectRepository,
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectRepository.java:10:public interface ProjectRepository {
backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java:5:import br.com.facilit.kanban.application.project.ProjectRepository;
backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java:15:public final class InMemoryProjectRepository implements ProjectRepository {
backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java:9:import br.com.facilit.kanban.application.support.InMemoryProjectRepository;
backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java:30:    private InMemoryProjectRepository projectRepository;
backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java:37:        projectRepository = new InMemoryProjectRepository();
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:8:import br.com.facilit.kanban.application.support.InMemoryProjectRepository;
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:28:    private InMemoryProjectRepository projectRepository;
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:35:        projectRepository = new InMemoryProjectRepository();
### ProjectService list/listByStatus/transition consumers
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleRestController.java:48:        PageResult<Responsible> result = service.list(new PageQuery(page, size));
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:53:                ? service.list(pageQuery)
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:54:                : service.listByStatus(status, pageQuery);
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:76:        return ProjectResponse.from(service.transition(id, request.status()));
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ResponsibleGraphQlController.java:31:        PageResult<Responsible> result = service.list(new PageQuery(page, size));
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:40:                ? service.list(pageQuery)
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:41:                : service.listByStatus(status, pageQuery);
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:70:        return ProjectGraphQlResponse.from(service.transition(UUID.fromString(id), status));
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:51:        return new ProjectService(
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:30:    public ProjectService(
backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java:53:        assertThat(service.list(new PageQuery(0, 20)).content()).containsExactly(created);
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:45:        service = new ProjectService(
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:59:        assertThat(service.list(new PageQuery(0, 20)).content()).containsExactly(created);
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:60:        assertThat(service.listByStatus(ProjectStatus.NOT_STARTED, new PageQuery(0, 20)).content())
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:62:        assertThat(service.listByStatus(ProjectStatus.IN_PROGRESS, new PageQuery(0, 20)).content())
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:69:        Project completed = service.transition(created.id(), ProjectStatus.COMPLETED);
### project status delivery/schema consumers
backend/src/main/resources/graphql/kanban.graphqls:5:  projects(page: Int = 0, size: Int = 20, status: ProjectStatus): ProjectPage!
backend/src/main/resources/graphql/kanban.graphqls:14:  transitionProject(id: ID!, status: ProjectStatus!): Project!
backend/src/main/resources/graphql/kanban.graphqls:44:enum ProjectStatus {
backend/src/main/resources/graphql/kanban.graphqls:63:  status: ProjectStatus!
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:8:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:50:            @RequestParam(required = false) ProjectStatus status) {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:72:    @PatchMapping("/{id}/status")
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:75:            @Valid @RequestBody ProjectStatusRequest request) {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectStatusRequest.java:3:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectStatusRequest.java:6:public record ProjectStatusRequest(@NotNull ProjectStatus status) {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectResponse.java:4:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectResponse.java:13:        ProjectStatus status,
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:8:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:34:    public ProjectGraphQlPage projects(
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:37:            @Argument ProjectStatus status) {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:67:    public ProjectGraphQlResponse transitionProject(
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:69:            @Argument ProjectStatus status) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatus.java:3:public enum ProjectStatus {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleMetrics.java:6:        ProjectStatus status,
backend/src/main/java/br/com/facilit/kanban/domain/project/Project.java:31:    public ProjectStatus status() {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java:13:        ProjectStatus status = calculateStatus(dates, today);
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java:20:    private ProjectStatus calculateStatus(ProjectDates dates, LocalDate today) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java:22:            return ProjectStatus.COMPLETED;
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java:31:            return ProjectStatus.OVERDUE;
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java:39:            return ProjectStatus.IN_PROGRESS;
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java:42:        return ProjectStatus.NOT_STARTED;
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java:45:    private long calculateDelayDays(ProjectDates dates, ProjectStatus status, LocalDate today) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java:46:        if (status == ProjectStatus.COMPLETED
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java:57:            ProjectStatus status,
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java:59:        if (status == ProjectStatus.COMPLETED
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:6:public final class ProjectStatusTransition {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:10:    public ProjectStatusTransition(ProjectScheduleCalculator scheduleCalculator) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:16:            ProjectStatus requestedStatus,
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:22:        ProjectStatus currentStatus = project.status();
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:46:            ProjectStatus currentStatus,
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:47:            ProjectStatus requestedStatus,
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:50:        if (currentStatus == ProjectStatus.NOT_STARTED
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:51:                && requestedStatus == ProjectStatus.OVERDUE
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:60:            ProjectStatus currentStatus,
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:61:            ProjectStatus requestedStatus,
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:64:        if (requestedStatus == ProjectStatus.COMPLETED
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:65:                && currentStatus != ProjectStatus.COMPLETED) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:69:        if (currentStatus == ProjectStatus.NOT_STARTED
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:70:                && requestedStatus == ProjectStatus.IN_PROGRESS) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:74:        if (currentStatus == ProjectStatus.IN_PROGRESS
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:75:                && requestedStatus == ProjectStatus.NOT_STARTED) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:79:        if (currentStatus == ProjectStatus.COMPLETED
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:80:                && (requestedStatus == ProjectStatus.IN_PROGRESS
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:81:                        || requestedStatus == ProjectStatus.OVERDUE)) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:105:            ProjectStatus currentStatus,
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:106:            ProjectStatus requestedStatus,
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:107:            ProjectStatus recalculatedStatus) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:108:        if (currentStatus == ProjectStatus.IN_PROGRESS
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:109:                && requestedStatus == ProjectStatus.OVERDUE) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:113:        if (currentStatus == ProjectStatus.OVERDUE
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:114:                && requestedStatus == ProjectStatus.NOT_STARTED) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:118:        if (currentStatus == ProjectStatus.OVERDUE
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:119:                && requestedStatus == ProjectStatus.IN_PROGRESS) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:123:        if (currentStatus == ProjectStatus.COMPLETED
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:124:                && requestedStatus == ProjectStatus.NOT_STARTED) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:128:        if (currentStatus == ProjectStatus.COMPLETED
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:129:                && requestedStatus == ProjectStatus.IN_PROGRESS) {
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:133:        if (currentStatus == ProjectStatus.COMPLETED
backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java:134:                && requestedStatus == ProjectStatus.OVERDUE) {
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:11:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:56:    public PageResult<Project> findByStatus(ProjectStatus status, PageQuery pageQuery) {
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaRepository.java:7:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaRepository.java:25:    Page<ProjectJpaEntity> findByStatus(ProjectStatus status, Pageable pageable);
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaEntity.java:3:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaEntity.java:33:    private ProjectStatus status;
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaEntity.java:72:            ProjectStatus status,
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaEntity.java:105:    public ProjectStatus getStatus() {
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:12:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:13:import br.com.facilit.kanban.domain.project.ProjectStatusTransition;
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:27:    private final ProjectStatusTransition statusTransition;
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:38:        this.statusTransition = new ProjectStatusTransition(scheduleCalculator);
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:70:    public PageResult<Project> listByStatus(ProjectStatus status, PageQuery pageQuery) {
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:93:    public Project transition(UUID id, ProjectStatus requestedStatus) {
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectRepository.java:6:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectRepository.java:16:    PageResult<Project> findByStatus(ProjectStatus status, PageQuery pageQuery);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectTest.java:17:        Project project = project(new ProjectScheduleMetrics(ProjectStatus.OVERDUE, 2, 0));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectTest.java:19:        assertThat(project.status()).isEqualTo(ProjectStatus.OVERDUE);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectTest.java:33:        assertThatThrownBy(() -> new ProjectScheduleMetrics(ProjectStatus.OVERDUE, -1, 0))
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectTest.java:36:        assertThatThrownBy(() -> new ProjectScheduleMetrics(ProjectStatus.NOT_STARTED, 0, 101))
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:13:class ProjectStatusTransitionTest {
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:18:    private final ProjectStatusTransition transition = new ProjectStatusTransition(calculator);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:22:        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, TODAY.plusDays(10), null, null));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:24:        ProjectTransitionResult result = transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:27:        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.IN_PROGRESS);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:32:        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY.minusDays(1), TODAY.plusDays(10), null, null));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:34:        ProjectTransitionResult result = transition.transition(project, ProjectStatus.OVERDUE, TODAY);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:36:        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.OVERDUE);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:41:        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY.plusDays(1), TODAY.plusDays(10), null, null));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:43:        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY))
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:50:        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, TODAY.plusDays(10), null, null));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:52:        ProjectTransitionResult result = transition.transition(project, ProjectStatus.COMPLETED, TODAY);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:55:        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.COMPLETED);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:60:        Project project = project(ProjectStatus.IN_PROGRESS, new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:62:        ProjectTransitionResult result = transition.transition(project, ProjectStatus.NOT_STARTED, TODAY);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:65:        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.NOT_STARTED);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:71:                ProjectStatus.IN_PROGRESS,
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:74:        ProjectTransitionResult result = transition.transition(project, ProjectStatus.OVERDUE, TODAY);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:76:        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.OVERDUE);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:81:        Project project = project(ProjectStatus.IN_PROGRESS, new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:83:        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY))
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:90:        Project project = project(ProjectStatus.IN_PROGRESS, new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:92:        ProjectTransitionResult result = transition.transition(project, ProjectStatus.COMPLETED, TODAY);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:95:        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.COMPLETED);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:100:        Project project = project(ProjectStatus.OVERDUE, new ProjectDates(TODAY.minusDays(2), TODAY.plusDays(10), null, null));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:102:        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY))
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:109:        Project project = project(ProjectStatus.OVERDUE, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), null));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:111:        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY))
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:118:        Project project = project(ProjectStatus.OVERDUE, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), null));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:120:        ProjectTransitionResult result = transition.transition(project, ProjectStatus.COMPLETED, TODAY);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:123:        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.COMPLETED);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:128:        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY, TODAY.plusDays(10), null, TODAY));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:130:        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY))
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:137:        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(1), TODAY.plusDays(10), TODAY.minusDays(1), TODAY));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:139:        ProjectTransitionResult result = transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:142:        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.IN_PROGRESS);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:147:        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), TODAY));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:149:        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.IN_PROGRESS, TODAY))
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:156:        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), TODAY));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:158:        ProjectTransitionResult result = transition.transition(project, ProjectStatus.OVERDUE, TODAY);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:161:        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.OVERDUE);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:166:        Project project = project(ProjectStatus.COMPLETED, new ProjectDates(TODAY.minusDays(1), TODAY.plusDays(10), TODAY.minusDays(1), TODAY));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:168:        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.OVERDUE, TODAY))
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:175:        Project project = project(ProjectStatus.NOT_STARTED, new ProjectDates(TODAY, TODAY.plusDays(10), null, null));
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:177:        assertThatThrownBy(() -> transition.transition(project, ProjectStatus.NOT_STARTED, TODAY))
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java:182:    private Project project(ProjectStatus sourceStatus, ProjectDates dates) {
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculatorTest.java:19:        assertThat(metrics.status()).isEqualTo(ProjectStatus.NOT_STARTED);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculatorTest.java:29:        assertThat(metrics.status()).isEqualTo(ProjectStatus.OVERDUE);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculatorTest.java:38:        assertThat(metrics.status()).isEqualTo(ProjectStatus.OVERDUE);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculatorTest.java:48:        assertThat(metrics.status()).isEqualTo(ProjectStatus.IN_PROGRESS);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculatorTest.java:57:        assertThat(metrics.status()).isEqualTo(ProjectStatus.IN_PROGRESS);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculatorTest.java:67:        assertThat(metrics.status()).isEqualTo(ProjectStatus.COMPLETED);
backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculatorTest.java:76:        assertThat(metrics.status()).isEqualTo(ProjectStatus.NOT_STARTED);
backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java:7:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java:43:    public PageResult<Project> findByStatus(ProjectStatus status, PageQuery pageQuery) {
backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java:16:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java:93:                new ProjectScheduleMetrics(ProjectStatus.NOT_STARTED, 0, 0),
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:13:import br.com.facilit.kanban.domain.project.ProjectStatus;
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:56:        assertThat(created.status()).isEqualTo(ProjectStatus.NOT_STARTED);
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:60:        assertThat(service.listByStatus(ProjectStatus.NOT_STARTED, new PageQuery(0, 20)).content())
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:62:        assertThat(service.listByStatus(ProjectStatus.IN_PROGRESS, new PageQuery(0, 20)).content())
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:66:        assertThat(updated.status()).isEqualTo(ProjectStatus.IN_PROGRESS);
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:69:        Project completed = service.transition(created.id(), ProjectStatus.COMPLETED);
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:70:        assertThat(completed.status()).isEqualTo(ProjectStatus.COMPLETED);
```

Conclusão: os dois implementadores de `ProjectRepository` (`ProjectPersistenceAdapter` e `InMemoryProjectRepository`) foram atualizados. Os consumidores REST, GraphQL, composition root e testes de aplicação foram lidos; não há consumidor conhecido omitido no escopo pesquisado.
