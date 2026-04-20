import { useState } from "react";
import Layout from "./layouts/Layout";
import OrderPage from "./features/order/page/OrderPage";
import ApprovalPage from "./features/approval/page/ApprovalPage";

type Page = "orders" | "approval";

function App() {
  const [page, setPage] = useState<Page>("orders");

  return (
    <Layout currentPage={page} setPage={setPage}>
      {page === "orders" && <OrderPage />}
      {page === "approval" && <ApprovalPage />}
    </Layout>
  );
}

export default App;