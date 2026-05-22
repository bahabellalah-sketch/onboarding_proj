import React, { useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { getNavItemsForRole, getPageTitle } from '../../config/navigation';
import '../../styles/DarkRedTheme.css';
import './AppLayout.css';

const AppLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const navItems = getNavItemsForRole(user?.role);
  const pageTitle = getPageTitle(location.pathname);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isNavActive = (path: string, matchPrefix?: boolean) => {
    if (matchPrefix) {
      return location.pathname === path || location.pathname.startsWith(`${path}/`);
    }
    return location.pathname === path;
  };

  return (
    <div className="app-shell">
      <button
        type="button"
        className="app-shell__overlay"
        aria-hidden={!sidebarOpen}
        onClick={() => setSidebarOpen(false)}
      />

      <aside className={`app-sidebar ${sidebarOpen ? 'app-sidebar--open' : ''}`}>
        <div className="app-sidebar__brand">
          <span className="app-sidebar__logo" aria-hidden>
            O
          </span>
          <div>
            <span className="app-sidebar__name">Onboarding</span>
            <span className="app-sidebar__tagline">Plateforme RH</span>
          </div>
        </div>

        <nav className="app-sidebar__nav" aria-label="Navigation principale">
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={!item.matchPrefix}
              className={({ isActive }) =>
                `app-sidebar__link ${isActive || isNavActive(item.path, item.matchPrefix) ? 'app-sidebar__link--active' : ''}`
              }
              onClick={() => setSidebarOpen(false)}
            >
              <span className="app-sidebar__icon" aria-hidden>
                {item.icon}
              </span>
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="app-sidebar__footer">
          <div className="app-sidebar__user">
            <span className="app-sidebar__avatar" aria-hidden>
              {(user?.prenom?.[0] || user?.email?.[0] || '?').toUpperCase()}
            </span>
            <div className="app-sidebar__user-text">
              <span className="app-sidebar__user-name">
                {user?.prenom} {user?.nom}
              </span>
              <span className={`app-sidebar__role app-sidebar__role--${(user?.role || '').toLowerCase()}`}>
                {user?.role}
              </span>
            </div>
          </div>
          <button type="button" className="app-sidebar__logout" onClick={handleLogout}>
            Déconnexion
          </button>
        </div>
      </aside>

      <div className="app-main">
        <header className="app-topbar">
          <button
            type="button"
            className="app-topbar__menu"
            aria-label="Ouvrir le menu"
            onClick={() => setSidebarOpen(true)}
          >
            ☰
          </button>
          <h2 className="app-topbar__title">{pageTitle}</h2>
        </header>

        <div className="app-content">
          <Outlet />
        </div>
      </div>
    </div>
  );
};

export default AppLayout;
