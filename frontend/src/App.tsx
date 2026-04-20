import { useState } from "react";
import Layout from "./layouts/Layout";
import OrderPage from "./features/order/page/OrderPage";
import RequestsPage from "./features/requests/page/RequestsPage";

function App() {
  const [page, setPage] = useState("orders");

  return (
    <Layout setPage={setPage}>
      {page === "orders" && <OrderPage />}
      {page === "requests" && <RequestsPage />}
      {/* 之後 */}
      {/* {page === "approval" && <ApprovalPage />} */}
    </Layout>
  );
}

export default App;