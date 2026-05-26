export type UserRole = "REQUESTER" | "LAB_STAFF" | "MANAGER" | "ADMIN";

export type AuthUser = {
  id: number;
  email: string;
  name: string;
  avatarUrl?: string | null;
  role?: UserRole | null;
  managerId?: number | null;
};

export type ManagerOption = {
  id: number;
  name: string;
  email: string;
  role: Extract<UserRole, "MANAGER" | "ADMIN">;
};
