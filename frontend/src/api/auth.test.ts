import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { getCurrentUser, login } from "./auth";

const fetchMock = vi.fn<typeof fetch>();
let cookieValue = "";

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json",
    },
  });
}

describe("auth API", () => {
  beforeEach(() => {
    fetchMock.mockReset();
    cookieValue = "";
    vi.stubGlobal("fetch", fetchMock);
    vi.spyOn(document, "cookie", "get").mockImplementation(() => cookieValue);
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it(
    "sends the CSRF cookie value in the backend-provided header when logging in",
    async () => {
      cookieValue = "XSRF-TOKEN=csrf-token-value";
      fetchMock
        .mockResolvedValueOnce(
          jsonResponse({
            headerName: "X-XSRF-TOKEN",
            cookieName: "XSRF-TOKEN",
          }),
        )
        .mockResolvedValueOnce(
          jsonResponse({
            email: "admin@example.invalid",
            authorities: ["ROLE_ADMIN"],
          }),
        );

      const user = await login("admin@example.invalid", "secret-value");

      expect(user.email).toBe("admin@example.invalid");
      expect(fetchMock).toHaveBeenNthCalledWith(
        2,
        "/api/v1/auth/login",
        expect.objectContaining({
          method: "POST",
          credentials: "same-origin",
          headers: expect.objectContaining({
            "X-XSRF-TOKEN": "csrf-token-value",
          }),
        }),
      );
    },
  );

  it("preserves the unauthorized status when the session is absent", async () => {
    fetchMock.mockResolvedValueOnce(
      jsonResponse(
        {
          code: "UNAUTHORIZED",
          detail: "Authentication required",
        },
        401,
      ),
    );

    await expect(getCurrentUser()).rejects.toMatchObject({ status: 401 });
  });
});
