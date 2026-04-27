import type { AuthUser } from "../model/AuthUser";

export type Page = "orders" | "approval" | "requests";

export type NavItem = {
  page: Page;
  label: string;
};

export function getNavItems(user: AuthUser): NavItem[] {
  if (user.role === "ADMIN") {
    return [
      { page: "requests", label: "Requests" },
      { page: "approval", label: "Approval" },
      { page: "orders", label: "Orders" },
    ];
  }

  if (user.role === "REQUESTER") {
    return [{ page: "requests", label: "Requests" }];
  }

  if (user.role === "MANAGER") {
    return [{ page: "approval", label: "Approval" }];
  }

  if (user.role === "LAB_STAFF") {
    return [{ page: "orders", label: "Orders" }];
  }

  return [];
}