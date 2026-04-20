import React from "react";

type Page = "orders" | "approval";

type Props = {
  children: React.ReactNode;
  currentPage: Page;
  setPage: (page: Page) => void;
};

function Layout({ children, currentPage, setPage }: Props) {
  return (
    <>
      <div className="topbar">
        <div className="logo">LAB SYSTEM</div>
      </div>

      <div className="container">
        <div className="sidebar">
          <div
            className={`menu-item ${currentPage === "orders" ? "active" : ""}`}
            onClick={() => setPage("orders")}
          >
            Orders
          </div>

          <div
            className={`menu-item ${currentPage === "approval" ? "active" : ""}`}
            onClick={() => setPage("approval")}
          >
            Approval
          </div>
        </div>

        <div className="content">{children}</div>
      </div>
    </>
  );
}

export default Layout;