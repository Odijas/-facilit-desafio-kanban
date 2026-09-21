import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { App } from "./App";
import { getHealthStatus } from "./api/health";

vi.mock("./api/health", () => ({
  getHealthStatus: vi.fn(),
}));

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
    mockedGetHealthStatus.mockReset();
  });

  it("shows the backend status when health succeeds", async () => {
    mockedGetHealthStatus.mockResolvedValue({ status: "UP" });

    renderApp();

    expect(await screen.findByText("Backend UP")).toBeInTheDocument();
  });

  it("shows an error when health fails", async () => {
    mockedGetHealthStatus.mockRejectedValue(new Error("offline"));

    renderApp();

    expect(await screen.findByText("Backend indisponível")).toBeInTheDocument();
  });
});
