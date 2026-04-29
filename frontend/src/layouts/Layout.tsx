import type { Page, NavItem } from "../features/auth/utils/getNavItems";

type Props = {
  children: React.ReactNode;
  currentPage: Page;
  navItems: NavItem[];
  userName?: string;
  onNavigate: (page: Page) => void;
  onLogout: () => void;
};

function Layout({
  children,
  currentPage,
  navItems,
  userName,
  onNavigate,
  onLogout,
}: Props) {
  return (
    <>
      <div
        className="topbar"
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <div className="logo">LAB SYSTEM</div>

        <div
          className="user-section"
          style={{
            display: "flex",
            alignItems: "center",
            gap: "15px",
          }}
        >
          {userName && <span>{userName}</span>}
          <button
            onClick={onLogout}
            style={{
              padding: "5px 10px",
              cursor: "pointer",
              background: "#4e0c05ff",
              color: "white",
              border: "none",
              borderRadius: "5px",
            }}
          >
            Logout
          </button>
        </div>
      </div>

      <div className="container">
        <div className="sidebar">
          {navItems.map((item) =>
            item.subItems ? (
              <div key={item.page} className="nav-group menu-item">
                {item.label} ▾
                <div className="sub-menu">
                  {item.subItems.map((sub) => (
                    <div
                      key={sub.page}
                      className={`menu-item sub-menu-item ${
                        currentPage === sub.page ? "active" : ""
                      }`}
                      onClick={(e) => {
                        e.stopPropagation();
                        onNavigate(sub.page);
                      }}
                    >
                      {sub.label}
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div
                key={item.page}
                className={`menu-item ${
                  currentPage === item.page ? "active" : ""
                }`}
                onClick={() => onNavigate(item.page)}
              >
                {item.label}
              </div>
            )
          )}
        </div>

        <div className="content">{children}</div>
      </div>
    </>
  );
}

export default Layout;