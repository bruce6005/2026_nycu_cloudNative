import type { AuthUser, UserRole } from "./AuthUser";

const USER_ROLES: UserRole[] = ["REQUESTER", "LAB_STAFF", "MANAGER", "ADMIN"];

function readString(value: unknown): string {
  return typeof value === "string" ? value : "";
}

function readOptionalString(value: unknown): string | null {
  return typeof value === "string" && value.trim() ? value : null;
}

function readNumber(value: unknown): number {
  return typeof value === "number" && Number.isFinite(value) ? value : 0;
}

function readOptionalNumber(value: unknown): number | null {
  return typeof value === "number" && Number.isFinite(value) ? value : null;
}

function readRole(value: unknown): UserRole | null {
  return typeof value === "string" && USER_ROLES.includes(value as UserRole)
    ? (value as UserRole)
    : null;
}

export function sanitizeAuthUser(value: unknown): AuthUser | null {
  if (!value || typeof value !== "object") {
    return null;
  }

  const candidate = value as Record<string, unknown>;
  const id = readNumber(candidate.id);
  const email = readString(candidate.email);
  const name = readString(candidate.name);

  if (!id || !email || !name) {
    return null;
  }

  const sanitized: AuthUser = {
    id,
    email,
    name,
    avatarUrl: readOptionalString(candidate.avatarUrl),
    role: readRole(candidate.role),
    managerId: readOptionalNumber(candidate.managerId),
  };

  const token = readOptionalString(candidate.token);
  if (token) {
    sanitized.token = token;
  }

  return sanitized;
}

export function sanitizeAuthUserForStorage(value: unknown): string | null {
  const user = sanitizeAuthUser(value);
  if (!user) {
    return null;
  }

  return JSON.stringify({
    id: user.id,
    email: user.email,
    name: user.name,
    avatarUrl: user.avatarUrl ?? null,
    role: user.role ?? null,
    managerId: user.managerId ?? null,
    token: user.token,
  });
}
