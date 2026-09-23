# F3-L1 — CONSUMIDORES

Data: 2026-09-22

Busca executada após as alterações:

```text
### Backend repositories/services/controllers
backend/src/main/resources/graphql/kanban.graphqls:15:  projectIndicators: ProjectIndicators!
backend/src/main/resources/graphql/kanban.graphqls:119:type ProjectIndicators {
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:7:import br.com.facilit.kanban.application.secretariat.SecretariatRepository;
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:8:import br.com.facilit.kanban.application.secretariat.SecretariatService;
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:36:            SecretariatRepository secretariatRepository,
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:47:    SecretariatService secretariatService(
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:48:            SecretariatRepository secretariatRepository,
backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java:51:        return new SecretariatService(secretariatRepository, responsibleRepository, applicationClock);
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/responsible/ResponsiblePersistenceAdapter.java:89:    public boolean existsBySecretariatId(UUID secretariatId) {
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/responsible/ResponsiblePersistenceAdapter.java:90:        return repository.existsBySecretariatId(secretariatId);
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/responsible/ResponsibleJpaRepository.java:15:    boolean existsBySecretariatId(UUID secretariatId);
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/secretariat/SecretariatPersistenceAdapter.java:5:import br.com.facilit.kanban.application.secretariat.SecretariatRepository;
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/secretariat/SecretariatPersistenceAdapter.java:16:public class SecretariatPersistenceAdapter implements SecretariatRepository {
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:6:import br.com.facilit.kanban.application.project.ProjectFilter;
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:7:import br.com.facilit.kanban.application.project.ProjectIndicators;
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:66:    public PageResult<Project> search(ProjectFilter filter, PageQuery pageQuery) {
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:79:    public ProjectIndicators indicators() {
backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java:98:        return new ProjectIndicators(
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:5:import br.com.facilit.kanban.application.project.ProjectFilter;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:121:        ProjectFilter filter = new ProjectFilter(
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsResponse.java:3:import br.com.facilit.kanban.application.project.ProjectIndicators;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsResponse.java:6:public record ProjectIndicatorsResponse(
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsResponse.java:11:    static ProjectIndicatorsResponse from(ProjectIndicators indicators) {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsResponse.java:12:        return new ProjectIndicatorsResponse(
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsRestController.java:13:public class ProjectIndicatorsRestController {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsRestController.java:17:    public ProjectIndicatorsRestController(ProjectService service) {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsRestController.java:23:    public ProjectIndicatorsResponse get() {
backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsRestController.java:24:        return ProjectIndicatorsResponse.from(service.indicators());
backend/src/main/java/br/com/facilit/kanban/delivery/rest/SecretariatRestController.java:6:import br.com.facilit.kanban.application.secretariat.SecretariatService;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/SecretariatRestController.java:29:    private final SecretariatService service;
backend/src/main/java/br/com/facilit/kanban/delivery/rest/SecretariatRestController.java:31:    public SecretariatRestController(SecretariatService service) {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:5:import br.com.facilit.kanban.application.project.ProjectFilter;
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:6:import br.com.facilit.kanban.application.project.ProjectIndicators;
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:49:        ProjectFilter filter = new ProjectFilter(
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:70:    public ProjectIndicatorsGraphQlResponse projectIndicators() {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:71:        return ProjectIndicatorsGraphQlResponse.from(service.indicators());
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:181:    public record ProjectIndicatorsGraphQlResponse(
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:186:        static ProjectIndicatorsGraphQlResponse from(ProjectIndicators indicators) {
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java:187:            return new ProjectIndicatorsGraphQlResponse(
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/SecretariatGraphQlController.java:6:import br.com.facilit.kanban.application.secretariat.SecretariatService;
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/SecretariatGraphQlController.java:20:    private final SecretariatService service;
backend/src/main/java/br/com/facilit/kanban/delivery/graphql/SecretariatGraphQlController.java:22:    public SecretariatGraphQlController(SecretariatService service) {
backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleService.java:8:import br.com.facilit.kanban.application.secretariat.SecretariatRepository;
backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleService.java:19:    private final SecretariatRepository secretariatRepository;
backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleService.java:25:            SecretariatRepository secretariatRepository,
backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleRepository.java:26:    boolean existsBySecretariatId(UUID secretariatId);
backend/src/main/java/br/com/facilit/kanban/application/secretariat/SecretariatService.java:15:public final class SecretariatService {
backend/src/main/java/br/com/facilit/kanban/application/secretariat/SecretariatService.java:17:    private final SecretariatRepository secretariatRepository;
backend/src/main/java/br/com/facilit/kanban/application/secretariat/SecretariatService.java:21:    public SecretariatService(
backend/src/main/java/br/com/facilit/kanban/application/secretariat/SecretariatService.java:22:            SecretariatRepository secretariatRepository,
backend/src/main/java/br/com/facilit/kanban/application/secretariat/SecretariatService.java:63:        if (responsibleRepository.existsBySecretariatId(current.id())) {
backend/src/main/java/br/com/facilit/kanban/application/secretariat/SecretariatRepository.java:9:public interface SecretariatRepository {
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:76:    public PageResult<Project> search(ProjectFilter filter, PageQuery pageQuery) {
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java:82:    public ProjectIndicators indicators() {
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectFilter.java:7:public record ProjectFilter(
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectFilter.java:15:    public ProjectFilter {
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectFilter.java:22:    public static ProjectFilter empty() {
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectFilter.java:23:        return new ProjectFilter(null, null, null, null, null, null);
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectRepository.java:18:    PageResult<Project> search(ProjectFilter filter, PageQuery pageQuery);
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectRepository.java:20:    ProjectIndicators indicators();
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectIndicators.java:6:public record ProjectIndicators(
backend/src/main/java/br/com/facilit/kanban/application/project/ProjectIndicators.java:11:    public ProjectIndicators {
backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java:11:import br.com.facilit.kanban.application.support.InMemorySecretariatRepository;
backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java:31:    private InMemorySecretariatRepository secretariatRepository;
backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java:38:        secretariatRepository = new InMemorySecretariatRepository();
backend/src/test/java/br/com/facilit/kanban/application/secretariat/SecretariatServiceTest.java:10:import br.com.facilit.kanban.application.support.InMemorySecretariatRepository;
backend/src/test/java/br/com/facilit/kanban/application/secretariat/SecretariatServiceTest.java:20:class SecretariatServiceTest {
backend/src/test/java/br/com/facilit/kanban/application/secretariat/SecretariatServiceTest.java:23:    private InMemorySecretariatRepository secretariatRepository;
backend/src/test/java/br/com/facilit/kanban/application/secretariat/SecretariatServiceTest.java:25:    private SecretariatService service;
backend/src/test/java/br/com/facilit/kanban/application/secretariat/SecretariatServiceTest.java:29:        secretariatRepository = new InMemorySecretariatRepository();
backend/src/test/java/br/com/facilit/kanban/application/secretariat/SecretariatServiceTest.java:31:        service = new SecretariatService(
backend/src/test/java/br/com/facilit/kanban/application/support/InMemorySecretariatRepository.java:5:import br.com.facilit.kanban.application.secretariat.SecretariatRepository;
backend/src/test/java/br/com/facilit/kanban/application/support/InMemorySecretariatRepository.java:14:public final class InMemorySecretariatRepository implements SecretariatRepository {
backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java:5:import br.com.facilit.kanban.application.project.ProjectFilter;
backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java:6:import br.com.facilit.kanban.application.project.ProjectIndicators;
backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java:73:    public PageResult<Project> search(ProjectFilter filter, PageQuery pageQuery) {
backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java:106:    public ProjectIndicators indicators() {
backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java:121:        return new ProjectIndicators(values.size(), delayed, summaries);
backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryResponsibleRepository.java:70:    public boolean existsBySecretariatId(UUID secretariatId) {
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:93:                new ProjectFilter(
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:103:        ProjectIndicators indicators = service.indicators();
backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java:115:        assertThatThrownBy(() -> new ProjectFilter(

### Frontend API consumers
frontend/src/api/kanban.test.ts:3:import { getProjectIndicators, listProjects, transitionProject } from "./kanban";
frontend/src/api/kanban.test.ts:59:    const projects = await listProjects();
frontend/src/api/kanban.test.ts:78:    await listProjects({
frontend/src/api/kanban.test.ts:105:    const indicators = await getProjectIndicators();
frontend/src/api/kanban.ts:355:export function listProjects(filters: ProjectFilters = {}): Promise<Project[]> {
frontend/src/api/kanban.ts:365:export function listSecretariats(): Promise<Secretariat[]> {
frontend/src/api/kanban.ts:369:export async function getProjectIndicators(): Promise<ProjectIndicators> {
frontend/src/api/kanban.ts:419:export async function createSecretariat(
frontend/src/api/kanban.ts:426:export async function updateSecretariat(
frontend/src/api/kanban.ts:438:export async function deleteSecretariat(secretariatId: string): Promise<void> {
frontend/src/features/indicators/ProjectIndicatorsPanel.test.tsx:4:import { getProjectIndicators } from "../../api/kanban";
frontend/src/features/indicators/ProjectIndicatorsPanel.test.tsx:8:  getProjectIndicators: vi.fn(),
frontend/src/features/indicators/ProjectIndicatorsPanel.test.tsx:11:const mockedGetProjectIndicators = vi.mocked(getProjectIndicators);
frontend/src/features/indicators/ProjectIndicatorsPanel.tsx:4:  getProjectIndicators,
frontend/src/features/indicators/ProjectIndicatorsPanel.tsx:19:    queryFn: () => getProjectIndicators(),
frontend/src/features/kanban/KanbanBoard.tsx:16:  listProjects,
frontend/src/features/kanban/KanbanBoard.tsx:18:  listSecretariats,
frontend/src/features/kanban/KanbanBoard.tsx:76:    queryFn: () => listProjects(filters),
frontend/src/features/kanban/KanbanBoard.tsx:86:    queryFn: () => listSecretariats(),
frontend/src/features/kanban/KanbanBoard.test.tsx:10:  listProjects,
frontend/src/features/kanban/KanbanBoard.test.tsx:12:  listSecretariats,
frontend/src/features/kanban/KanbanBoard.test.tsx:34:    listProjects: vi.fn(),
frontend/src/features/kanban/KanbanBoard.test.tsx:36:    listSecretariats: vi.fn(),
frontend/src/features/kanban/KanbanBoard.test.tsx:45:const mockedListProjects = vi.mocked(listProjects);
frontend/src/features/kanban/KanbanBoard.test.tsx:47:const mockedListSecretariats = vi.mocked(listSecretariats);
frontend/src/features/secretariats/SecretariatPanel.test.tsx:6:  createSecretariat,
frontend/src/features/secretariats/SecretariatPanel.test.tsx:7:  deleteSecretariat,
frontend/src/features/secretariats/SecretariatPanel.test.tsx:8:  listSecretariats,
frontend/src/features/secretariats/SecretariatPanel.test.tsx:10:  updateSecretariat,
frontend/src/features/secretariats/SecretariatPanel.test.tsx:23:  listSecretariats: vi.fn(),
frontend/src/features/secretariats/SecretariatPanel.test.tsx:24:  createSecretariat: vi.fn(),
frontend/src/features/secretariats/SecretariatPanel.test.tsx:25:  updateSecretariat: vi.fn(),
frontend/src/features/secretariats/SecretariatPanel.test.tsx:26:  deleteSecretariat: vi.fn(),
frontend/src/features/secretariats/SecretariatPanel.test.tsx:29:const mockedListSecretariats = vi.mocked(listSecretariats);
frontend/src/features/secretariats/SecretariatPanel.test.tsx:30:const mockedCreateSecretariat = vi.mocked(createSecretariat);
frontend/src/features/secretariats/SecretariatPanel.test.tsx:31:const mockedUpdateSecretariat = vi.mocked(updateSecretariat);
frontend/src/features/secretariats/SecretariatPanel.test.tsx:32:const mockedDeleteSecretariat = vi.mocked(deleteSecretariat);
frontend/src/features/secretariats/SecretariatPanel.tsx:15:  createSecretariat,
frontend/src/features/secretariats/SecretariatPanel.tsx:16:  deleteSecretariat,
frontend/src/features/secretariats/SecretariatPanel.tsx:18:  listSecretariats,
frontend/src/features/secretariats/SecretariatPanel.tsx:21:  updateSecretariat,
frontend/src/features/secretariats/SecretariatPanel.tsx:50:    queryFn: () => listSecretariats(),
frontend/src/features/secretariats/SecretariatPanel.tsx:60:    mutationFn: (input: SecretariatInput) => createSecretariat(input),
frontend/src/features/secretariats/SecretariatPanel.tsx:71:      updateSecretariat(secretariatId, input),
frontend/src/features/secretariats/SecretariatPanel.tsx:82:    mutationFn: (secretariatId: string) => deleteSecretariat(secretariatId),
```

