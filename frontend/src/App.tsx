import { useState } from "react";
import Layout from "./layouts/Layout";
import OrderPage from "./features/order/page/OrderPage";
<<<<<<< HEAD
import ApprovalPage from "./features/approval/page/ApprovalPage";

type Page = "orders" | "approval";

function App() {
  const [page, setPage] = useState<Page>("orders");
=======
import LoginPage from "./features/auth/page/LoginPage";
// 之後可以加 ApprovalPage...

function App() {
  const [user, setUser] = useState<any>(null);
  const [page, setPage] = useState("orders");
>>>>>>> c6872dd18e15995423efcc701ba3845e8b2ebcc6

  if (!user) {
    return <LoginPage setUser={setUser} />;
  }

  return (
<<<<<<< HEAD
    <Layout currentPage={page} setPage={setPage}>
=======
    <Layout setPage={setPage} user={user} setUser={setUser}>
>>>>>>> c6872dd18e15995423efcc701ba3845e8b2ebcc6
      {page === "orders" && <OrderPage />}
      {page === "approval" && <ApprovalPage />}
    </Layout>
  );
}

export default App;