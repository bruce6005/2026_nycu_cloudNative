import { googleLogout } from '@react-oauth/google';

function Layout({ children, setPage, user, setUser }: any) {
  const handleLogout = () => {
    googleLogout();
    setUser(null);
  };

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
      <div className="topbar" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div className="logo">LAB SYSTEM</div>
        <div className="user-section" style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          <span>{user.name}</span>
          <button onClick={handleLogout} style={{ padding: '5px 10px', cursor: 'pointer', background: '#4e0c05ff', color: 'white', border: 'none', borderRadius: '5px' }}>
            Logout
          </button>
        </div>
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