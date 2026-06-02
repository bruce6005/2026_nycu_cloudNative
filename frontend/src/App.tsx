import { useMemo, useState, useEffect } from "react";
import { googleLogout } from "@react-oauth/google";

import Layout from "./layouts/Layout";
import ApprovalPage from "./features/approval/page/ApprovalPage";
import RequestPage from "./features/request/page/RequestPage";
import WIPManagementPage from "./features/wip_management/page/WIPManagementPage";
import WIPBuilderPage from "./features/wip_builder/page/WIPBuilderPage";
import LoginPage from "./features/auth/page/LoginPage";
import ProfileSetupPage from "./features/auth/page/ProfileSetupPage";
import EquipmentPage from "./features/equipment/page/EquipmentPage";
import EquipmentTypeManagementPage from "./features/equipment/page/EquipmentTypeManagementPage";
import RecipeManagementPage from "./features/recipe/page/RecipeManagementPage";
import ManagerDashboardPage from "./features/managerLog/page/ManagerDashboardPage";
import type { AuthUser } from "./features/auth/model/AuthUser";
import { sanitizeAuthUser } from "./features/auth/model/sanitizeAuthUser";
import { getNavItems, type Page } from "./features/utils/getNavItems";
import { clearToken, saveToken } from "./features/utils/authToken";
import "./features/utils/apiClient";

const AUTH_USER_STORAGE_KEY = "auth_user";
const MAX_STORED_AUTH_USER_LENGTH = 4096;

const pageMap: Record<Page, React.ComponentType<any>> = {
  approval: ApprovalPage,
  request: RequestPage,
  equipment: EquipmentPage,
  equipmentTypes: EquipmentTypeManagementPage,
  recipe: RecipeManagementPage,
  wip_builder: WIPBuilderPage,
  wip_management: WIPManagementPage,
  manager_dashboard: ManagerDashboardPage,
  management: () => (
    <div className="p-4">Please select an item from the sidebar dropdown.</div>
  ),
};

function loadStoredAuthUser(): AuthUser | null {
  const saved = localStorage.getItem(AUTH_USER_STORAGE_KEY);
  if (!saved || saved.length > MAX_STORED_AUTH_USER_LENGTH) {
    localStorage.removeItem(AUTH_USER_STORAGE_KEY);
    return null;
  }

  try {
    const parsed: unknown = JSON.parse(saved);
    const sanitizedUser = sanitizeAuthUser(parsed);
    if (!sanitizedUser) {
      localStorage.removeItem(AUTH_USER_STORAGE_KEY);
    }
    return sanitizedUser;
  } catch {
    localStorage.removeItem(AUTH_USER_STORAGE_KEY);
    return null;
  }
}

function App() {
  const [user, setUser] = useState<AuthUser | null>(loadStoredAuthUser);

  const [page, setPage] = useState<Page>(() => {
    return (localStorage.getItem("current_page") as Page) || "request";
  });

  useEffect(() => {
    if (user) {
      const sanitizedUser = sanitizeAuthUser(user);
      if (sanitizedUser) {
        localStorage.setItem(AUTH_USER_STORAGE_KEY, JSON.stringify(sanitizedUser));
        if (sanitizedUser.token) {
          saveToken(sanitizedUser.token);
        }
      }
    } else {
      localStorage.removeItem(AUTH_USER_STORAGE_KEY);
      clearToken();
    }
  }, [user]);

  useEffect(() => {
    localStorage.setItem("current_page", page);
  }, [page]);

  const navItems = useMemo(() => {
    return user ? getNavItems(user) : [];
  }, [user]);

  if (!user) {
    return <LoginPage setUser={setUser} />;
  }

  const needsSetup = !user.role || (user.role === "REQUESTER" && !user.managerId);

  if (needsSetup) {
    return <ProfileSetupPage user={user} setUser={setUser} />;
  }

  const isPageValid = navItems.some(
    (item) => item.page === page || item.subItems?.some((sub) => sub.page === page)
  );

  const safePage = isPageValid ? page : navItems[0]?.page ?? "request";

  const CurrentPage = pageMap[safePage];

  const handleLogout = () => {
    googleLogout();
    clearToken();
    setUser(null);
  };

  return (
    <Layout
      currentPage={safePage}
      navItems={navItems}
      userName={user.name}
      onNavigate={setPage}
      onLogout={handleLogout}
    >
      <CurrentPage user={user} />
    </Layout>
  );
}

export default App;
