import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './TeamPage.css';
import PageHeader from './layout/PageHeader';
import './AssignmentManagement.css';
import './ProfileButton.css';
import TeamChat from './TeamChat';
import { API_BASE_URL } from '../config/apiConfig';

interface User {
  id: string;
  prenom: string;
  nom: string;
  email: string;
  role: string;
  poste?: string;
  departement?: string;
  managerId?: string;
  telephone?: string;
  dateEmbauche?: string;
  statut?: boolean;
}

interface TeamStats {
  totalMembers: number;
  activeMembers: number;
  departments: string[];
}

const CollaboratorTeamPage: React.FC = () => {
  const navigate = useNavigate();
  const { user: authUser, isAuthenticated } = useAuth();
  const isManager = authUser?.role === 'MANAGER';
  const isCollaborator = authUser?.role === 'COLLABORATEUR';
  const [teamMembers, setTeamMembers] = useState<User[]>([]);
  const [manager, setManager] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState<'name' | 'department' | 'role'>('name');
  const [expandedMember, setExpandedMember] = useState<string | null>(null);

  console.log('CollaboratorTeamPage: Component mounted, authUser:', authUser ? 'found' : 'not found', 'isAuthenticated:', isAuthenticated);

  useEffect(() => {
    console.log('CollaboratorTeamPage: useEffect running');
    const fetchUserData = async () => {
      try {
        // Use user from AuthContext
        console.log('CollaboratorTeamPage: authUser from context:', authUser ? 'found' : 'not found');
        if (!authUser) {
          console.log('CollaboratorTeamPage: No user found in context, navigating to login');
          navigate('/login');
          return;
        }

        if (authUser.role !== 'COLLABORATEUR' && authUser.role !== 'MANAGER') {
          navigate('/dashboard');
          return;
        }

        const userData = authUser;

        // Fetch team members using the new efficient endpoint
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/users/team-members`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
        if (!response.ok) {
          throw new Error('Failed to fetch team members');
        }

        const teamData: User[] = await response.json();
        
        console.log('DEBUG: API Response - team members:', teamData);
        console.log('DEBUG: Current user data from AuthContext:', userData);

        const members = teamData.filter((u) => u.role === 'COLLABORATEUR');

        // Collaborators: manager + peers. Managers: only their managed collaborators (API).
        if (userData.role === 'MANAGER') {
          setManager(null);
          setTeamMembers(members);
        } else {
          const managerData = teamData.find((u) => u.role === 'MANAGER');
          setManager(managerData ?? null);
          setTeamMembers(members);
        }
        console.log('DEBUG: Team members set:', members);
        console.log('CollaboratorTeamPage: Data loaded successfully');
      } catch (err) {
        console.log('CollaboratorTeamPage: Error occurred:', err);
        setError(err instanceof Error ? err.message : 'An error occurred');
      } finally {
        setLoading(false);
        console.log('CollaboratorTeamPage: Loading set to false');
      }
    };

    fetchUserData();
  }, [navigate, authUser]);

  // Calculate team statistics
  const getTeamStats = (): TeamStats => {
    const departmentSet = new Set(teamMembers.map(m => m.departement).filter((dept): dept is string => dept !== undefined));
    const departments = Array.from(departmentSet) as string[];
    const activeMembers = teamMembers.filter(m => m.statut !== false).length;
    
    return {
      totalMembers: teamMembers.length,
      activeMembers,
      departments
    };
  };

  // Filter and sort team members
  const getFilteredAndSortedMembers = () => {
    let filtered = teamMembers.filter(member => 
      member.prenom.toLowerCase().includes(searchTerm.toLowerCase()) ||
      member.nom.toLowerCase().includes(searchTerm.toLowerCase()) ||
      member.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (member.poste && member.poste.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (member.departement && member.departement.toLowerCase().includes(searchTerm.toLowerCase()))
    );

    return filtered.sort((a, b) => {
      switch (sortBy) {
        case 'name':
          return `${a.prenom} ${a.nom}`.localeCompare(`${b.prenom} ${b.nom}`);
        case 'department':
          return (a.departement || '').localeCompare(b.departement || '');
        case 'role':
          return a.role.localeCompare(b.role);
        default:
          return 0;
      }
    });
  };

  const handleRetry = () => {
    setError(null);
    setLoading(true);
    // Trigger useEffect again
    window.location.reload();
  };

  const toggleMemberExpansion = (memberId: string) => {
    setExpandedMember(expandedMember === memberId ? null : memberId);
  };

  console.log('CollaboratorTeamPage: About to render, loading:', loading, 'error:', error);

  const handleBack = () => {
    navigate(isManager ? '/assignments' : '/dashboard');
  };

  if (loading) {
    return (
      <div className="dashboard-container team-page">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Loading team information...</p>
          <div className="loading-progress">
            <div className="progress-bar"></div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-container team-page">
        <div className="error-message">
          <div className="error-icon">⚠️</div>
          <h3>Unable to Load Team Data</h3>
          <p>{error}</p>
          <div className="error-actions">
            {(isCollaborator || isManager) && (
                <button onClick={() => navigate('/profile')} className="btn btn-secondary">
                  👤 My Profile
                </button>
              )}
            <button onClick={handleRetry} className="btn btn-primary">
              🔄 Retry
            </button>
            <button onClick={handleBack} className="btn btn-secondary">
              ← Back to Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container team-page">
      <main className="dashboard-main">
        <PageHeader
          title="Mon équipe"
          subtitle={
            isManager
              ? 'Consultez et échangez avec les collaborateurs que vous managez.'
              : 'Retrouvez les membres de votre équipe et votre manager.'
          }
        />

        <div className="dashboard-content">
          {/* Team Statistics */}
          <div className="team-stats">
            <div className="stat-card stat-card--total">
              <div className="stat-number">{getTeamStats().totalMembers}</div>
              <div className="stat-label">{isManager ? 'Collaborators' : 'Team Members'}</div>
            </div>
            <div className="stat-card stat-card--active">
              <div className="stat-number">{getTeamStats().activeMembers}</div>
              <div className="stat-label">Active Members</div>
            </div>
            <div className="stat-card stat-card--departments">
              <div className="stat-number">{getTeamStats().departments.length}</div>
              <div className="stat-label">Departments</div>
            </div>
          </div>

          {/* Search and Filter Controls */}
          <div className="team-controls">
            <div className="search-container">
              <input
                type="text"
                placeholder="Search team members..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="search-input"
              />
              {searchTerm && (
                <button 
                  className="search-clear"
                  onClick={() => setSearchTerm('')}
                  aria-label="Clear search"
                >
                  ✕
                </button>
              )}
              <div className="search-loading"></div>
              {/* Search suggestions dropdown */}
              {searchTerm && getFilteredAndSortedMembers().length > 0 && (
                <div className="search-suggestions active">
                  {getFilteredAndSortedMembers().slice(0, 5).map((member) => (
                    <div 
                      key={member.id} 
                      className="search-suggestion-item"
                      onClick={() => {
                        setSearchTerm(`${member.prenom} ${member.nom}`);
                        // Optional: Navigate to member details or expand card
                      }}
                    >
                      <div className="search-suggestion-icon">
                        {member.prenom.charAt(0)}{member.nom.charAt(0)}
                      </div>
                      <div className="search-suggestion-text">
                        <div className="search-suggestion-name">
                          {member.prenom} {member.nom}
                        </div>
                        <div className="search-suggestion-role">
                          {member.role} {member.poste && `• ${member.poste}`}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
            <div className="sort-container">
              <label htmlFor="sort-select">Sort by:</label>
              <select
                id="sort-select"
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value as 'name' | 'department' | 'role')}
                className="sort-select"
              >
                <option value="name">Name</option>
                <option value="department">Department</option>
                <option value="role">Role</option>
              </select>
            </div>
          </div>
          {/* Manager Section (collaborators only) */}
          {isCollaborator && manager && (
            <div className="team-section">
              <h2>Manager</h2>
              <div className="member-card manager-card">
                <div className="member-info">
                  <div className="member-name">
                    {manager.prenom} {manager.nom}
                  </div>
                  <div className="member-email">{manager.email}</div>
                  <div className="member-role">
                    <span className="role-badge manager">{manager.role}</span>
                  </div>
                  {manager.poste && (
                    <div className="member-position">{manager.poste}</div>
                  )}
                </div>
              </div>
            </div>
          )}

          <TeamChat currentUserId={authUser?.id} />

          {/* Team Members Section */}
          <div className="team-section">
            <h2>
              {isManager ? 'My Collaborators' : 'Team Members'} ({getFilteredAndSortedMembers().length})
            </h2>
            {getFilteredAndSortedMembers().length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">🔍</div>
                <h3>No team members found</h3>
                <p>
                  {searchTerm 
                    ? `No results found for "${searchTerm}". Try a different search term.`
                    : isManager
                      ? 'No collaborators are assigned to you yet.'
                      : 'No team members available.'}
                </p>
              </div>
            ) : (
              <div className="team-grid">
                {getFilteredAndSortedMembers().map((member) => (
                  <div 
                    key={member.id} 
                    className={`member-card ${expandedMember === member.id ? 'expanded' : ''}`}
                    onClick={() => toggleMemberExpansion(member.id)}
                  >
                    <div className="member-header">
                      <div className="member-avatar">
                        {member.prenom.charAt(0)}{member.nom.charAt(0)}
                      </div>
                      <div className="member-basic-info">
                        <div className="member-name">
                          {member.prenom} {member.nom}
                        </div>
                        <div className="member-email">{member.email}</div>
                      </div>
                      <div className="member-status">
                        <span className={`status-indicator ${member.statut === false ? 'inactive' : 'active'}`}></span>
                        <span className="status-text">
                          {member.statut === false ? 'Inactive' : 'Active'}
                        </span>
                      </div>
                    </div>
                    
                    <div className="member-details">
                      <div className="member-role">
                        <span className="role-badge collaborator">{member.role}</span>
                      </div>
                      {member.poste && (
                        <div className="member-position">💼 {member.poste}</div>
                      )}
                      {member.departement && (
                        <div className="member-department">🏢 {member.departement}</div>
                      )}
                      {member.telephone && (
                        <div className="member-phone">📞 {member.telephone}</div>
                      )}
                      {member.dateEmbauche && (
                        <div className="member-hire-date">📅 Joined {new Date(member.dateEmbauche).toLocaleDateString()}</div>
                      )}
                      <button
                        type="button"
                        className="profile-button"
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/profile/${member.id}`);
                        }}
                      >
                        Voir le profil
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default CollaboratorTeamPage;
