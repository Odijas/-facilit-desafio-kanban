import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  createSecretariat,
  deleteSecretariat,
  listSecretariats,
  type Secretariat,
  updateSecretariat,
} from "../../api/kanban";
import { SecretariatPanel } from "./SecretariatPanel";

vi.mock("../../api/kanban", () => ({
  KanbanApiError: class MockKanbanApiError extends Error {
    readonly status: number;

    constructor(message: string, status: number) {
      super(message);
      this.status = status;
    }
  },
  listSecretariats: vi.fn(),
  createSecretariat: vi.fn(),
  updateSecretariat: vi.fn(),
  deleteSecretariat: vi.fn(),
}));

const mockedListSecretariats = vi.mocked(listSecretariats);
const mockedCreateSecretariat = vi.mocked(createSecretariat);
const mockedUpdateSecretariat = vi.mocked(updateSecretariat);
const mockedDeleteSecretariat = vi.mocked(deleteSecretariat);

const secretariat: Secretariat = {
  id: "10000000-0000-4000-8000-000000000001",
  name: "Secretaria Digital",
  createdAt: "2026-09-22T12:00:00Z",
  updatedAt: "2026-09-22T12:00:00Z",
};

function renderPanel(canManage = true) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <SecretariatPanel canManage={canManage} />
    </QueryClientProvider>,
  );
}

describe("SecretariatPanel", () => {
  beforeEach(() => {
    mockedListSecretariats.mockReset();
    mockedCreateSecretariat.mockReset();
    mockedUpdateSecretariat.mockReset();
    mockedDeleteSecretariat.mockReset();

    mockedListSecretariats.mockResolvedValue([secretariat]);
    mockedCreateSecretariat.mockResolvedValue(secretariat);
    mockedUpdateSecretariat.mockResolvedValue(secretariat);
    mockedDeleteSecretariat.mockResolvedValue();
  });

  it("hides secretariat management actions from non-administrators", async () => {
    renderPanel(false);

    expect(await screen.findByText("Secretaria Digital")).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Nova secretaria" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Editar" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Excluir" }),
    ).not.toBeInTheDocument();
  });

  it("creates, updates and deletes a secretariat through explicit actions", async () => {
    const user = userEvent.setup();
    renderPanel();

    expect(await screen.findByText("Secretaria Digital")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Nova secretaria" }));
    await user.type(
      screen.getByLabelText(/Nome da secretaria/),
      "Secretaria de Obras",
    );
    await user.click(screen.getByRole("button", { name: "Salvar" }));
    await waitFor(() => {
      expect(mockedCreateSecretariat).toHaveBeenCalledWith({
        name: "Secretaria de Obras",
      });
    });
    await waitFor(() => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "Editar" }));
    const nameField = screen.getByLabelText(/Nome da secretaria/);
    await user.clear(nameField);
    await user.type(nameField, "Secretaria de Inovação");
    await user.click(screen.getByRole("button", { name: "Salvar" }));
    await waitFor(() => {
      expect(mockedUpdateSecretariat).toHaveBeenCalledWith(secretariat.id, {
        name: "Secretaria de Inovação",
      });
    });
    await waitFor(() => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "Excluir" }));
    const dialog = screen.getByRole("dialog", { name: "Excluir secretaria?" });
    await user.click(within(dialog).getByRole("button", { name: "Excluir" }));
    await waitFor(() => {
      expect(mockedDeleteSecretariat).toHaveBeenCalledWith(secretariat.id);
    });
  });
});
