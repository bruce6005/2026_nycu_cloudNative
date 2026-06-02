import React from "react";
import type { Page, NavItem } from "../features/utils/getNavItems";

type IconName =
  | "approval"
  | "request"
  | "equipment"
  | "equipmentTypes"
  | "recipe"
  | "management"
  | "wip_builder"
  | "wip_management"
  | "manager_dashboard";

const navIconPaths: Record<IconName, React.ReactNode> = {
  request: (
    <>
      <path d="M7 3.5h7l3 3V20.5H7z" />
      <path d="M14 3.5v3h3" />
      <path d="M9.5 11h5" />
      <path d="M9.5 14.5h5" />
    </>
  ),
  approval: (
    <>
      <path d="M12 3.5l7 3v5.2c0 4.1-2.7 7.2-7 8.8-4.3-1.6-7-4.7-7-8.8V6.5z" />
      <path d="M8.8 12.2l2.1 2.1 4.4-4.6" />
    </>
  ),
  wip_builder: (
    <>
      <path d="M4 7h16" />
      <path d="M4 12h16" />
      <path d="M4 17h16" />
      <path d="M8 4v16" />
      <path d="M16 4v16" />
    </>
  ),
  wip_management: (
    <>
      <path d="M5 16.5V7.5l7-4 7 4v9l-7 4z" />
      <path d="M5.5 8l6.5 3.7L18.5 8" />
      <path d="M12 12v8" />
    </>
  ),
  manager_dashboard: (
    <>
      <path d="M4.5 19.5h15" />
      <path d="M7 16v-5" />
      <path d="M12 16V6" />
      <path d="M17 16v-8" />
    </>
  ),
  management: (
    <>
      <path d="M12 8.2a3.8 3.8 0 1 0 0 7.6 3.8 3.8 0 0 0 0-7.6z" />
      <path d="M19.2 13.4v-2.8l-2-.5a6.3 6.3 0 0 0-.8-1.9l1-1.8-2-2-1.8 1a6.3 6.3 0 0 0-1.9-.8l-.5-2h-2.8l-.5 2a6.3 6.3 0 0 0-1.9.8l-1.8-1-2 2 1 1.8a6.3 6.3 0 0 0-.8 1.9l-2 .5v2.8l2 .5c.2.7.4 1.3.8 1.9l-1 1.8 2 2 1.8-1c.6.4 1.2.6 1.9.8l.5 2h2.8l.5-2c.7-.2 1.3-.4 1.9-.8l1.8 1 2-2-1-1.8c.4-.6.6-1.2.8-1.9z" />
    </>
  ),
  equipmentTypes: (
    <>
      <path d="M5 6.5h14" />
      <path d="M5 12h14" />
      <path d="M5 17.5h14" />
      <path d="M8 4v5" />
      <path d="M16 9.5v5" />
      <path d="M10.5 15v5" />
    </>
  ),
  equipment: (
    <>
      <path d="M6 8h12v8H6z" />
      <path d="M9 5h6v3H9z" />
      <path d="M9 16v3" />
      <path d="M15 16v3" />
      <path d="M8.5 11.8h7" />
    </>
  ),
  recipe: (
    <>
      <path d="M7 4.5h10v15H7z" />
      <path d="M9.5 8h5" />
      <path d="M9.5 11.5h5" />
      <path d="M9.5 15h3" />
    </>
  ),
};

function NavIcon({ page }: Readonly<{ page: Page }>) {
  const paths = navIconPaths[page as IconName];

  return (
    <svg className="nav-icon" viewBox="0 0 24 24" aria-hidden="true">
      <g fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        {paths}
      </g>
    </svg>
  );
}

type Props = {
  readonly children: React.ReactNode;
  readonly currentPage: Page;
  readonly navItems: readonly NavItem[];
  readonly userName?: string;
  readonly onNavigate: (page: Page) => void;
  readonly onLogout: () => void;
};

function Layout({
  children,
  currentPage,
  navItems,
  userName,
  onNavigate,
  onLogout,
}: Readonly<Props>) {
  const isItemActive = (item: NavItem) =>
    currentPage === item.page || item.subItems?.some((sub) => sub.page === currentPage);

  return (
    <>
      <div className="topbar">
        <div className="logo">LAB SYSTEM</div>

        <div className="user-section">
          {userName && <span>{userName}</span>}
          <button onClick={onLogout} className="logout-btn">
            Logout
          </button>
        </div>
      </div>

      <div className="container">
        <div className="sidebar">
          {navItems.map((item) =>
            item.subItems ? (
              <div
                key={item.page}
                className={`nav-group menu-item ${isItemActive(item) ? "active" : ""}`}
              >
                <div className="nav-row">
                  <NavIcon page={item.page} />
                  <span className="nav-label">{item.label}</span>
                  <span className="nav-caret">v</span>
                </div>
                <div className="sub-menu">
                  {item.subItems.map((sub) => (
                    <button
                      key={sub.page}
                      type="button"
                      className={`menu-item sub-menu-item ${currentPage === sub.page ? "active" : ""}`}
                      onClick={(event) => {
                        event.stopPropagation();
                        onNavigate(sub.page);
                      }}
                    >
                      <NavIcon page={sub.page} />
                      <span className="nav-label">{sub.label}</span>
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              <button
                key={item.page}
                type="button"
                className={`menu-item ${currentPage === item.page ? "active" : ""}`}
                onClick={() => onNavigate(item.page)}
              >
                <NavIcon page={item.page} />
                <span className="nav-label">{item.label}</span>
              </button>
            )
          )}
        </div>

        <div className="content">{children}</div>
      </div>
    </>
  );
}

export default Layout;
