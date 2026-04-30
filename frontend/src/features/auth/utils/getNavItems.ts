import type { AuthUser } from "../model/AuthUser";

export type Page = "orders" | "approval" | "dispatch" | "wip" | "request";

export type NavItem = {
  page: Page;
  label: string;
};

export function getNavItems(user: AuthUser): NavItem[] {
  if (user.role === "ADMIN") {
    return [
      { page: "request", label: "Request" },
      { page: "approval", label: "Approval" },
      { page: "dispatch", label: "Dispatch" },
      { page: "wip", label: "WIP Board" },
      { page: "orders", label: "Orders" },
    ];
  }

  if (user.role === "REQUESTER") {
    return [{ page: "request", label: "Request" }];
  }

  if (user.role === "MANAGER") {
    return [{ page: "approval", label: "Approval" }];
  }

  if (user.role === "LAB_STAFF") {
    return [
      { page: "dispatch", label: "Dispatch" },
      { page: "wip", label: "WIP Board" },
      { page: "orders", label: "Orders" },
    ];
  }

  return [];
}