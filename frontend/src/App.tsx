import { useMemo, useState } from "react";
import { googleLogout } from "@react-oauth/google";

import Layout from "./layouts/Layout";
import ApprovalPage from "./features/approval/page/ApprovalPage";
import RequestPage from "./features/request/page/RequestPage";
import WIPManagementPage from "./features/wip_management/page/WIPManagementPage";
import WIPBuilderPage from "./features/wip_builder/page/WIPBuilderPage";
import RequestsPage from "./features/requests/page/RequestsPage";
import AlarmPage from "./features/alarm/page/AlarmPage";
import LoginPage from "./features/auth/page/LoginPage";
import ProfileSetupPage from "./features/auth/page/ProfileSetupPage";
import EquipmentPage from "./features/equipment/page/EquipmentPage";
import EquipmentTypeManagementPage from "./features/equipment/page/EquipmentTypeManagementPage";
import RecipeManagementPage from "./features/recipe/page/RecipeManagementPage";
import type { AuthUser } from "./features/auth/model/AuthUser";
import { getNavItems, type Page } from "./features/utils/getNavItems";
import RequestsReceivePage from "./features/requestsReceive/page/RequestsReceivePage";

const pageMap: Record<Page, React.ComponentType<any>> = {
  approval: ApprovalPage,
  request: RequestPage,
  requestsReceive: RequestsReceivePage,
  equipment: EquipmentPage,
  equipmentTypes: EquipmentTypeManagementPage,
  recipe: RecipeManagementPage,
  wip_builder: WIPBuilderPage,
  wip_management: WIPManagementPage,
  management: () => <div className="p-4">Please select an item from the sidebar dropdown.</div>,
  requests: RequestsPage,
  alarm: AlarmPage,
};

function App() {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [page, setPage] = useState<Page>("request");

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
