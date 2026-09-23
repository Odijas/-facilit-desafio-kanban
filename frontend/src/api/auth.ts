export type AuthUser = {
  email: string;
  authorities: string[];
};

export class AuthApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "AuthApiError";
    this.status = status;
  }
}

type CsrfDescriptor = {
  headerName: string;
  cookieName: string;
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

function parseAuthUser(value: unknown): AuthUser {
  if (
    !isRecord(value) ||
    typeof value.email !== "string" ||
    !isStringArray(value.authorities)
  ) {
    throw new Error("Authentication endpoint returned an invalid payload");
  }

  return {
    email: value.email,
    authorities: value.authorities,
  };
}

function parseCsrfDescriptor(value: unknown): CsrfDescriptor {
  if (
    !isRecord(value) ||
    typeof value.headerName !== "string" ||
    typeof value.cookieName !== "string"
  ) {
    throw new Error("CSRF endpoint returned an invalid payload");
  }

  return {
    headerName: value.headerName,
    cookieName: value.cookieName,
  };
}

function readCookie(name: string): string {
  const prefix = `${encodeURIComponent(name)}=`;
  const cookie = document.cookie
    .split(";")
    .map((part) => part.trim())
    .find((part) => part.startsWith(prefix));

  if (cookie === undefined) {
    throw new Error(`Required cookie was not provided: ${name}`);
  }

  return decodeURIComponent(cookie.slice(prefix.length));
}

export async function getCsrfHeaders(): Promise<Record<string, string>> {
  const response = await fetch("/api/v1/auth/csrf", {
    credentials: "same-origin",
    headers: {
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    throw new AuthApiError(
      "Unable to initialize CSRF protection",
      response.status,
    );
  }

  const descriptor = parseCsrfDescriptor(await response.json());
  return {
    [descriptor.headerName]: readCookie(descriptor.cookieName),
  };
}

export async function getCurrentUser(): Promise<AuthUser> {
  const response = await fetch("/api/v1/auth/me", {
    credentials: "same-origin",
    headers: {
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    throw new AuthApiError(
      "Unable to validate the current session",
      response.status,
    );
  }

  return parseAuthUser(await response.json());
}

export async function login(
  email: string,
  password: string,
): Promise<AuthUser> {
  const csrfHeaders = await getCsrfHeaders();
  const response = await fetch("/api/v1/auth/login", {
    method: "POST",
    credentials: "same-origin",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      ...csrfHeaders,
    },
    body: JSON.stringify({ email, password }),
  });

  if (!response.ok) {
    throw new AuthApiError(
      response.status === 401
        ? "Invalid email or password"
        : "Unable to authenticate",
      response.status,
    );
  }

  return parseAuthUser(await response.json());
}

export async function logout(): Promise<void> {
  const csrfHeaders = await getCsrfHeaders();
  const response = await fetch("/api/v1/auth/logout", {
    method: "POST",
    credentials: "same-origin",
    headers: {
      ...csrfHeaders,
    },
  });

  if (!response.ok) {
    throw new AuthApiError(
      "Unable to end the current session",
      response.status,
    );
  }
}
