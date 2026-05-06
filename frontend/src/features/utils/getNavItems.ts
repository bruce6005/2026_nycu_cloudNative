import type { AuthUser } from "../auth/model/AuthUser";

export type Page = "orders" | "approval" | "requests" | "requestsReceive" | "equipment" | "equipmentTypes" | "recipe" | "management";

export type NavItem = {
  page: Page;
  label: string;
  subItems?: { page: Page; label: string }[];
};

export function getNavItems(user: AuthUser): NavItem[] {
  if (user.role === "ADMIN") {
    return [
      { page: "requests", label: "Requests" },
      { page: "approval", label: "Approval" },
      { page: "orders", label: "Orders" },
      { page: "requestsReceive", label: "RequestsReceive" },
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
    return [{ page: "requests", label: "Requests" }];
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
    return [{ page: "requestsReceive", label: "RequestsReceive" }];
  }

  return [];
}
