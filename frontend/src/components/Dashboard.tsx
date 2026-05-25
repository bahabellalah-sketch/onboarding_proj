import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../contexts/AuthContext';
import api, { User, UserCreationRequest } from '../services/api';
import ReportModal from './ReportModal';
import ConfirmationModal from './ConfirmationModal';
import './Dashboard.css';
import PageHeader from './layout/PageHeader';

const Dashboard: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [filteredUsers, setFilteredUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [userToDelete, setUserToDelete] = useState<number | null>(null);
  const [userNameToDelete, setUserNameToDelete] = useState<string>('');

  // Filter states
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [departmentFilter, setDepartmentFilter] = useState('ALL');

  // Form state for creating users
  const [newUser, setNewUser] = useState<UserCreationRequest>({
    prenom: '',
    nom: '',
    email: '',
    password: '',
    role: 'COLLABORATEUR',
    poste: '',
    departement: '',
    managerId: undefined,
    dateEmbauche: '',
    typeContrat: 'CDI',
    adresse: '',
    telephone: '',
    cin: '',
    diplome: '',
  });

  const filterUsers = useCallback(() => {
    let filtered = users;

    // Filter by search term
    if (searchTerm) {
      filtered = filtered.filter(user => 
        `${user.prenom} ${user.nom}`.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.poste?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Filter by role
    if (roleFilter !== 'ALL') {
      filtered = filtered.filter(user => user.role === roleFilter);
    }

    // Filter by department
    if (departmentFilter !== 'ALL') {
      filtered = filtered.filter(user => user.departement === departmentFilter);
    }

    setFilteredUsers(filtered);
  }, [users, searchTerm, roleFilter, departmentFilter]);

  const fetchUsers = useCallback(async () => {
    try {
      const userList = await api.getAllUsers();
      setUsers(userList);
      setFilteredUsers(userList);
      setError(null);
    } catch (error: any) {
      console.error('Error fetching users:', error);
      setError('Failed to fetch users. Please try again.');
    } finally {
      setLoading(false);
    }
  }, []);

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.createUser(newUser);
      alert('User created successfully!');
      setNewUser({
        prenom: '',
        nom: '',
        email: '',
        password: '',
        role: 'COLLABORATEUR',
        poste: '',
        departement: '',
        managerId: undefined,
        dateEmbauche: '',
        typeContrat: 'CDI',
        adresse: '',
        telephone: '',
        cin: '',
        diplome: '',
      });
      setShowCreateForm(false);
      fetchUsers();
    } catch (error: any) {
      console.error('Error creating user:', error);
      setError('Failed to create user. Please try again.');
    }
  };

  const handleUpdateRole = async (userId: number, newRole: string) => {
    try {
      await api.updateUserRole(userId, newRole);
      alert('User role updated successfully!');
      fetchUsers();
    } catch (error: any) {
      console.error('Error updating user role:', error);
      setError('Failed to update user role. Please try again.');
    }
  };

  const handleUpdateStatus = async (userId: number, newStatus: boolean) => {
    try {
      await api.updateUserStatus(userId, newStatus);
      alert('User status updated successfully!');
      fetchUsers();
    } catch (error: any) {
      console.error('Error updating user status:', error);
      setError('Failed to update user status. Please try again.');
    }
  };

  const confirmDeleteUser = async () => {
    if (userToDelete === null) return;

    try {
      await api.deleteUser(userToDelete);
      fetchUsers();
    } catch (error: any) {
      console.error('Error deleting user:', error);
      setError('Failed to delete user. Please try again.');
    } finally {
      setShowConfirmation(false);
      setUserToDelete(null);
      setUserNameToDelete('');
    }
  };

  const cancelDeleteUser = () => {
    setShowConfirmation(false);
    setUserToDelete(null);
    setUserNameToDelete('');
  };

  const handleDeleteUser = async (userId: number) => {
    const user = users.find(u => u.id === userId);
    if (!user) return;
    
    setUserToDelete(userId);
    setUserNameToDelete(`${user.prenom} ${user.nom}`);
    setShowConfirmation(true);
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchUsers();
    }
  }, [isAuthenticated, fetchUsers]);

  useEffect(() => {
    filterUsers();
  }, [filterUsers]);

  if (!isAuthenticated) {
    return (
      <div className="dashboard">
        <div className="login-prompt">
          <h2>Please log in to access the dashboard</h2>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="dashboard">
        <div className="loading">Loading...</div>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <PageHeader
        title="Tableau de bord"
        subtitle={`Bienvenue ${user?.prenom} ${user?.nom} — gérez les utilisateurs et suivez l'activité de la plateforme.`}
        actions={
          user?.role === 'ADMINISTRATEUR' ? (
            <button
              type="button"
              onClick={() => setShowCreateForm(true)}
              className="btn btn-success btn-sm"
            >
              +ajouter un utilisateur
            </button>
          ) : undefined
        }
      />

      <main className="dashboard-main">
        {error && (
          <div className="error-message">
            {error}
            <button onClick={() => setError(null)} className="close-error">×</button>
          </div>
        )}

        <div className="dashboard-content">
          {/* module shortcuts: sidebar */}
          {false && (user?.role === 'ADMINISTRATEUR' || user?.role === 'MANAGER') && (
            <div className="actions-section">
              <button 
                onClick={() => {}} 
                className="btn btn-purple btn-sm"
              >
                � Manage Parcours & Etapes
              </button>
              <button 
                onClick={() => {}} 
                className="btn btn-primary btn-sm"
              >
                � Manage Assignments
              </button>
              <button 
                onClick={() => {}} 
                className="btn btn-secondary btn-sm"
              >
                � Analytics Dashboard
              </button>
              <button 
                onClick={() => {}} 
                className="btn btn-warning btn-sm"
              >
                �                 Notifications
              </button>
              {user?.role === 'MANAGER' && (
                <button 
                  onClick={() => {}} 
                  className="btn btn-purple btn-sm"
                >
                  👥 My Team
                </button>
              )}
            </div>
          )}
        {false && (user?.role === 'ADMINISTRATEUR') && (
            <div className="actions-section">
              <button 
                onClick={() => {}}
                className="btn btn-info btn-sm"
              >
                � Manage Users
              </button>
              <button 
                onClick={() => {}} 
                className="btn btn-danger btn-sm"
              >
                � Manage Reports
              </button>
              {user?.role === 'ADMINISTRATEUR' && (
                <button 
                  onClick={() => setShowCreateForm(true)} 
                  className="btn btn-success btn-sm"
                >
                  ➕ Create New User
                </button>
              )}
            </div>
          )}
          
          <div className="users-table-container">
            <h2>Liste des utilisateurs</h2>
            <div className="results-count">
              {filteredUsers.length} sur {users.length} utilisateurs
            </div>
            
            {/* Filter Controls */}
            <div className="filter-controls">
              <div className="form-group">
                <label htmlFor="search">Search:</label>
                <input
                  type="text"
                  id="search"
                  placeholder="Search by name, email, or position..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="form-input"
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="role-filter">Role:</label>
                <select
                  id="role-filter"
                  value={roleFilter}
                  onChange={(e) => setRoleFilter(e.target.value)}
                  className="form-select form-select-sm"
                >
                  <option value="ALL">All Roles</option>
                  <option value="ADMINISTRATEUR">Administrator</option>
                  <option value="MANAGER">Manager</option>
                  <option value="COLLABORATEUR">Collaborateur</option>
                </select>
              </div>
              
              <div className="form-group">
                <label htmlFor="dept-filter">Department:</label>
                <select
                  id="dept-filter"
                  value={departmentFilter}
                  onChange={(e) => setDepartmentFilter(e.target.value)}
                  className="form-select form-select-sm"
                >
                  <option value="ALL">All Departments</option>
                  {Array.from(new Set(users.filter(user => user.departement).map(user => user.departement))).map(dept => (
                    <option key={dept} value={dept}>{dept}</option>
                  ))}
                </select>
              </div>
              
              <button
                onClick={() => {
                  setSearchTerm('');
                  setRoleFilter('ALL');
                  setDepartmentFilter('ALL');
                }}
                className="btn btn-secondary btn-xs"
              >
                Clear Filters
              </button>
            </div>

            <div className="users-table-container">
                <table className="users-table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Role</th>
                      <th>Position</th>
                      <th>Department</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredUsers.map((userItem) => (
                      <tr key={userItem.id} className="user-row">
                        <td className="name-cell">
                          {userItem.prenom} {userItem.nom}
                        </td>
                        <td className="email-cell">{userItem.email}</td>
                        <td>
                          <span className={`role-badge ${userItem.role.toLowerCase()}`}>
                            {userItem.role}
                          </span>
                        </td>
                        <td>{userItem.poste || '-'}</td>
                        <td>{userItem.departement || '-'}</td>
                        <td className="actions-cell">
                          <div className="actions">
                            {user?.role === 'ADMINISTRATEUR' && (
                              <select
                                value={userItem.role}
                                onChange={(e) => handleUpdateRole(userItem.id, e.target.value)}
                                className="form-select form-select-sm"
                              >
                                <option value="ADMINISTRATEUR">Administrateur</option>
                                <option value="MANAGER">Manager</option>
                                <option value="COLLABORATEUR">Collaborateur</option>
                              </select>
                            )}
                            <button
                              onClick={() => {
                                setSelectedUser(userItem);
                                setShowReportModal(true);
                              }}
                              className="btn btn-warning btn-sm"
                              title="Signaler cet utilisateur"
                            >
                              ⚠️
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
          </div>
        </div>
      </main>

      {showCreateForm && (
        <div className="modal-overlay">
          <div className="modal">
            <h2>Create New User</h2>
            <form onSubmit={handleCreateUser} className="create-form">
              <div className="form-grid">
                <div className="form-group">
                  <label>First Name *</label>
                  <input
                    type="text"
                    value={newUser.prenom}
                    onChange={(e) => setNewUser({...newUser, prenom: e.target.value})}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label>Last Name *</label>
                  <input
                    type="text"
                    value={newUser.nom}
                    onChange={(e) => setNewUser({...newUser, nom: e.target.value})}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label>Email *</label>
                  <input
                    type="email"
                    value={newUser.email}
                    onChange={(e) => setNewUser({...newUser, email: e.target.value})}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Password *</label>
                  <input
                    type="password"
                    value={newUser.password}
                    onChange={(e) => setNewUser({...newUser, password: e.target.value})}
                    required
                    minLength={8}
                  />
                </div>

                <div className="form-group">
                  <label>Role *</label>
                  <select
                    value={newUser.role}
                    onChange={(e) => setNewUser({...newUser, role: e.target.value})}
                    required
                  >
                    <option value="ADMINISTRATEUR">Administrator</option>
                    <option value="MANAGER">Manager</option>
                    <option value="COLLABORATEUR">Collaborator</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Position</label>
                  <input
                    type="text"
                    value={newUser.poste}
                    onChange={(e) => setNewUser({...newUser, poste: e.target.value})}
                  />
                </div>

                <div className="form-group">
                  <label>Department</label>
                  <input
                    type="text"
                    value={newUser.departement}
                    onChange={(e) => setNewUser({...newUser, departement: e.target.value})}
                  />
                </div>

                <div className="form-group">
                  <label>Manager</label>
                  <select
                    value={newUser.managerId || ''}
                    onChange={(e) => setNewUser({...newUser, managerId: e.target.value ? parseInt(e.target.value) : undefined})}
                  >
                    <option value="">No Manager</option>
                    {users.filter(u => u.role === 'MANAGER' || u.role === 'ADMINISTRATEUR').map(manager => (
                      <option key={manager.id} value={manager.id}>
                        {manager.prenom} {manager.nom} ({manager.role})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Hire Date</label>
                  <input
                    type="date"
                    value={newUser.dateEmbauche}
                    onChange={(e) => setNewUser({...newUser, dateEmbauche: e.target.value})}
                  />
                </div>

                <div className="form-group">
                  <label>Contract Type</label>
                  <select
                    value={newUser.typeContrat}
                    onChange={(e) => setNewUser({...newUser, typeContrat: e.target.value})}
                  >
                    <option value="">Select</option>
                    <option value="CDI">CDI</option>
                    <option value="CDD">CDD</option>
                    <option value="CVIP">CVIP</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Phone</label>
                  <input
                    type="tel"
                    value={newUser.telephone}
                    onChange={(e) => setNewUser({...newUser, telephone: e.target.value})}
                  />
                </div>

                <div className="form-group">
                  <label>CIN</label>
                  <input
                    type="text"
                    value={newUser.cin}
                    onChange={(e) => setNewUser({...newUser, cin: e.target.value})}
                  />
                </div>

                <div className="form-group">
                  <label>Diploma</label>
                  <input
                    type="text"
                    value={newUser.diplome}
                    onChange={(e) => setNewUser({...newUser, diplome: e.target.value})}
                  />
                </div>

                <div className="form-group full-width">
                  <label>Address</label>
                  <input
                    type="text"
                    value={newUser.adresse}
                    onChange={(e) => setNewUser({...newUser, adresse: e.target.value})}
                  />
                </div>
              </div>
              
              <div className="form-actions">
                <button type="button" onClick={() => setShowCreateForm(false)} className="cancel-button">
                  Cancel
                </button>
                <button type="submit" className="submit-button">
                  Create User
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      
      {showReportModal && selectedUser && (
        <ReportModal
          isOpen={showReportModal}
          onClose={() => {
            setShowReportModal(false);
            setSelectedUser(null);
          }}
          userId={selectedUser.id}
          userName={`${selectedUser.prenom} ${selectedUser.nom}`}
        />
      )}
      
      <ConfirmationModal
        isOpen={showConfirmation}
        title="Delete User"
        message={`Are you sure you want to delete "${userNameToDelete}"? This action cannot be undone.`}
        confirmText="Delete"
        cancelText="Cancel"
        onConfirm={confirmDeleteUser}
        onCancel={cancelDeleteUser}
        type="danger"
      />
    </div>
  );
};

export default Dashboard;
