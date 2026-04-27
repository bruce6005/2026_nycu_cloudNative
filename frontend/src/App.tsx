import { useState } from "react";
import Layout from "./layouts/Layout";
import OrderPage from "./features/order/page/OrderPage";
<<<<<<< HEAD
import ApprovalPage from "./features/approval/page/ApprovalPage";
import RequestsPage from "./features/requests/page/RequestsPage";
// import RequestsPage from "./features/requests/page/RequestsPage";

type Page = "orders" | "approval" | "requests";

const pageMap = {
  orders: OrderPage,
  approval: ApprovalPage,
  requests: RequestsPage,
};

function App() {
  const [page, setPage] = useState<Page>("orders");

  const CurrentPage = pageMap[page];

  return (
<<<<<<< HEAD
    <Layout currentPage={page} setPage={setPage}>
      <CurrentPage />
    </Layout>
  );
}

export default App;