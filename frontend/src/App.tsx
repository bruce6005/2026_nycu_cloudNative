import { useState } from "react";
import Layout from "./layouts/Layout";
import OrderPage from "./features/order/page/OrderPage";
import LoginPage from "./features/auth/page/LoginPage";
// 之後可以加 ApprovalPage...

function App() {
  const [user, setUser] = useState<any>(null);
  const [page, setPage] = useState("orders");

  if (!user) {
    return <LoginPage setUser={setUser} />;
  }

  return (
    <Layout setPage={setPage} user={user} setUser={setUser}>
      {page === "orders" && <OrderPage />}
      {/* 之後 */}
      {/* {page === "approval" && <ApprovalPage />} */}
    </Layout>
  );
}

export default App;