export type HealthStatus = {
  status: string;
};

export async function getHealthStatus(): Promise<HealthStatus> {
  const response = await fetch("/api/v1/health", {
    headers: {
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`Health endpoint returned HTTP ${response.status}`);
  }

  const body: unknown = await response.json();

  if (
    typeof body !== "object" ||
    body === null ||
    !("status" in body) ||
    typeof body.status !== "string"
  ) {
    throw new Error("Health endpoint returned an invalid payload");
  }

  return { status: body.status };
}
