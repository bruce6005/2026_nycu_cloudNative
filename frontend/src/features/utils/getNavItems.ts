import type { AuthUser } from "../auth/model/AuthUser";

export type Page =
  | "approval"
  | "request"
  | "equipment"
  | "equipmentTypes"
  | "recipe"
  | "management"
  | "wip_builder"
  | "wip_management"
  | "manager_dashboard";

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
      { page: "wip_builder", label: "WIP Builder" },
      { page: "wip_management", label: "WIP Management" },
      { page: "manager_dashboard", label: "Manager Dashboard" },
      {
        page: "management",
        label: "Management",
        subItems: [
          { page: "equipmentTypes", label: "Equipment Types" },
          { page: "equipment", label: "Equipment" },
          { page: "recipe", label: "Recipe" },
        ],
      },
    ];
  }

  if (user.role === "REQUESTER") {
    return [{ page: "request", label: "Request" }];
  }

  if (user.role === "MANAGER") {
    return [
      { page: "manager_dashboard", label: "Manager Dashboard" },
      { page: "approval", label: "Approval" },
      {
        page: "management",
        label: "Management",
        subItems: [
          { page: "equipmentTypes", label: "Equipment Types" },
          { page: "equipment", label: "Equipment" },
          { page: "recipe", label: "Recipe" },
        ],
      },
    ];
  }

  if (user.role === "LAB_STAFF") {
    return [
      { page: "wip_builder", label: "WIP Builder" },
      { page: "wip_management", label: "WIP Management" },
    ];
  }

  return [];
}