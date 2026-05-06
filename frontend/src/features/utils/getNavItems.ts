import type { AuthUser } from "../auth/model/AuthUser";

export type Page = "approval" | "request" | "requestsReceive" | "equipment" | "equipmentTypes" | "recipe" | "management" | "wip_builder" | "wip_management";

export type NavItem = {
  page: Page;
  label: string;
  subItems?: { page: Page; label: string }[];
};

export function getNavItems(user: AuthUser): NavItem[] {
  if (user.role === "ADMIN") {
    return [
      { page: "request", label: "Request" },
      { page: "approval", label: "Approval" },
      { page: "requestsReceive", label: "RequestsReceive" },
      { page: "wip_builder", label: "WIP Builder" },
      { page: "wip_management", label: "WIP Management" },
      {
        page: "management",
        label: "Management",
        subItems: [
          { page: "equipmentTypes", label: "Equipment Types" },
          { page: "equipment", label: "Equipment" },
          { page: "recipe", label: "Recipe" }
        ]
      },
    ];
  }

  if (user.role === "REQUESTER") {
    return [{ page: "request", label: "Request" }];
  }

  if (user.role === "MANAGER") {
    return [
      { page: "approval", label: "Approval" },
      {
        page: "management",
        label: "Management",
        subItems: [
          { page: "equipmentTypes", label: "Equipment Types" },
          { page: "equipment", label: "Equipment" },
          { page: "recipe", label: "Recipe" }
        ]
      },
    ];
  }

  if (user.role === "LAB_STAFF") {
    return [
      { page: "requestsReceive", label: "RequestsReceive" },
      { page: "wip_builder", label: "WIP Builder" },
      { page: "wip_management", label: "WIP Management" },
    ];
  }

  return [];
}
