import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import {
  fireEvent,
  render,
  screen,
  waitFor,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  createProject,
  createResponsible,
  deleteProject,
  KanbanApiError,
  listProjects,
  listResponsibles,
  listSecretariats,
  type Project,
  type Responsible,
  type Secretariat,
  transitionProject,
  updateProject,
} from "../../api/kanban";
import { KanbanBoard } from "./KanbanBoard";

vi.mock("../../api/kanban", () => {
  class MockKanbanApiError extends Error {
    readonly status: number;

    constructor(message: string, status: number) {
      super(message);
      this.name = "KanbanApiError";
      this.status = status;
    }
  }

  return {
    KanbanApiError: MockKanbanApiError,
    listProjects: vi.fn(),
    listResponsibles: vi.fn(),
    listSecretariats: vi.fn(),
    createProject: vi.fn(),
    updateProject: vi.fn(),
    transitionProject: vi.fn(),
    deleteProject: vi.fn(),
    createResponsible: vi.fn(),
  };
});

const mockedListProjects = vi.mocked(listProjects);
const mockedListResponsibles = vi.mocked(listResponsibles);
const mockedListSecretariats = vi.mocked(listSecretariats);
const mockedCreateProject = vi.mocked(createProject);
const mockedUpdateProject = vi.mocked(updateProject);
const mockedTransitionProject = vi.mocked(transitionProject);
const mockedDeleteProject = vi.mocked(deleteProject);
const mockedCreateResponsible = vi.mocked(createResponsible);

const project: Project = {
  id: "30000000-0000-4000-8000-000000000001",
  name: "Portal cidadão",
  status: "NOT_STARTED",
  responsibleIds: ["20000000-0000-4000-8000-000000000001"],
  plannedStart: "2026-09-22",
  plannedEnd: "2026-10-10",
  actualStart: null,
  actualEnd: null,
  delayDays: 0,
  remainingTimePercentage: 100,
  createdAt: "2026-09-22T12:00:00Z",
  updatedAt: "2026-09-22T12:00:00Z",
};

const secretariat: Secretariat = {
  id: "10000000-0000-4000-8000-000000000001",
  name: "Secretaria Digital",
  createdAt: "2026-09-22T12:00:00Z",
  updatedAt: "2026-09-22T12:00:00Z",
};

const responsible: Responsible = {
  id: "20000000-0000-4000-8000-000000000001",
  name: "Maria Silva",
  email: "maria@example.com",
  position: "Analista",
  secretariatId: null,
  createdAt: "2026-09-22T12:00:00Z",
  updatedAt: "2026-09-22T12:00:00Z",
};

function renderBoard() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <KanbanBoard />
    </QueryClientProvider>,
  );
}

function createDataTransfer() {
  let value = "";
  return {
    effectAllowed: "all",
    setData: vi.fn((_format: string, nextValue: string) => {
      value = nextValue;
    }),
    getData: vi.fn(() => value),
  };
}

