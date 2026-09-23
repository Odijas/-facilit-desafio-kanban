import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { getProjectIndicators } from "../../api/kanban";
import { ProjectIndicatorsPanel } from "./ProjectIndicatorsPanel";

vi.mock("../../api/kanban", () => ({
  getProjectIndicators: vi.fn(),
}));

const mockedGetProjectIndicators = vi.mocked(getProjectIndicators);

function renderPanel() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <ProjectIndicatorsPanel />
    </QueryClientProvider>,
  );
}

describe("ProjectIndicatorsPanel", () => {
  beforeEach(() => {
    mockedGetProjectIndicators.mockReset();
  });

  it("renders project quantities and average delay by status", async () => {
    mockedGetProjectIndicators.mockResolvedValue({
      totalProjects: 4,
      delayedProjects: 1,
      byStatus: [
        { status: "NOT_STARTED", projectCount: 2, averageDelayDays: 0 },
        { status: "IN_PROGRESS", projectCount: 1, averageDelayDays: 0 },
        { status: "OVERDUE", projectCount: 1, averageDelayDays: 5.5 },
        { status: "COMPLETED", projectCount: 0, averageDelayDays: 0 },
      ],
    });

    renderPanel();

    expect(await screen.findByText("Indicadores")).toBeInTheDocument();
    expect(screen.getByText("Total de projetos")).toBeInTheDocument();
    expect(screen.getByText("Projetos com atraso")).toBeInTheDocument();
    expect(screen.getByText("Média de atraso: 5.5 dia(s)")).toBeInTheDocument();
  });

  it("shows an error when indicators cannot be loaded", async () => {
    mockedGetProjectIndicators.mockRejectedValue(new Error("offline"));

    renderPanel();

    expect(
      await screen.findByText("Não foi possível carregar os indicadores."),
    ).toBeInTheDocument();
  });
});
