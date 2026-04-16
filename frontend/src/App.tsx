import { useState } from "react";
import Layout from "./layouts/Layout";
import OrderPage from "./features/order/page/OrderPage";
// 之後可以加 ApprovalPage...

function App() {
  const [page, setPage] = useState("orders");

  return (
    <Layout setPage={setPage}>
      {page === "orders" && <OrderPage />}
      {/* 之後 */}
      {/* {page === "approval" && <ApprovalPage />} */}
    </Layout>
  );
}

export default App;