describe("KanbanBoard", () => {
  beforeEach(() => {
    mockedListProjects.mockReset();
    mockedListResponsibles.mockReset();
    mockedListSecretariats.mockReset();
    mockedCreateProject.mockReset();
    mockedUpdateProject.mockReset();
    mockedTransitionProject.mockReset();
    mockedDeleteProject.mockReset();
    mockedCreateResponsible.mockReset();

    mockedListProjects.mockResolvedValue([project]);
    mockedListResponsibles.mockResolvedValue([responsible]);
    mockedListSecretariats.mockResolvedValue([secretariat]);
    mockedCreateProject.mockResolvedValue(project);
    mockedUpdateProject.mockResolvedValue(project);
    mockedTransitionProject.mockResolvedValue({
      ...project,
      status: "IN_PROGRESS",
    });
    mockedDeleteProject.mockResolvedValue();
    mockedCreateResponsible.mockResolvedValue(responsible);
  });

  it("renders the four Kanban columns and project responsibility", async () => {
    renderBoard();

    expect(await screen.findByText("Portal cidadão")).toBeInTheDocument();
    expect(screen.getByText("Maria Silva")).toBeInTheDocument();
    expect(
      screen.getByRole("region", { name: "Coluna A iniciar" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("region", { name: "Coluna Em andamento" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("region", { name: "Coluna Atrasado" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("region", { name: "Coluna Concluído" }),
    ).toBeInTheDocument();
  });

  it("requests projects with advanced text filtering", async () => {
    const user = userEvent.setup();
    renderBoard();

    await screen.findByText("Portal cidadão");
    await user.type(screen.getByLabelText("Buscar projeto"), "Portal");

    await waitFor(() => {
      expect(mockedListProjects).toHaveBeenLastCalledWith({ text: "Portal" });
    });
  });

  it("requests the backend transition when a project is dropped in another column", async () => {
    renderBoard();
    const transfer = createDataTransfer();
    const card = await screen.findByRole("article", {
      name: "Projeto Portal cidadão",
    });

    fireEvent.dragStart(card, { dataTransfer: transfer });
    fireEvent.dragOver(
      screen.getByRole("region", { name: "Coluna Em andamento" }),
      {
        dataTransfer: transfer,
      },
    );
    fireEvent.drop(
      screen.getByRole("region", { name: "Coluna Em andamento" }),
      {
        dataTransfer: transfer,
      },
    );

    await waitFor(() => {
      expect(mockedTransitionProject).toHaveBeenCalledWith(
        project.id,
        "IN_PROGRESS",
      );
    });
  });

  it("shows the backend domain message when a transition is blocked", async () => {
    mockedTransitionProject.mockRejectedValue(
      new KanbanApiError("Ajuste as datas previstas antes da transição", 400),
    );
    renderBoard();
    const transfer = createDataTransfer();
    const card = await screen.findByRole("article", {
      name: "Projeto Portal cidadão",
    });

    fireEvent.dragStart(card, { dataTransfer: transfer });
    fireEvent.drop(
      screen.getByRole("region", { name: "Coluna Em andamento" }),
      {
        dataTransfer: transfer,
      },
    );

    expect(
      await screen.findByText("Ajuste as datas previstas antes da transição"),
    ).toBeInTheDocument();
  });

  it("creates a project only after selecting a responsible", async () => {
    const user = userEvent.setup();
    renderBoard();

    await screen.findByText("Portal cidadão");
    await user.click(screen.getByRole("button", { name: "Novo projeto" }));
    await user.type(screen.getByLabelText(/Nome do projeto/), "Novo serviço");

    const saveButton = screen.getByRole("button", { name: "Salvar" });
    expect(saveButton).toBeDisabled();

    await user.click(screen.getByRole("checkbox", { name: /Maria Silva/ }));
    expect(saveButton).toBeEnabled();
    await user.click(saveButton);

    await waitFor(() => {
      expect(mockedCreateProject).toHaveBeenCalledWith({
        name: "Novo serviço",
        responsibleIds: [responsible.id],
        plannedStart: null,
        plannedEnd: null,
        actualStart: null,
        actualEnd: null,
      });
    });
  });

  it("updates an existing project from the edit form", async () => {
    const user = userEvent.setup();
    renderBoard();

    await screen.findByText("Portal cidadão");
    await user.click(screen.getByRole("button", { name: "Editar" }));

    const nameField = screen.getByLabelText(/Nome do projeto/);
    await user.clear(nameField);
    await user.type(nameField, "Portal atualizado");
    await user.click(screen.getByRole("button", { name: "Salvar" }));

    await waitFor(() => {
      expect(mockedUpdateProject).toHaveBeenCalledWith(project.id, {
        name: "Portal atualizado",
        responsibleIds: [responsible.id],
        plannedStart: "2026-09-22",
        plannedEnd: "2026-10-10",
        actualStart: null,
        actualEnd: null,
      });
    });
  });

  it("confirms project deletion before calling the API", async () => {
    const user = userEvent.setup();
    renderBoard();

    await screen.findByText("Portal cidadão");
    await user.click(screen.getByRole("button", { name: "Excluir" }));

    const dialog = screen.getByRole("dialog", { name: "Excluir projeto?" });
    await user.click(within(dialog).getByRole("button", { name: "Excluir" }));

    await waitFor(() => {
      expect(mockedDeleteProject).toHaveBeenCalledWith(project.id);
    });
  });

  it("creates a responsible from the board", async () => {
    const user = userEvent.setup();
    renderBoard();

    await screen.findByText("Portal cidadão");
    await user.click(screen.getByRole("button", { name: "Novo responsável" }));
    await user.type(screen.getByLabelText(/Nome/), "João Souza");
    await user.type(screen.getByLabelText(/E-mail/), "joao@example.com");
    await user.type(screen.getByLabelText(/Cargo/), "Gestor");
    const dialog = screen.getByRole("dialog", { name: "Novo responsável" });
    await user.click(within(dialog).getByLabelText("Secretaria"));
    await user.click(
      screen.getByRole("option", { name: "Secretaria Digital" }),
    );
    await user.click(screen.getByRole("button", { name: "Salvar" }));

    await waitFor(() => {
      expect(mockedCreateResponsible).toHaveBeenCalledWith({
        name: "João Souza",
        email: "joao@example.com",
        position: "Gestor",
        secretariatId: secretariat.id,
      });
    });
  });

  it("shows a recoverable error when Kanban data cannot be loaded", async () => {
    mockedListProjects.mockRejectedValue(new Error("offline"));
    mockedListResponsibles.mockResolvedValue([responsible]);
    mockedListSecretariats.mockResolvedValue([secretariat]);

    renderBoard();

    expect(
      await screen.findByText("Não foi possível carregar o Kanban."),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Tentar novamente" }),
    ).toBeInTheDocument();
  });

  it("shows a loading state while Kanban data is pending", () => {
    mockedListProjects.mockImplementation(
      () => new Promise<never>(() => undefined),
    );
    mockedListResponsibles.mockImplementation(
      () => new Promise<never>(() => undefined),
    );

    renderBoard();

    expect(screen.getByText("Carregando quadro Kanban")).toBeInTheDocument();
  });
});
