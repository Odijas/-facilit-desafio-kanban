import { getCsrfHeaders } from "./auth";

export type ProjectStatus =
  | "NOT_STARTED"
  | "IN_PROGRESS"
  | "OVERDUE"
  | "COMPLETED";

export type Project = {
  id: string;
  name: string;
  status: ProjectStatus;
  responsibleIds: string[];
  plannedStart: string | null;
  plannedEnd: string | null;
  actualStart: string | null;
  actualEnd: string | null;
  delayDays: number;
  remainingTimePercentage: number;
  createdAt: string;
  updatedAt: string;
};

export type Responsible = {
  id: string;
  name: string;
  email: string;
  position: string;
  secretariatId: string | null;
  createdAt: string;
  updatedAt: string;
};

export type ProjectInput = {
  name: string;
  responsibleIds: string[];
  plannedStart: string | null;
  plannedEnd: string | null;
  actualStart: string | null;
  actualEnd: string | null;
};

export type ResponsibleInput = {
  name: string;
  email: string;
  position: string;
  secretariatId: string | null;
};

export class KanbanApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "KanbanApiError";
    this.status = status;
  }
}

type PagePayload<T> = {
  content: T[];
  page: number;
  hasNext: boolean;
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function isStringArray(value: unknown): value is string[] {
  return (
    Array.isArray(value) &&
    value.every((item: unknown) => typeof item === "string")
  );
}

function isNullableString(value: unknown): value is string | null {
  return value === null || typeof value === "string";
}

function isProjectStatus(value: unknown): value is ProjectStatus {
  return (
    value === "NOT_STARTED" ||
    value === "IN_PROGRESS" ||
    value === "OVERDUE" ||
    value === "COMPLETED"
  );
}

function parseProject(value: unknown): Project {
  if (
    !isRecord(value) ||
    typeof value.id !== "string" ||
    typeof value.name !== "string" ||
    !isProjectStatus(value.status) ||
    !isStringArray(value.responsibleIds) ||
    !isNullableString(value.plannedStart) ||
    !isNullableString(value.plannedEnd) ||
    !isNullableString(value.actualStart) ||
    !isNullableString(value.actualEnd) ||
    typeof value.delayDays !== "number" ||
    typeof value.remainingTimePercentage !== "number" ||
    typeof value.createdAt !== "string" ||
    typeof value.updatedAt !== "string"
  ) {
    throw new Error("Projects endpoint returned an invalid project payload");
  }

  return {
    id: value.id,
    name: value.name,
    status: value.status,
    responsibleIds: value.responsibleIds,
    plannedStart: value.plannedStart,
    plannedEnd: value.plannedEnd,
    actualStart: value.actualStart,
    actualEnd: value.actualEnd,
    delayDays: value.delayDays,
    remainingTimePercentage: value.remainingTimePercentage,
    createdAt: value.createdAt,
    updatedAt: value.updatedAt,
  };
}

function parseResponsible(value: unknown): Responsible {
  if (
    !isRecord(value) ||
    typeof value.id !== "string" ||
    typeof value.name !== "string" ||
    typeof value.email !== "string" ||
    typeof value.position !== "string" ||
    !isNullableString(value.secretariatId) ||
    typeof value.createdAt !== "string" ||
    typeof value.updatedAt !== "string"
  ) {
    throw new Error(
      "Responsibles endpoint returned an invalid responsible payload",
    );
  }

  return {
    id: value.id,
    name: value.name,
    email: value.email,
    position: value.position,
    secretariatId: value.secretariatId,
    createdAt: value.createdAt,
    updatedAt: value.updatedAt,
  };
}

function parsePage<T>(
  value: unknown,
  parseItem: (item: unknown) => T,
): PagePayload<T> {
  if (
    !isRecord(value) ||
    !Array.isArray(value.content) ||
    typeof value.page !== "number" ||
    typeof value.hasNext !== "boolean"
  ) {
    throw new Error("Paginated endpoint returned an invalid payload");
  }

  return {
    content: value.content.map(parseItem),
    page: value.page,
    hasNext: value.hasNext,
  };
}

async function errorFrom(response: Response): Promise<KanbanApiError> {
  const fallback = `Request failed with HTTP ${response.status}`;

  try {
    const body: unknown = await response.json();
    if (isRecord(body) && typeof body.detail === "string") {
      return new KanbanApiError(body.detail, response.status);
    }
  } catch {
    return new KanbanApiError(fallback, response.status);
  }

  return new KanbanApiError(fallback, response.status);
}

async function requireOk(response: Response): Promise<Response> {
  if (!response.ok) {
    throw await errorFrom(response);
  }
  return response;
}

async function mutateJson(
  url: string,
  method: "POST" | "PUT" | "PATCH" | "DELETE",
  body?: unknown,
): Promise<Response> {
  const csrfHeaders = await getCsrfHeaders();
  return requireOk(
    await fetch(url, {
      method,
      credentials: "same-origin",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        ...csrfHeaders,
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    }),
  );
}

async function listAll<T>(
  endpoint: string,
  parseItem: (item: unknown) => T,
): Promise<T[]> {
  const items: T[] = [];
  let page = 0;

  while (true) {
    const response = await requireOk(
      await fetch(`${endpoint}?page=${page}&size=100`, {
        credentials: "same-origin",
        headers: { Accept: "application/json" },
      }),
    );
    const payload = parsePage(await response.json(), parseItem);
    items.push(...payload.content);

    if (!payload.hasNext) {
      return items;
    }

    page += 1;
  }
}

export function listProjects(): Promise<Project[]> {
  return listAll("/api/v1/projects", parseProject);
}

export function listResponsibles(): Promise<Responsible[]> {
  return listAll("/api/v1/responsibles", parseResponsible);
}

export async function createProject(input: ProjectInput): Promise<Project> {
  const response = await mutateJson("/api/v1/projects", "POST", input);
  return parseProject(await response.json());
}

export async function updateProject(
  projectId: string,
  input: ProjectInput,
): Promise<Project> {
  const response = await mutateJson(
    `/api/v1/projects/${projectId}`,
    "PUT",
    input,
  );
  return parseProject(await response.json());
}

export async function transitionProject(
  projectId: string,
  status: ProjectStatus,
): Promise<Project> {
  const response = await mutateJson(
    `/api/v1/projects/${projectId}/status`,
    "PATCH",
    {
      status,
    },
  );
  return parseProject(await response.json());
}

export async function deleteProject(projectId: string): Promise<void> {
  await mutateJson(`/api/v1/projects/${projectId}`, "DELETE");
}

export async function createResponsible(
  input: ResponsibleInput,
): Promise<Responsible> {
  const response = await mutateJson("/api/v1/responsibles", "POST", input);
  return parseResponsible(await response.json());
}
