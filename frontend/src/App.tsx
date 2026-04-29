import { useMemo, useState } from "react";
import { googleLogout } from "@react-oauth/google";

import Layout from "./layouts/Layout";
import OrderPage from "./features/order/page/OrderPage";
import ApprovalPage from "./features/approval/page/ApprovalPage";
import RequestsPage from "./features/requests/page/RequestsPage";
import LoginPage from "./features/auth/page/LoginPage";
import ProfileSetupPage from "./features/auth/page/ProfileSetupPage";
import EquipmentPage from "./features/equipment/page/EquipmentPage";
import EquipmentTypeManagementPage from "./features/equipment/page/EquipmentTypeManagementPage";
import RecipeManagementPage from "./features/recipe/page/RecipeManagementPage";
import type { AuthUser } from "./features/auth/model/AuthUser";
import { getNavItems, type Page } from "./features/auth/utils/getNavItems";

const pageMap: Record<Page, React.ComponentType<any>> = {
  orders: OrderPage,
  approval: ApprovalPage,
  requests: RequestsPage,
  equipment: EquipmentPage,
  equipmentTypes: EquipmentTypeManagementPage,
  recipe: RecipeManagementPage,
  management: () => <div className="p-4">Please select an item from the sidebar dropdown.</div>,
};

function App() {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [page, setPage] = useState<Page>("requests");

  const navItems = useMemo(() => {
    return user ? getNavItems(user) : [];
  }, [user]);

  if (!user) {
    return <LoginPage setUser={setUser} />;
  }

  const needsSetup =
    !user.role || (user.role === "REQUESTER" && !user.managerId);

  if (needsSetup) {
    return <ProfileSetupPage user={user} setUser={setUser} />;
  }

  const isPageValid = navItems.some(
    (item) => item.page === page || item.subItems?.some((sub) => sub.page === page)
  );

  const safePage = isPageValid ? page : navItems[0]?.page ?? "requests";

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
