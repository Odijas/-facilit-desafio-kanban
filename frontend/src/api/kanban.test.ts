import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { getCsrfHeaders } from "./auth";
import {
  getProjectIndicators,
  listProjects,
  transitionProject,
} from "./kanban";

vi.mock("./auth", () => ({
  getCsrfHeaders: vi.fn(),
}));

const mockedGetCsrfHeaders = vi.mocked(getCsrfHeaders);
const fetchMock = vi.fn<typeof fetch>();

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

const projectPayload = {
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

describe("kanban API", () => {
  beforeEach(() => {
    fetchMock.mockReset();
    mockedGetCsrfHeaders.mockReset();
    mockedGetCsrfHeaders.mockResolvedValue({ "X-XSRF-TOKEN": "csrf-token" });
    vi.stubGlobal("fetch", fetchMock);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("parses a paginated projects response", async () => {
    fetchMock.mockResolvedValueOnce(
      jsonResponse({
        content: [projectPayload],
        page: 0,
        size: 100,
        totalElements: 1,
        totalPages: 1,
        hasNext: false,
        hasPrevious: false,
      }),
    );

    const projects = await listProjects();

    expect(projects).toHaveLength(1);
    expect(projects[0]?.name).toBe("Portal cidadão");
  });

  it("sends advanced project filters as query parameters", async () => {
    fetchMock.mockResolvedValueOnce(
      jsonResponse({
        content: [],
        page: 0,
        size: 100,
        totalElements: 0,
        totalPages: 0,
        hasNext: false,
        hasPrevious: false,
      }),
    );

    await listProjects({
      status: "IN_PROGRESS",
      secretariatId: "10000000-0000-4000-8000-000000000001",
      responsibleId: "20000000-0000-4000-8000-000000000001",
      plannedFrom: "2026-09-01",
      plannedTo: "2026-09-30",
      text: "  portal digital  ",
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/v1/projects?status=IN_PROGRESS&secretariatId=10000000-0000-4000-8000-000000000001&responsibleId=20000000-0000-4000-8000-000000000001&plannedFrom=2026-09-01&plannedTo=2026-09-30&text=portal+digital&page=0&size=100",
      expect.objectContaining({ credentials: "same-origin" }),
    );
  });

  it("parses project indicators", async () => {
    fetchMock.mockResolvedValueOnce(
      jsonResponse({
        totalProjects: 3,
        delayedProjects: 1,
        byStatus: [
          { status: "NOT_STARTED", projectCount: 2, averageDelayDays: 0 },
          { status: "OVERDUE", projectCount: 1, averageDelayDays: 4.5 },
        ],
      }),
    );

    const indicators = await getProjectIndicators();

    expect(indicators.totalProjects).toBe(3);
    expect(indicators.byStatus[1]?.averageDelayDays).toBe(4.5);
  });

  it("uses CSRF and preserves a backend transition error detail", async () => {
    fetchMock.mockResolvedValueOnce(
      jsonResponse(
        {
          title: "INVALID_REQUEST",
          status: 400,
          detail: "Ajuste as datas previstas antes da transição",
          code: "INVALID_REQUEST",
        },
        400,
      ),
    );

    await expect(
      transitionProject(projectPayload.id, "IN_PROGRESS"),
    ).rejects.toMatchObject({
      message: "Ajuste as datas previstas antes da transição",
      status: 400,
    });

    expect(mockedGetCsrfHeaders).toHaveBeenCalledOnce();
    expect(fetchMock).toHaveBeenCalledWith(
      `/api/v1/projects/${projectPayload.id}/status`,
      expect.objectContaining({
        method: "PATCH",
        credentials: "same-origin",
        headers: expect.objectContaining({
          "X-XSRF-TOKEN": "csrf-token",
        }),
      }),
    );
  });
});
