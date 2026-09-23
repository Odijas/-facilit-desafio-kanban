# F2-L3 — CONSUMIDORES MAPEADOS

Data: 2026-09-22

Busca executada após a correção estrutural final:

```text
=== APP COMPOSITION ===
frontend/src/main.tsx:5:import { App } from "./App";
frontend/src/main.tsx:21:        <App />
frontend/src/App.test.tsx:5:import { App } from "./App";
frontend/src/App.test.tsx:52:      <App />

=== NAVIGATION ===
frontend/src/features/dashboard/DashboardPage.tsx:36:      navigate("/login", true);
frontend/src/features/auth/LoginPage.tsx:37:      navigate("/", true);
frontend/src/features/auth/ProtectedHome.tsx:18:  const redirectToLogin = useCallback(() => navigate("/login", true), []);
frontend/src/App.tsx:1:import { usePathname } from "./app/navigation";
frontend/src/App.tsx:7:  const pathname = usePathname();
frontend/src/pages/NotFoundPage.tsx:11:        <Button onClick={() => navigate("/", true)} variant="contained">
frontend/src/app/navigation.ts:8:export function usePathname(): string {
frontend/src/app/navigation.ts:16:export function navigate(pathname: string, replace = false): void {

=== KANBAN BOARD ===
frontend/src/features/kanban/KanbanBoard.test.tsx:17:import { KanbanBoard } from "./KanbanBoard";
frontend/src/features/kanban/KanbanBoard.test.tsx:85:      <KanbanBoard />
frontend/src/features/kanban/KanbanBoard.test.tsx:101:describe("KanbanBoard", () => {
frontend/src/features/kanban/KanbanBoard.tsx:59:export function KanbanBoard() {
frontend/src/features/dashboard/DashboardPage.tsx:19:import { KanbanBoard } from "../kanban/KanbanBoard";
frontend/src/features/dashboard/DashboardPage.tsx:99:          <KanbanBoard />
frontend/src/App.test.tsx:32:vi.mock("./features/kanban/KanbanBoard", () => ({
frontend/src/App.test.tsx:33:  KanbanBoard: () => <div>Quadro Kanban</div>,

=== KANBAN API ===
frontend/src/features/kanban/KanbanBoard.test.tsx:6:  createProject,
frontend/src/features/kanban/KanbanBoard.test.tsx:7:  createResponsible,
frontend/src/features/kanban/KanbanBoard.test.tsx:8:  deleteProject,
frontend/src/features/kanban/KanbanBoard.test.tsx:10:  listProjects,
frontend/src/features/kanban/KanbanBoard.test.tsx:11:  listResponsibles,
frontend/src/features/kanban/KanbanBoard.test.tsx:14:  transitionProject,
frontend/src/features/kanban/KanbanBoard.test.tsx:15:  updateProject,
frontend/src/features/kanban/KanbanBoard.test.tsx:32:    listProjects: vi.fn(),
frontend/src/features/kanban/KanbanBoard.test.tsx:33:    listResponsibles: vi.fn(),
frontend/src/features/kanban/KanbanBoard.test.tsx:34:    createProject: vi.fn(),
frontend/src/features/kanban/KanbanBoard.test.tsx:35:    updateProject: vi.fn(),
frontend/src/features/kanban/KanbanBoard.test.tsx:36:    transitionProject: vi.fn(),
frontend/src/features/kanban/KanbanBoard.test.tsx:37:    deleteProject: vi.fn(),
frontend/src/features/kanban/KanbanBoard.test.tsx:38:    createResponsible: vi.fn(),
frontend/src/features/kanban/KanbanBoard.test.tsx:42:const mockedListProjects = vi.mocked(listProjects);
frontend/src/features/kanban/KanbanBoard.test.tsx:43:const mockedListResponsibles = vi.mocked(listResponsibles);
frontend/src/features/kanban/KanbanBoard.test.tsx:44:const mockedCreateProject = vi.mocked(createProject);
frontend/src/features/kanban/KanbanBoard.test.tsx:45:const mockedUpdateProject = vi.mocked(updateProject);
frontend/src/features/kanban/KanbanBoard.test.tsx:46:const mockedTransitionProject = vi.mocked(transitionProject);
frontend/src/features/kanban/KanbanBoard.test.tsx:47:const mockedDeleteProject = vi.mocked(deleteProject);
frontend/src/features/kanban/KanbanBoard.test.tsx:48:const mockedCreateResponsible = vi.mocked(createResponsible);
frontend/src/features/kanban/KanbanBoard.tsx:12:  createProject,
frontend/src/features/kanban/KanbanBoard.tsx:13:  createResponsible,
frontend/src/features/kanban/KanbanBoard.tsx:14:  deleteProject,
frontend/src/features/kanban/KanbanBoard.tsx:16:  listProjects,
frontend/src/features/kanban/KanbanBoard.tsx:17:  listResponsibles,
frontend/src/features/kanban/KanbanBoard.tsx:22:  transitionProject,
frontend/src/features/kanban/KanbanBoard.tsx:23:  updateProject,
frontend/src/features/kanban/KanbanBoard.tsx:69:    queryFn: () => listProjects(),
frontend/src/features/kanban/KanbanBoard.tsx:74:    queryFn: () => listResponsibles(),
frontend/src/features/kanban/KanbanBoard.tsx:91:    mutationFn: (input: ProjectInput) => createProject(input),
frontend/src/features/kanban/KanbanBoard.tsx:102:      updateProject(projectId, input),
frontend/src/features/kanban/KanbanBoard.tsx:114:      transitionProject(projectId, status),
frontend/src/features/kanban/KanbanBoard.tsx:121:    mutationFn: (projectId: string) => deleteProject(projectId),
frontend/src/features/kanban/KanbanBoard.tsx:131:    mutationFn: (input: ResponsibleInput) => createResponsible(input),
frontend/src/api/kanban.ts:237:export function listProjects(): Promise<Project[]> {
frontend/src/api/kanban.ts:241:export function listResponsibles(): Promise<Responsible[]> {
frontend/src/api/kanban.ts:245:export async function createProject(input: ProjectInput): Promise<Project> {
frontend/src/api/kanban.ts:250:export async function updateProject(
frontend/src/api/kanban.ts:258:export async function transitionProject(
frontend/src/api/kanban.ts:268:export async function deleteProject(projectId: string): Promise<void> {
frontend/src/api/kanban.ts:272:export async function createResponsible(
frontend/src/api/kanban.test.ts:3:import { listProjects, transitionProject } from "./kanban";
frontend/src/api/kanban.test.ts:59:    const projects = await listProjects();
frontend/src/api/kanban.test.ts:79:      transitionProject(projectPayload.id, "IN_PROGRESS"),

=== AUTH API ===
frontend/src/features/dashboard/DashboardPage.tsx:33:    mutationFn: () => logout(),
frontend/src/features/auth/LoginPage.tsx:34:      login(loginEmail, loginPassword),
frontend/src/features/auth/ProtectedHome.tsx:5:import { AuthApiError, getCurrentUser } from "../../api/auth";
frontend/src/features/auth/ProtectedHome.tsx:13:    queryFn: () => getCurrentUser(),
frontend/src/App.test.tsx:6:import { AuthApiError, getCurrentUser, login, logout } from "./api/auth";
frontend/src/App.test.tsx:22:    getCurrentUser: vi.fn(),
frontend/src/App.test.tsx:36:const mockedGetCurrentUser = vi.mocked(getCurrentUser);
frontend/src/api/auth.test.ts:2:import { getCurrentUser, login } from "./auth";
frontend/src/api/auth.test.ts:47:      const user = await login("admin@example.invalid", "secret-value");
frontend/src/api/auth.test.ts:75:    await expect(getCurrentUser()).rejects.toMatchObject({ status: 401 });
frontend/src/api/auth.ts:97:export async function getCurrentUser(): Promise<AuthUser> {
frontend/src/api/auth.ts:115:export async function login(email: string, password: string): Promise<AuthUser> {
frontend/src/api/auth.ts:140:export async function logout(): Promise<void> {
```

Conclusão: `App` é consumido apenas pelo entrypoint e pelo teste de integração de UI; navegação, autenticação e Kanban têm consumidores explícitos mapeados acima. A camada `api` continua consumida pelos features/testes sem receber diretamente callbacks/contextos do React Query.
