import type { AuthUser } from "../model/AuthUser";

export type Page = "orders" | "approval" | "wip_builder" | "wip_management" | "request";

export type NavItem = {
  page: Page;
  label: string;
};

export function getNavItems(user: AuthUser): NavItem[] {
  if (user.role === "ADMIN") {
    return [
      { page: "request", label: "Request" },
      { page: "approval", label: "Approval" },
      { page: "wip_builder", label: "WIP Builder" },
      { page: "wip_management", label: "WIP Management" },
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
      { page: "wip_builder", label: "WIP Builder" },
      { page: "wip_management", label: "WIP Management" },
      { page: "orders", label: "Orders" },
    ];
  }

  return [];
}