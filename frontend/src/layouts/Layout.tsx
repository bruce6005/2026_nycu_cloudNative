function Layout({ children, setPage }: any) {
  return (
    <>
      <div className="topbar">
        <div className="logo">LAB SYSTEM</div>
      </div>

      <div className="container">
        <div className="sidebar">
          <div className="menu-item" onClick={() => setPage("orders")}>
            Orders
          </div>

          <div className="menu-item" onClick={() => setPage("approval")}>
            Approval
          </div>
        </div>

        <div className="content">{children}</div>
      </div>
    </>
  );
}

export default Layout;