## Rev2 — 2026-09-22

```text
$ grep -rn "application\.responsible\.SecretariatRepository" backend/src
(sem saída)
$ grep -rn "SecretariatRepository" backend/src --include=*.java | grep "import\|implements"
InMemorySecretariatRepository.java:5 import ...application.secretariat.SecretariatRepository
InMemorySecretariatRepository.java:14 implements SecretariatRepository
ApplicationBeans.java:7 import ...application.secretariat.SecretariatRepository
SecretariatPersistenceAdapter.java:5 import ...application.secretariat.SecretariatRepository
SecretariatPersistenceAdapter.java:16 implements SecretariatRepository
ResponsibleService.java:8 import ...application.secretariat.SecretariatRepository
```

`KanbanBoard` não altera contrato público; consumidores: `DashboardPage.tsx` e `KanbanBoard.test.tsx` (sem mudança de props).

## Rev3 — 2026-09-22

```text
$ grep -rn "repository\.search(\|ProjectJpaRepository" backend/src/main backend/src/test (excluindo o próprio arquivo)
ProjectPersistenceAdapter.java:40,44,78,81,85 (único consumidor)
$ grep -rn "is null or" backend/src/main
(sem saída)
```

Porta `ProjectRepository.search(ProjectFilter, PageQuery)` inalterada; consumidores `ProjectService.search`, `ProjectRestController.list`, `ProjectGraphQlController.projects` e `InMemoryProjectRepository` sem mudança.
