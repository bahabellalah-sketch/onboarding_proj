import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { 
  assignmentApi, 
  usersApi, 
  parcoursApiForAssignment, 
  Assignment, 
  AssignmentRequest, 
  Checklist,
  User, 
  Parcours 
} from '../services/assignmentApi';
import StepDocumentUpload from './StepDocumentUpload';
import ConfirmationModal from './ConfirmationModal';
import './AssignmentManagement.css';
import PageHeader from './layout/PageHeader';
import EvaluationComponent from './EvaluationComponent';
import AssignmentEvaluationPanel from './evaluations/AssignmentEvaluationPanel';

const AssignmentManagement: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [collaborateurs, setCollaborateurs] = useState<User[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [parcours, setParcours] = useState<Parcours[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [selectedAssignment, setSelectedAssignment] = useState<Assignment | null>(null);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [assignmentToDelete, setAssignmentToDelete] = useState<number | null>(null);
  const [assignmentNameToDelete, setAssignmentNameToDelete] = useState<string>('');
  const [selectedParcours, setSelectedParcours] = useState<Parcours | null>(null);
  const [showAssignmentForm, setShowAssignmentForm] = useState(false);
  const [selectedCollaborateur, setSelectedCollaborateur] = useState<User | null>(null);
  const [checklists, setChecklists] = useState<{ [key: number]: Checklist[] }>({});
  
  // Form states
  const [newAssignment, setNewAssignment] = useState<AssignmentRequest>({
    userId: 0,
    parcoursId: 0,
    dateDebut: new Date().toISOString().split('T')[0]
  });

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    
    fetchAssignments();
    
    // Seuls les admins/managers peuvent voir les collaborateurs et parcours
    if (user?.role === 'ADMINISTRATEUR' || user?.role === 'MANAGER') {
      fetchCollaborateurs();
      fetchParcours();
    }
  }, [isAuthenticated, navigate, user?.role]);

  const fetchAssignments = async () => {
    try {
      let data;
      
      // Utiliser le bon endpoint selon le rôle
      if (user?.role === 'COLLABORATEUR') {
        data = await assignmentApi.getMyAssignments();
      } else {
        data = await assignmentApi.getAllAssignments();
      }
      
      setAssignments(data);
    } catch (error: any) {
      console.error('Error fetching assignments:', error);
      setError('Failed to fetch assignments');
    } finally {
      setLoading(false);
    }
  };

  const fetchCollaborateurs = async () => {
    try {
      const data = await usersApi.getAllUsers();
      setCollaborateurs(data.filter(u => u.role === 'COLLABORATEUR'));
    } catch (error: any) {
      console.error('Error fetching collaborators:', error);
    }
  };

  const fetchParcours = async () => {
    try {
      const data = await parcoursApiForAssignment.getAllParcoursActifs();
      setParcours(data);
    } catch (error: any) {
      console.error('Error fetching parcours:', error);
    }
  };

  const handleAssignParcours = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newAssignment.userId || !newAssignment.parcoursId) {
      setError('Veuillez sélectionner un collaborateur et un parcours.');
      return;
    }

    try {
      await assignmentApi.assignerParcours(newAssignment);
      setShowAssignmentForm(false);
      setNewAssignment({
        userId: 0,
        parcoursId: 0,
        dateDebut: new Date().toISOString().split('T')[0]
      });
      setSelectedCollaborateur(null);
      setSelectedParcours(null);
      fetchAssignments();
      setError(null);
    } catch (error: any) {
      console.error('Error assigning parcours:', error);
      const data = error?.response?.data;
      const serverMessage =
        (typeof data === 'string' ? data : null) ||
        data?.message ||
        data?.error ||
        error?.message;
      setError(
        serverMessage && typeof serverMessage === 'string'
          ? serverMessage
          : 'Failed to assign parcours. Please try again.'
      );
    }
  };

  const handleUpdateStatut = async (assignmentId: number, newStatut: string) => {
    console.log('Updating status:', { assignmentId, newStatut });
    
    // Validate checklist completion before allowing TERMINE status
    if (newStatut === 'TERMINE') {
      console.log('Checking checklist completion for assignment:', assignmentId);
      console.log('Available checklists:', Object.keys(checklists));
      
      // Fetch checklists if not already loaded
      if (!checklists[assignmentId]) {
        console.log('Fetching checklists for assignment:', assignmentId);
        await fetchChecklists(assignmentId);
      }
      
      const assignmentChecklists = checklists[assignmentId];
      console.log('Assignment checklists:', assignmentChecklists);
      
      if (assignmentChecklists && Array.isArray(assignmentChecklists) && assignmentChecklists.length > 0) {
        // Check if any checklist item is not completed (status not TERMINE)
        const incompleteItems = assignmentChecklists.filter(item => item.statut !== 'TERMINE');
        console.log('Incomplete items:', incompleteItems);
        console.log('Checklist item statuses:', assignmentChecklists.map(item => ({ id: item.id, statut: item.statut })));
        
        if (incompleteItems.length > 0) {
          const errorMessage = `Impossible de marquer comme terminé. ${incompleteItems.length} élément(s) de la checklist ne sont pas terminés.`;
          console.log('Blocking status change:', errorMessage);
          setError(errorMessage);
          return;
        }
        
        // Additional validation: Check if any checklist item is EN_ATTENTE
        const enAttenteItems = assignmentChecklists.filter(item => item.statut === 'EN_ATTENTE');
        if (enAttenteItems.length > 0) {
          const errorMessage = `Impossible de marquer comme terminé. ${enAttenteItems.length} élément(s) de la checklist sont en attente.`;
          console.log('Blocking status change due to EN_ATTENTE items:', errorMessage);
          setError(errorMessage);
          return;
        }
      } else {
        console.log('No checklists found or empty checklist for assignment:', assignmentId);
        // If no checklists exist, allow the status change
      }
    }

    try {
      console.log('Proceeding with status update');
      await assignmentApi.updateStatutAssignment(assignmentId, newStatut);
      fetchAssignments();
    } catch (error: any) {
      console.error('Error updating status:', error);
      setError('Failed to update status. Please try again.');
    }
  };

  const handleUpdateAvancement = async (assignmentId: number, pourcentage: number) => {
    try {
      await assignmentApi.updateAvancement(assignmentId, pourcentage);
      fetchAssignments();
    } catch (error: any) {
      console.error('Error updating progress:', error);
      setError('Failed to update progress. Please try again.');
    }
  };

  const fetchChecklists = async (assignmentId: number) => {
    try {
      const checklistsData = await assignmentApi.getChecklistsByAssignmentId(assignmentId);
      setChecklists(prev => ({ ...prev, [assignmentId]: checklistsData }));
    } catch (error: any) {
      console.error('Error fetching checklists:', error);
    }
  };

  const handleUpdateChecklistStatut = async (checklistId: number, newStatut: string) => {
    try {
      await assignmentApi.updateChecklistStatut(checklistId, newStatut);
      
      // Rafraîchir les checklists pour toutes les assignments
      Object.keys(checklists).forEach(assignmentId => {
        fetchChecklists(Number(assignmentId));
      });
      
      // Rafraîchir les assignations pour mettre à jour la progression et le statut
      await fetchAssignments();
      
    } catch (error: any) {
      console.error('Error updating checklist status:', error);
      // Display the actual error message from backend
      const errorMessage = error.response?.data || error.message || 'Failed to update checklist status. Please try again.';
      console.log('Backend error message:', errorMessage);
      setError(errorMessage);
      // Also alert the error for immediate visibility
      alert('Backend Error: ' + errorMessage);
    }
  };

  const toggleChecklists = (assignmentId: number) => {
    if (checklists[assignmentId]) {
      setChecklists(prev => {
        const newChecklists = { ...prev };
        delete newChecklists[assignmentId];
        return newChecklists;
      });
    } else {
      fetchChecklists(assignmentId);
    }
  };

  const handleDeleteAssignment = async (id: number) => {
    const assignment = assignments.find(a => a.id === id);
    if (!assignment) return;
    
    setAssignmentToDelete(id);
    setAssignmentNameToDelete(`Assignment for ${assignment.parcoursNom || 'Unknown'}`);
    setShowConfirmation(true);
  };

  const confirmDeleteAssignment = async () => {
    if (assignmentToDelete === null) return;
    
    try {
      await assignmentApi.deleteAssignment(assignmentToDelete);
      fetchAssignments();
    } catch (error: any) {
      console.error('Error deleting assignment:', error);
      setError('Failed to delete assignment. Please try again.');
    } finally {
      setShowConfirmation(false);
      setAssignmentToDelete(null);
      setAssignmentNameToDelete('');
    }
  };

  const cancelDeleteAssignment = () => {
    setShowConfirmation(false);
    setAssignmentToDelete(null);
    setAssignmentNameToDelete('');
  };

  const getStatutColor = (statut: string) => {
    switch (statut) {
      case 'EN_COURS': return '#3b82f6';
      case 'TERMINE': return '#10b981';
      case 'EN_RETARD': return '#ef4444';
      case 'EN_PAUSE': return '#f59e0b';
      case 'ANNULE': return '#6b7280';
      default: return '#6b7280';
    }
  };

  const getAvancementColor = (pourcentage: number) => {
    if (pourcentage >= 80) return '#10b981';
    if (pourcentage >= 50) return '#3b82f6';
    if (pourcentage >= 25) return '#f59e0b';
    return '#ef4444';
  };

  if (loading) {
    return <div className="loading">Chargement...</div>;
  }

  return (
    <div className="assignment-management">
        <PageHeader
          title={
            user?.role === 'ADMINISTRATEUR' || user?.role === 'MANAGER'
              ? 'Gestion des assignations'
              : 'Mes assignations'
          }
          subtitle={
            user?.role === 'COLLABORATEUR'
              ? 'Suivez vos parcours d\'onboarding et votre progression.'
              : 'Assignez des parcours aux collaborateurs et suivez leur avancement.'
          }
          actions={
            (user?.role === 'ADMINISTRATEUR' || user?.role === 'MANAGER') ? (
              <button
                type="button"
                onClick={() => setShowAssignmentForm(true)}
                className="btn btn-success"
              >
                + Assigner un parcours
              </button>
            ) : undefined
          }
        />

        <main className="dashboard-main">
          {error && (
            <div className="alert alert-danger">
              <span className="alert-icon">⚠️</span>
              <span className="alert-message">{error}</span>
              <button onClick={() => setError(null)} className="alert-close">×</button>
            </div>
          )}

          <div className="dashboard-content">
            {/* Assignations List */}
            <div className="assignments-section">
              <h2>Assignations Actives</h2>
              <div className="assignments-grid">
                {assignments.map((assignment) => (
                  <div key={assignment.id} className="assignment-card">
                    <div className="assignment-header">
                      <h3>{assignment.parcoursNom}</h3>
                      <span 
                        className="status-badge" 
                        style={{ backgroundColor: getStatutColor(assignment.statut) }}
                      >
                        {assignment.statut.replace('_', ' ')}
                      </span>
                    </div>
                    
                    <div className="assignment-details">
                      {user?.role !== 'COLLABORATEUR' && (
                        <>
                          <p><strong>Collaborateur:</strong> {assignment.userPrenom} {assignment.userName}</p>
                          <p><strong>Email:</strong> {assignment.userEmail}</p>
                        </>
                      )}
                      <p><strong>Date de début:</strong> {new Date(assignment.dateDebut).toLocaleDateString()}</p>
                      <p><strong>Fin prévisionnelle:</strong> {new Date(assignment.dateFinPrevisionnelle).toLocaleDateString()}</p>
                      {assignment.dateFinReelle && (
                        <p><strong>Fin réelle:</strong> {new Date(assignment.dateFinReelle).toLocaleDateString()}</p>
                      )}
                      <p><strong>Assigné par:</strong> {assignment.assignePar}</p>
                    </div>

                    <div className="progress-section">
                      <div className="progress-header">
                        <span>Avancement: {assignment.pourcentageAvancement}%</span>
                        {user?.role === 'COLLABORATEUR' && (
                          <span className="auto-progress-indicator">🔄 Progression automatique</span>
                        )}
                      </div>
                      <div className="progress-bar">
                        <div 
                          className="progress-fill" 
                          style={{ 
                            width: `${assignment.pourcentageAvancement}%`,
                            backgroundColor: getAvancementColor(assignment.pourcentageAvancement)
                          }}
                        />
                      </div>
                    </div>

                    <div className="assignment-actions">
                      {(user?.role === 'ADMINISTRATEUR' || user?.role === 'MANAGER') && (
                        <>
                          <select 
                            value={assignment.statut}
                            onChange={(e) => handleUpdateStatut(assignment.id, e.target.value)}
                            className="form-select"
                          >
                            <option value="EN_COURS">En cours</option>
                            <option value="TERMINE">Terminé</option>
                            <option value="EN_PAUSE">En pause</option>
                            <option value="EN_RETARD">En retard</option>
                            <option value="ANNULE">Annulé</option>
                          </select>
                          
                          <input
                            type="number"
                            min="0"
                            max="100"
                            value={assignment.pourcentageAvancement}
                            onChange={(e) => handleUpdateAvancement(assignment.id, parseInt(e.target.value))}
                            className="form-input"
                            placeholder="%"
                          />
                        </>
                      )}
                      
                      {(user?.role === 'ADMINISTRATEUR' || user?.role === 'MANAGER') && (
                        <button 
                          onClick={() => handleDeleteAssignment(assignment.id)}
                          className="btn btn-danger btn-sm"
                        >
                          🗑️ Supprimer
                        </button>
                      )}
                      
                      <button 
                        onClick={() => toggleChecklists(assignment.id)}
                        className="btn btn-info btn-sm"
                      >
                        📋 {checklists[assignment.id] ? 'Masquer' : 'Voir'} Checklists
                      </button>
                    </div>

                    <AssignmentEvaluationPanel
                      assignmentId={assignment.id}
                      parcoursNom={assignment.parcoursNom}
                      pourcentageAvancement={assignment.pourcentageAvancement}
                      statut={assignment.statut}
                    />

                    {/* Section Checklists */}
                    {checklists[assignment.id] && (
                      <div className="checklist-section">
                        <h4>📋 Tâches à accomplir</h4>
                        {checklists[assignment.id]!.length === 0 ? (
                          <p>Aucune checklist disponible pour cette assignation.</p>
                        ) : (
                          <div className="checklist-items">
                            {checklists[assignment.id]!.map((checklist) => (
                              <div key={checklist.id} className={`checklist-item ${!checklist.unlocked ? 'locked' : ''} ${checklist.lockedCompleted ? 'locked-completed' : ''}`}>
                                <div className="checklist-header">
                                  <h5>
                                    {checklist.titre}
                                    {!checklist.unlocked && 
                                      <span className="lock-icon">
                                        {checklist.lockedCompleted ? '✅🔒' : '🔒'}
                                      </span>
                                    }
                                  </h5>
                                  <span className={`checklist-status ${checklist.statut.toLowerCase()}`}>
                                    {checklist.statut.replace('_', ' ')}
                                  </span>
                                </div>
                                <p className="checklist-description">{checklist.description}</p>
                                <div className="checklist-meta">
                                  <span>Ordre: {checklist.ordre}</span>
                                  {checklist.obligatoire && <span className="obligatory">Obligatoire</span>}
                                  {checklist.dateRealisation && (
                                    <span>Fait le: {new Date(checklist.dateRealisation).toLocaleDateString()}</span>
                                  )}
                                  {!checklist.unlocked && !checklist.lockedCompleted && (
                                    <span className="locked-info">🔒 Verrouillé - Terminez l'étape {checklist.ordre! - 1} d'abord</span>
                                  )}
                                  {checklist.lockedCompleted && (
                                    <span className="completed-info">✅ Terminé et verrouillé</span>
                                  )}
                                </div>
                                <select 
                                  value={checklist.statut}
                                  onChange={(e) => handleUpdateChecklistStatut(checklist.id, e.target.value)}
                                  className="form-select"
                                  disabled={!checklist.unlocked}
                                >
                                  <option value="EN_ATTENTE">En attente</option>
                                  <option value="EN_COURS">En cours</option>
                                  <option value="TERMINE">Terminé</option>
                                  <option value="SAUTE">Sauté</option>
                                  <option value="BLOQUE">Bloqué</option>
                                </select>
                                {checklist.requiertDocument && (
                                  <StepDocumentUpload
                                    etapeId={checklist.etapeId || checklist.id}
                                    etapeNom={checklist.titre}
                                    assignmentId={checklist.assignmentId}
                                    isAdmin={user?.role === 'ADMINISTRATEUR' || user?.role === 'MANAGER'}
                                    unlocked={checklist.unlocked}
                                  />
                                )}
                                {/* Evaluation form - only show when checklist is completed */}
                                <EvaluationComponent
                                  checklistId={checklist.id}
                                  checklistTitle={checklist.titre}
                                  checklistStatus={checklist.statut}
                                  onEvaluationSubmitted={() => {
                                    // Refresh checklist data after evaluation
                                    fetchChecklists(assignment.id);
                                  }}
                                />
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </main>

        {/* Assignment Form Modal */}
        {showAssignmentForm && (
          <div className="modal-overlay">
            <div className="modal-content">
              <div className="modal-header">
                <h2>Assigner un parcours</h2>
                <button onClick={() => setShowAssignmentForm(false)} className="close-modal">×</button>
              </div>
              
              <form onSubmit={handleAssignParcours} className="assignment-form">
                <div className="form-group">
                  <label>Collaborateur *</label>
                  <select
                    className="form-select"
                    value={newAssignment.userId}
                    onChange={(e) => {
                      const userId = parseInt(e.target.value);
                      setNewAssignment({ ...newAssignment, userId });
                      const collaborateur = collaborateurs.find(c => c.id === userId);
                      setSelectedCollaborateur(collaborateur || null);
                    }}
                    required
                  >
                    <option value="">Sélectionner un collaborateur</option>
                    {collaborateurs.map(collaborateur => (
                      <option key={collaborateur.id} value={collaborateur.id}>
                        {collaborateur.prenom} {collaborateur.nom} - {collaborateur.email}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Parcours *</label>
                  <select
                    className="form-select"
                    value={newAssignment.parcoursId}
                    onChange={(e) => {
                      const parcoursId = parseInt(e.target.value);
                      setNewAssignment({ ...newAssignment, parcoursId });
                      const parcoursItem = parcours.find(p => p.id === parcoursId);
                      setSelectedParcours(parcoursItem || null);
                    }}
                    required
                  >
                    <option value="">Sélectionner un parcours</option>
                    {parcours.map(parcoursItem => (
                      <option key={parcoursItem.id} value={parcoursItem.id}>
                        {parcoursItem.nom} - {parcoursItem.dureeGlobaleEstimee} jours
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Date de début *</label>
                  <input
                    type="date"
                    className="form-input"
                    value={newAssignment.dateDebut}
                    onChange={(e) => setNewAssignment({ ...newAssignment, dateDebut: e.target.value })}
                    required
                  />
                </div>

                {selectedParcours && (
                  <div className="parcours-summary">
                    <h4>Résumé du parcours</h4>
                    <p><strong>Nom:</strong> {selectedParcours.nom}</p>
                    <p><strong>Description:</strong> {selectedParcours.description}</p>
                    <p><strong>Durée estimée:</strong> {selectedParcours.dureeGlobaleEstimee} jours</p>
                    <p><strong>Catégorie:</strong> {selectedParcours.categorieCible}</p>
                  </div>
                )}

                <div className="form-actions">
                  <button type="button" onClick={() => setShowAssignmentForm(false)} className="btn btn-secondary">
                    Annuler
                  </button>
                  <button type="submit" className="btn btn-primary">
                    Assigner le parcours
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      
      <ConfirmationModal
        isOpen={showConfirmation}
        title="Supprimer l'assignation"
        message={`Êtes-vous sûr de vouloir supprimer "${assignmentNameToDelete}"? Cette action est irréversible.`}
        confirmText="Supprimer"
        cancelText="Annuler"
        onConfirm={confirmDeleteAssignment}
        onCancel={cancelDeleteAssignment}
        type="danger"
      />
      </div>
  );
};

export default AssignmentManagement;
