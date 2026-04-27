import { useState } from "react";
import Layout from "./layouts/Layout";
import OrderPage from "./features/order/page/OrderPage";
import LoginPage from "./features/auth/page/LoginPage";
// 之後可以加 ApprovalPage...
import ApprovalPage from "./features/approval/page/ApprovalPage";

type Page = "orders" | "approval";

function App() {
  const [user, setUser] = useState<any>(null);
  const [page, setPage] = useState("orders");

  if (!user) {
    return <LoginPage setUser={setUser} />;
  }
  const [page, setPage] = useState<Page>("orders");

  return (
    <Layout currentPage={page} setPage={setPage}>
      {page === "orders" && <OrderPage />}
      {page === "approval" && <ApprovalPage />}
    </Layout>
  );
}

export default App;