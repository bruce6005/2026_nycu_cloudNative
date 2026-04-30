import { useMemo, useState } from "react";
import { googleLogout } from "@react-oauth/google";

import Layout from "./layouts/Layout";
import OrderPage from "./features/order/page/OrderPage";
import ApprovalPage from "./features/approval/page/ApprovalPage";
import RequestPage from "./features/request/page/RequestPage";
import WIPManagementPage from "./features/wip_management/page/WIPManagementPage";
import WIPBuilderPage from "./features/wip_builder/page/WIPBuilderPage";
import LoginPage from "./features/auth/page/LoginPage";
import ProfileSetupPage from "./features/auth/page/ProfileSetupPage";
import type { AuthUser } from "./features/auth/model/AuthUser";
import { getNavItems, type Page } from "./features/auth/utils/getNavItems";

const pageMap: Record<Page, React.ComponentType<any>> = {
  orders: OrderPage,
  approval: ApprovalPage,
  wip_builder: WIPBuilderPage,
  wip_management: WIPManagementPage,
  request: RequestPage,
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

  const needsSetup =
    !user.role || (user.role === "REQUESTER" && !user.managerId);

  if (needsSetup) {
    return <ProfileSetupPage user={user} setUser={setUser} />;
  }

  const safePage = navItems.some((item) => item.page === page)
    ? page
    : navItems[0]?.page ?? "request";

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