import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { App } from "./App";
import { AuthApiError, getCurrentUser, login, logout } from "./api/auth";
import { getHealthStatus } from "./api/health";

vi.mock("./api/auth", () => {
  class MockAuthApiError extends Error {
    readonly status: number;

    constructor(message: string, status: number) {
      super(message);
      this.name = "AuthApiError";
      this.status = status;
    }
  }

  return {
    AuthApiError: MockAuthApiError,
    getCurrentUser: vi.fn(),
    login: vi.fn(),
    logout: vi.fn(),
  };
});

vi.mock("./api/health", () => ({
  getHealthStatus: vi.fn(),
}));

vi.mock("./features/kanban/KanbanBoard", () => ({
  KanbanBoard: () => <div>Quadro Kanban</div>,
}));

vi.mock("./features/indicators/ProjectIndicatorsPanel", () => ({
  ProjectIndicatorsPanel: () => <div>Indicadores</div>,
}));

vi.mock("./features/secretariats/SecretariatPanel", () => ({
  SecretariatPanel: () => <div>Secretarias</div>,
}));

const mockedGetCurrentUser = vi.mocked(getCurrentUser);
const mockedLogin = vi.mocked(login);
const mockedLogout = vi.mocked(logout);
const mockedGetHealthStatus = vi.mocked(getHealthStatus);

function renderApp() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>,
  );
}

describe("App", () => {
  beforeEach(() => {
    window.history.replaceState(null, "", "/");
    mockedGetCurrentUser.mockReset();
    mockedLogin.mockReset();
    mockedLogout.mockReset();
    mockedGetHealthStatus.mockReset();
    mockedGetHealthStatus.mockResolvedValue({ status: "UP" });
  });

  it("redirects an anonymous user to the login page", async () => {
    mockedGetCurrentUser.mockRejectedValue(
      new AuthApiError("Authentication required", 401),
    );

    renderApp();

    expect(
      await screen.findByRole("heading", { name: "Facilit Kanban" }),
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/E-mail/)).toBeInTheDocument();
    expect(window.location.pathname).toBe("/login");
  });

  it("authenticates and opens the protected dashboard", async () => {
    window.history.replaceState(null, "", "/login");
    mockedLogin.mockResolvedValue({
      email: "admin@example.invalid",
      authorities: ["ROLE_ADMIN"],
      responsibleId: null,
    });
    const user = userEvent.setup();

    renderApp();

    await user.type(screen.getByLabelText(/E-mail/), "admin@example.invalid");
    await user.type(screen.getByLabelText(/Senha/), "secret-value");
    await user.click(screen.getByRole("button", { name: "Entrar" }));

    expect(
      await screen.findByText("Painel administrativo"),
    ).toBeInTheDocument();
    expect(screen.getByText("admin@example.invalid")).toBeInTheDocument();
    expect(window.location.pathname).toBe("/");
  });

  it("shows a safe message for invalid credentials", async () => {
    window.history.replaceState(null, "", "/login");
    mockedLogin.mockRejectedValue(
      new AuthApiError("Invalid email or password", 401),
    );
    const user = userEvent.setup();

    renderApp();

    await user.type(screen.getByLabelText(/E-mail/), "admin@example.invalid");
    await user.type(screen.getByLabelText(/Senha/), "wrong-password");
    await user.click(screen.getByRole("button", { name: "Entrar" }));

    expect(
      await screen.findByText("E-mail ou senha inválidos."),
    ).toBeInTheDocument();
  });

  it("shows the authenticated loading state", () => {
    mockedGetCurrentUser.mockImplementation(
      () => new Promise<never>(() => undefined),
    );

    renderApp();

    expect(screen.getByText("Carregando sessão")).toBeInTheDocument();
  });

  it("shows the backend status for an authenticated user", async () => {
    mockedGetCurrentUser.mockResolvedValue({
      email: "admin@example.invalid",
      authorities: ["ROLE_ADMIN"],
      responsibleId: null,
    });

    renderApp();

    expect(await screen.findByText("Backend UP")).toBeInTheDocument();
  });

  it("shows an error when the backend health check fails", async () => {
    mockedGetCurrentUser.mockResolvedValue({
      email: "admin@example.invalid",
      authorities: ["ROLE_ADMIN"],
      responsibleId: null,
    });
    mockedGetHealthStatus.mockRejectedValue(new Error("offline"));

    renderApp();

    expect(await screen.findByText("Backend indisponível")).toBeInTheDocument();
  });

  it("logs out and returns to the login page", async () => {
    mockedGetCurrentUser.mockResolvedValue({
      email: "admin@example.invalid",
      authorities: ["ROLE_ADMIN"],
      responsibleId: null,
    });
    mockedLogout.mockResolvedValue();
    const user = userEvent.setup();

    renderApp();

    await user.click(await screen.findByRole("button", { name: "Sair" }));

    expect(await screen.findByLabelText(/E-mail/)).toBeInTheDocument();
    expect(window.location.pathname).toBe("/login");
  });

  it("shows the responsible dashboard for a responsible user", async () => {
    mockedGetCurrentUser.mockResolvedValue({
      email: "responsavel@example.invalid",
      authorities: ["ROLE_RESPONSIBLE"],
      responsibleId: "20000000-0000-4000-8000-000000000001",
    });

    renderApp();

    expect(
      await screen.findByText("Painel do responsável"),
    ).toBeInTheDocument();
    expect(screen.queryByText("Painel administrativo")).not.toBeInTheDocument();
  });

  it("shows an error when the protected session cannot be validated", async () => {
    mockedGetCurrentUser.mockRejectedValue(new Error("offline"));

    renderApp();

    expect(
      await screen.findByText("Não foi possível validar sua sessão."),
    ).toBeInTheDocument();
  });
});
