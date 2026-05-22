import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import userService from '../services/userService';
import './UserManagement.css';
import PageHeader from './layout/PageHeader';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [filteredUsers, setFilteredUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [filterRole, setFilterRole] = useState('ALL');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const { user } = useAuth();

  useEffect(() => {
    loadUsers();
  }, []);

  useEffect(() => {
    filterUsers();
  }, [searchTerm, filterRole, filterStatus, allUsers]);

  const filterUsers = () => {
    let filtered = allUsers;

    // Filter by search term
    if (searchTerm) {
      filtered = filtered.filter(user => 
        `${user.prenom} ${user.nom}`.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.poste?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.departement?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Filter by role
    if (filterRole && filterRole !== 'ALL') {
      filtered = filtered.filter(user => user.role === filterRole);
    }

    // Filter by status
    if (filterStatus && filterStatus !== 'ALL') {
      filtered = filtered.filter(user => 
        filterStatus === 'ACTIVE' ? user.statut : !user.statut
      );
    }

    setFilteredUsers(filtered);
  };

  const loadUsers = async () => {
    try {
      setLoading(true);
      const userList = await userService.getAllUsers();
      setAllUsers(userList);
      setFilteredUsers(userList);
      setError('');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleActivateUser = async (userId) => {
    try {
      await userService.activateUser(userId);
      setSuccess('Utilisateur activé avec succès');
      loadUsers();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeactivateUser = async (userId) => {
    try {
      await userService.deactivateUser(userId);
      setSuccess('Utilisateur désactivé avec succès');
      loadUsers();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.message);
    }
  };

  
  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleFilterRole = (e) => {
    setFilterRole(e.target.value);
  };

  const handleFilterStatus = (e) => {
    setFilterStatus(e.target.value);
  };

  const clearFilters = () => {
    setSearchTerm('');
    setFilterRole('ALL');
    setFilterStatus('ALL');
  };

  const getStatusBadge = (user) => {
    return user.statut ? (
      <span className="status-badge active">Actif</span>
    ) : (
      <span className="status-badge inactive">Inactif</span>
    );
  };

  const getRoleBadge = (role) => {
    const roleColors = {
      'ADMINISTRATEUR': 'admin',
      'MANAGER': 'manager',
      'COLLABORATEUR': 'collaborator'
    };
    return <span className={`role-badge ${roleColors[role] || 'default'}`}>{role}</span>;
  };

  if (loading) {
    return (
      <div className="user-management">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Chargement des utilisateurs...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="user-management">
      <PageHeader
        title="Gestion des utilisateurs"
        subtitle="Activez, désactivez et supervisez les comptes de la plateforme."
      />

      {error && (
        <div className="alert alert-danger">
          <span className="alert-icon">⚠️</span>
          <span className="alert-message">{error}</span>
          <button className="alert-close" onClick={() => setError('')}>
            ×
          </button>
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          <span className="alert-icon">✅</span>
          <span className="alert-message">{success}</span>
          <button className="alert-close" onClick={() => setSuccess('')}>
            ×
          </button>
        </div>
      )}

      <div className="results-count">
        Affichage de {filteredUsers.length} sur {allUsers.length} utilisateurs
      </div>

      <div className="filters-section">
        <div className="filter-group">
          <input
            type="text"
            placeholder="Rechercher par nom, email, poste ou département..."
            value={searchTerm}
            onChange={handleSearch}
            className="form-input"
          />
        </div>
        
        <div className="filter-group">
          <select value={filterRole} onChange={handleFilterRole} className="form-select">
            <option value="ALL">Tous les rôles</option>
            <option value="ADMINISTRATEUR">Administrateurs</option>
            <option value="MANAGER">Managers</option>
            <option value="COLLABORATEUR">Collaborateurs</option>
          </select>
        </div>

        <div className="filter-group">
          <select value={filterStatus} onChange={handleFilterStatus} className="form-select">
            <option value="ALL">Tous les statuts</option>
            <option value="ACTIVE">Actifs</option>
            <option value="INACTIVE">Inactifs</option>
          </select>
        </div>

        <div className="filter-group">
          <button onClick={clearFilters} className="btn btn-secondary btn-sm">
            🔄 Effacer les filtres
          </button>
        </div>
      </div>

      <div className="users-table-container">
        <div className="users-grid">
          {/* Grid Header */}
          <div className="grid-header">
            <div className="grid-header-cell">ID</div>
            <div className="grid-header-cell">Nom</div>
            <div className="grid-header-cell">Email</div>
            <div className="grid-header-cell">Rôle</div>
            <div className="grid-header-cell">Statut</div>
            <div className="grid-header-cell">Date d'embauche</div>
            <div className="grid-header-cell">Actions</div>
          </div>
          
          {/* Grid Rows */}
          {filteredUsers.map((user) => (
            <div key={user.id} className="grid-row">
              <div className="grid-cell">{user.id}</div>
              <div className="grid-cell">{user.prenom} {user.nom}</div>
              <div className="grid-cell">{user.email}</div>
              <div className="grid-cell">{getRoleBadge(user.role)}</div>
              <div className="grid-cell">{getStatusBadge(user)}</div>
              <div className="grid-cell">{new Date(user.dateEmbauche).toLocaleDateString()}</div>
              <div className="grid-cell actions-cell">
                {user.statut ? (
                  <button
                    onClick={() => handleDeactivateUser(user.id)}
                    className="btn btn-sm btn-warning"
                    title="Désactiver"
                  >
                    Désactiver
                  </button>
                ) : (
                  <button
                    onClick={() => handleActivateUser(user.id)}
                    className="btn btn-sm btn-success"
                    title="Activer"
                  >
                    Activer
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
        
        {filteredUsers.length === 0 && !loading && (
          <div className="no-users">
            <div className="no-users-icon">👥</div>
            <p>Aucun utilisateur trouvé</p>
          </div>
        )}
    </div>
  );
};

export default UserManagement;
