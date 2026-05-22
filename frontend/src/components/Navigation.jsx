import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './Navigation.css';

const Navigation = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="navigation">
      <div className="nav-brand">
        <h1>🚀 Onboarding Platform</h1>
      </div>
      
      <div className="nav-menu">
        <button 
          className={`nav-item ${isActive('/dashboard') ? 'active' : ''}`}
          onClick={() => navigate('/dashboard')}
        >
          📊 Dashboard
        </button>
        
        <button 
          className={`nav-item ${isActive('/profile') ? 'active' : ''}`}
          onClick={() => navigate('/profile')}
        >
          👤 Profile
        </button>
      </div>

      <div className="nav-user">
        <div className="user-info">
          <span className="user-name">{user?.prenom} {user?.nom}</span>
          <span className={`user-role ${user?.role.toLowerCase()}`}>
            {user?.role}
          </span>
        </div>
        
        <div className="nav-actions">
          <button 
            className="profile-btn"
            onClick={() => navigate('/profile')}
          >
            👤 My Profile
          </button>
          <button 
            className="dashboard-btn"
            onClick={() => navigate('/dashboard')}
          >
            🏠 Return to Dashboard
          </button>
          <button 
            className="notifications-btn"
            onClick={() => navigate('/notifications')}
          >
            🔔 Go to Notifications
          </button>
          <button className="logout-btn" onClick={handleLogout}>
            🚪 Logout
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navigation;
