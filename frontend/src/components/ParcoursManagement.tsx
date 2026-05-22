import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import parcoursApi, { Parcours, ParcoursRequest, Etape, EtapeRequest } from '../services/parcoursApi';

import PageHeader from './layout/PageHeader';
import './ParcoursManagement.css';

const ParcoursManagement: React.FC = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [parcoursList, setParcoursList] = useState<Parcours[]>([]);
  const [selectedParcours, setSelectedParcours] = useState<Parcours | null>(null);
  const [etapesList, setEtapesList] = useState<Etape[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showParcoursForm, setShowParcoursForm] = useState(false);
  const [showEtapeForm, setShowEtapeForm] = useState(false);
  const [showAiForm, setShowAiForm] = useState(false);
  const [aiPrompt, setAiPrompt] = useState('');
  const [editingParcours, setEditingParcours] = useState<Parcours | null>(null);
  const [editingEtape, setEditingEtape] = useState<Etape | null>(null);

  // Confirmation modal states
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [confirmationType, setConfirmationType] = useState<'parcours' | 'etape'>('parcours');
  const [itemToDelete, setItemToDelete] = useState<number | null>(null);
  const [itemName, setItemName] = useState('');

  // Form states
  const [newParcours, setNewParcours] = useState<ParcoursRequest>({
    nom: '',
    description: '',
    categorieCible: '',
    departementCible: '',
    dureeGlobaleEstimee: 30,
    deadlineGlobaleParDefaut: 45,
    statut: 'ACTIF'
  });

  const [newEtape, setNewEtape] = useState<EtapeRequest>({
    nom: '',
    description: '',
    resourceLinks: '',
    type: 'ADMINISTRATIF',
    dureeEstimee: 1,
    ordreExecution: 1,
    requiertDocument: false,
    parcoursId: 0
  });

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    fetchParcours();
  }, [isAuthenticated, navigate]);

  const fetchParcours = async () => {
    try {
      const parcours = await parcoursApi.getAllParcours();
      setParcoursList(parcours);
      setError(null);
    } catch (error: any) {
      console.error('Error fetching parcours:', error);
      setError('Failed to fetch parcours. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const fetchEtapes = async (parcoursId: number) => {
    try {
      const etapes = await parcoursApi.getEtapesByParcoursId(parcoursId);
      setEtapesList(etapes);
    } catch (error: any) {
      console.error('Error fetching etapes:', error);
    }
  };

  const handleCreateParcours = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingParcours) {
        await parcoursApi.updateParcours(editingParcours.id, newParcours);
        setEditingParcours(null);
      } else {
        await parcoursApi.createParcours(newParcours);
      }
      setShowParcoursForm(false);
      setNewParcours({
        nom: '',
        description: '',
        categorieCible: '',
        departementCible: '',
        dureeGlobaleEstimee: 30,
        deadlineGlobaleParDefaut: 45,
        statut: 'ACTIF'
      });
      fetchParcours();
    } catch (error: any) {
      console.error('Error creating/updating parcours:', error);
      setError('Failed to create/update parcours. Please try again.');
    }
  };

  const handleCreateEtape = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedParcours) return;
    
    try {
      if (editingEtape) {
        await parcoursApi.updateEtape(editingEtape.id, { 
        ...newEtape, 
        parcoursId: selectedParcours.id,
        requiertDocument: newEtape.requiertDocument || false
      });
        setEditingEtape(null);
      } else {
        const etapeData = { 
        ...newEtape, 
        parcoursId: selectedParcours.id,
        requiertDocument: newEtape.requiertDocument || false
      };
        await parcoursApi.createEtape(etapeData);
      }
      setShowEtapeForm(false);
      setNewEtape({
        nom: '',
        description: '',
        resourceLinks: '',
        type: 'ADMINISTRATIF',
        dureeEstimee: 1,
        ordreExecution: 1,
        requiertDocument: false,
        parcoursId: 0
      });
      await fetchParcours();
      // Refresh the étapes list for the selected parcours
      if (selectedParcours) {
        await fetchEtapes(selectedParcours.id);
      }
    } catch (error) {
      console.error('Error creating/updating etape:', error);
      setError('Failed to create/update parcours. Please try again.');
    }
  };

  const handleDeleteParcours = async (id: number, name: string) => {
    setConfirmationType('parcours');
    setItemToDelete(id);
    setItemName(name);
    setShowConfirmation(true);
  };

  const handleDeleteEtape = async (id: number, name: string) => {
    setConfirmationType('etape');
    setItemToDelete(id);
    setItemName(name);
    setShowConfirmation(true);
  };

  const confirmDelete = async () => {
    if (itemToDelete === null) return;

    try {
      if (confirmationType === 'parcours') {
        await parcoursApi.deleteParcours(itemToDelete);
        fetchParcours();
      } else {
        await parcoursApi.deleteEtape(itemToDelete);
        if (selectedParcours) {
          fetchEtapes(selectedParcours.id);
        }
      }
    } catch (error: any) {
      console.error(`Error deleting ${confirmationType}:`, error);
      setError(`Failed to delete ${confirmationType}. Please try again.`);
    } finally {
      setShowConfirmation(false);
      setItemToDelete(null);
      setItemName('');
    }
  };

  const cancelDelete = () => {
    setShowConfirmation(false);
    setItemToDelete(null);
    setItemName('');
  };

  const handleSelectParcours = (parcours: Parcours) => {
    setSelectedParcours(parcours);
    fetchEtapes(parcours.id);
  };

  const handleEditParcours = (parcours: Parcours) => {
    setEditingParcours(parcours);
    setNewParcours({
      nom: parcours.nom,
      description: parcours.description || '',
      categorieCible: parcours.categorieCible,
      departementCible: parcours.departementCible,
      dureeGlobaleEstimee: parcours.dureeGlobaleEstimee || 30,
      deadlineGlobaleParDefaut: parcours.deadlineGlobaleParDefaut || 45,
      statut: parcours.statut
    });
    setShowParcoursForm(true);
  };

  const handleEditEtape = (etape: Etape) => {
    setEditingEtape(etape);
    setNewEtape({
      nom: etape.nom,
      description: etape.description || '',
      resourceLinks: etape.resourceLinks || '',
      type: etape.type,
      dureeEstimee: etape.dureeEstimee || 1,
      ordreExecution: etape.ordreExecution || 1,
      requiertDocument: etape.requiertDocument || false,
      parcoursId: etape.parcoursId
    });
    setShowEtapeForm(true);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleGenerateEtapesWithAI = async (parcoursId: number) => {
    try {
      setLoading(true);
      const response = await parcoursApi.generateEtapesWithAI(parcoursId);
      console.log('AI Generated Etapes:', response);
      await fetchEtapes(parcoursId);
      alert('Étapes générées et adaptées au parcours (anciennes étapes remplacées).');
    } catch (error: any) {
      console.error('Error generating etapes with AI:', error);
      setError('Failed to generate etapes with AI. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateFullParcoursWithAI = async () => {
    if (!aiPrompt.trim()) {
      alert('Veuillez décrire le parcours que vous souhaitez créer.');
      return;
    }
    try {
      setGenerating(true);
      setError(null);
      const result = await parcoursApi.generateFullParcoursWithAI(aiPrompt);
      console.log('AI Generated Full Parcours:', result);
      const etapeCount =
        result.etapes?.length ??
        result.etapeCount ??
        0;
      setShowAiForm(false);
      setAiPrompt('');
      await fetchParcours();
      if (result.id) {
        setSelectedParcours(result);
        if (result.etapes && result.etapes.length > 0) {
          setEtapesList(result.etapes);
        } else {
          await fetchEtapes(result.id);
        }
      }
      alert(`Parcours "${result.nom}" créé avec succès avec ${etapeCount} étape${etapeCount !== 1 ? 's' : ''} !`);
    } catch (error: any) {
      console.error('Error generating full parcours with AI:', error);
      const serverMsg = error.response?.data;
      const msg = typeof serverMsg === 'string' ? serverMsg : (serverMsg?.message || error.message);
      setError('Erreur: ' + msg);
    } finally {
      setGenerating(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="parcours-management">
      <PageHeader
        title="Parcours et étapes"
        subtitle="Créez et organisez les parcours d'onboarding de votre organisation."
      />

      {error && <div className="error-message">{error}</div>}

      <div className="management-container">
        {/* Parcours Section */}
        <div className="section">
          <div className="section-header">
            <h2>Parcours</h2>
            <div className="header-actions">
              <button 
                onClick={() => setShowAiForm(true)} 
                className="btn btn-purple"
                disabled={generating}
                title="Crée un parcours complet avec toutes ses étapes via l'IA"
              >
                {generating ? '⏳ Génération...' : '🤖 Générer avec IA'}
              </button>
              <button 
                onClick={() => setShowParcoursForm(true)} 
                className="btn btn-success"
              >
                ➕ Create Parcours
              </button>
            </div>
          </div>

          <div className="parcours-grid">
            {parcoursList.map(parcours => (
              <div 
                key={parcours.id} 
                className={`parcours-card ${selectedParcours?.id === parcours.id ? 'selected' : ''}`}
                onClick={() => handleSelectParcours(parcours)}
              >
                <h3>{parcours.nom}</h3>
                <p><strong>Category:</strong> {parcours.categorieCible}</p>
                <p><strong>Department:</strong> {parcours.departementCible}</p>
                <p><strong>Status:</strong> <span className={`status ${parcours.statut.toLowerCase()}`}>{parcours.statut}</span></p>
                {parcours.description && <p><strong>Description:</strong> {parcours.description}</p>}
                <div className="card-actions">
                  <button onClick={() => handleEditParcours(parcours)} className="btn btn-info btn-sm">
                    ✏️ Edit
                  </button>
                  <button onClick={() => handleDeleteParcours(parcours.id, parcours.nom)} className="btn btn-danger btn-sm">
                    🗑️ Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Etapes Section */}
        {selectedParcours && (
          <div className="section">
            <div className="section-header">
              <h2>Etapes for {selectedParcours.nom}</h2>
              <div className="header-actions">
                <button 
                  onClick={() => setShowEtapeForm(true)} 
                  className="btn btn-success"
                >
                  ➕ Create Etape
                </button>
                <button 
                  onClick={() => handleGenerateEtapesWithAI(selectedParcours.id)} 
                  className="btn btn-purple"
                  title="Analyse le nom, la description, la catégorie et le département du parcours (ex. «Backend developer» → étapes Spring Boot)."
                >
                  🤖 Generate with AI
                </button>
              </div>
            </div>
            <p className="ai-hint">
              La génération analyse le nom, la description, la catégorie et le département (backend/Spring, fullstack, DevOps, data, QA…).
              Les étapes existantes sont remplacées. Incluez des mots-clés précis dans le titre (ex. « Développeur Backend Spring Boot »).
            </p>

            <div className="etapes-list">
              {etapesList.map(etape => (
                <div key={etape.id} className="etape-card">
                  <h4>{etape.nom}</h4>
                  {etape.description && <p><strong>Description:</strong> {etape.description}</p>}
                  {etape.resourceLinks && (
                    <div className="resource-links">
                      <p><strong>Resources:</strong></p>
                      <div className="links-container">
                        {etape.resourceLinks.split(',').map((link, index) => {
                          const trimmedLink = link.trim();
                          if (trimmedLink.startsWith('http')) {
                            return (
                              <a 
                                key={index}
                                href={trimmedLink} 
                                target="_blank" 
                                rel="noopener noreferrer"
                                className="resource-link"
                              >
                                🔗 {trimmedLink.includes('youtube') ? 'YouTube Tutorial' : 'Resource Link'}
                              </a>
                            );
                          }
                          return <span key={index} className="resource-text">• {trimmedLink}</span>;
                        })}
                      </div>
                    </div>
                  )}
                  <p><strong>Type:</strong> {etape.type}</p>
                  <p><strong>Duration:</strong> {etape.dureeEstimee} days</p>
                  <p><strong>Order:</strong> {etape.ordreExecution}</p>
                  <div className="card-actions">
                    <button onClick={() => handleEditEtape(etape)} className="btn btn-info btn-sm">
                      ✏️ Edit
                    </button>
                    <button onClick={() => handleDeleteEtape(etape.id, etape.nom)} className="btn btn-danger btn-sm">
                      🗑️ Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Parcours Form Modal */}
      {showParcoursForm && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>{editingParcours ? 'Edit Parcours' : 'Create New Parcours'}</h3>
            <form onSubmit={handleCreateParcours}>
              <div className="form-group">
                <label>Name *</label>
                <input
                  type="text"
                  className="form-input"
                  value={newParcours.nom}
                  onChange={(e) => setNewParcours({...newParcours, nom: e.target.value})}
                  required
                  placeholder="Enter parcours name..."
                />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea
                  className="form-textarea"
                  value={newParcours.description}
                  onChange={(e) => setNewParcours({...newParcours, description: e.target.value})}
                  placeholder="Enter a detailed description of the parcours..."
                  rows={4}
                />
              </div>
              <div className="form-group">
                <label>Target Category *</label>
                <input
                  type="text"
                  className="form-input"
                  value={newParcours.categorieCible}
                  onChange={(e) => setNewParcours({...newParcours, categorieCible: e.target.value})}
                  required
                  placeholder="Enter target category..."
                />
              </div>
              <div className="form-group">
                <label>Target Department *</label>
                <input
                  type="text"
                  className="form-input"
                  value={newParcours.departementCible}
                  onChange={(e) => setNewParcours({...newParcours, departementCible: e.target.value})}
                  required
                  placeholder="Enter target department..."
                />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Estimated Duration (days)</label>
                  <input
                    type="number"
                    className="form-input"
                    value={newParcours.dureeGlobaleEstimee}
                    onChange={(e) => setNewParcours({...newParcours, dureeGlobaleEstimee: parseInt(e.target.value)})}
                    placeholder="30"
                    min="1"
                  />
                </div>
                <div className="form-group">
                  <label>Default Deadline (days)</label>
                  <input
                    type="number"
                    className="form-input"
                    value={newParcours.deadlineGlobaleParDefaut}
                    onChange={(e) => setNewParcours({...newParcours, deadlineGlobaleParDefaut: parseInt(e.target.value)})}
                    placeholder="45"
                    min="1"
                  />
                </div>
              </div>
              <div className="form-group">
                <label>Status</label>
                <select
                  className="form-select"
                  value={newParcours.statut}
                  onChange={(e) => setNewParcours({...newParcours, statut: e.target.value as 'ACTIF' | 'DESACTIVE'})}
                >
                  <option value="ACTIF">Active</option>
                  <option value="DESACTIVE">Inactive</option>
                </select>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">{editingParcours ? 'Update' : 'Create'}</button>
                <button type="button" onClick={() => {
                  setShowParcoursForm(false);
                  setEditingParcours(null);
                  setNewParcours({
                    nom: '',
                    description: '',
                    categorieCible: '',
                    departementCible: '',
                    dureeGlobaleEstimee: 30,
                    deadlineGlobaleParDefaut: 45,
                    statut: 'ACTIF'
                  });
                }} className="btn btn-secondary">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Etape Form Modal */}
      {showEtapeForm && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>{editingEtape ? 'Edit Etape' : 'Create New Etape'}</h3>
            <form onSubmit={handleCreateEtape}>
              <div className="form-group">
                <label>Name *</label>
                <input
                  type="text"
                  className="form-input"
                  value={newEtape.nom}
                  onChange={(e) => setNewEtape({...newEtape, nom: e.target.value})}
                  required
                  placeholder="Enter etape name..."
                />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea
                  className="form-textarea"
                  value={newEtape.description || ''}
                  onChange={(e) => {
                    const updated = { ...newEtape, description: e.target.value };
                    setNewEtape(updated);
                  }}
                  rows={3}
                  placeholder="Enter a detailed description of the etape..."
                />
              </div>
              <div className="form-group">
                <label>Resource Links</label>
                <textarea
                  className="form-textarea"
                  value={newEtape.resourceLinks || ''}
                  onChange={(e) => {
                    const updated = { ...newEtape, resourceLinks: e.target.value };
                    setNewEtape(updated);
                  }}
                  rows={2}
                  placeholder="Enter links separated by commas (e.g., https://youtube.com/watch?v=example, https://docs.example.com)"
                />
              </div>
              <div className="form-group">
                <label>Type *</label>
                <select
                  className="form-select"
                  value={newEtape.type}
                  onChange={(e) => setNewEtape({...newEtape, type: e.target.value as 'ADMINISTRATIF' | 'TECHNIQUE' | 'HUMAIN'})}
                  required
                >
                  <option value="ADMINISTRATIF">Administratif</option>
                  <option value="TECHNIQUE">Technique</option>
                  <option value="HUMAIN">Humain</option>
                </select>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Duration (days)</label>
                  <input
                    type="number"
                    className="form-input"
                    value={newEtape.dureeEstimee || ''}
                    onChange={(e) => setNewEtape({...newEtape, dureeEstimee: parseInt(e.target.value) || 0})}
                    placeholder="1"
                    min="1"
                  />
                </div>
                <div className="form-group">
                  <label>Execution Order</label>
                  <input
                    type="number"
                    className="form-input"
                    value={newEtape.ordreExecution || ''}
                    onChange={(e) => setNewEtape({...newEtape, ordreExecution: parseInt(e.target.value) || 0})}
                    placeholder="1"
                    min="1"
                  />
                </div>
              </div>
              
              <div className="form-group">
                <div className="form-checkbox">
                  <input
                    type="checkbox"
                    name="requiertDocument"
                    checked={newEtape.requiertDocument || false}
                    onChange={(e) => setNewEtape({...newEtape, requiertDocument: e.target.checked})}
                  />
                  <div className="checkbox-custom"></div>
                  <span>Requires Document Upload</span>
                </div>
              </div>
              
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">{editingEtape ? 'Update' : 'Create'}</button>
                <button type="button" onClick={() => {
                  setShowEtapeForm(false);
                  setEditingEtape(null);
                  setNewEtape({
                    nom: '',
                    type: 'ADMINISTRATIF',
                    dureeEstimee: 1,
                    ordreExecution: 1,
                    requiertDocument: false,
                    parcoursId: 0
                  });
                }} className="btn btn-secondary">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* AI Generation Modal */}
      {showAiForm && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>🤖 Générer un parcours complet avec l'IA</h3>
            <p style={{ marginBottom: '15px', color: '#666' }}>
              Décrivez le parcours d'onboarding que vous souhaitez créer. L'IA va générer le parcours 
              et toutes ses étapes automatiquement.
            </p>
            <div className="form-group">
              <label>Description du parcours *</label>
              <textarea
                className="form-textarea"
                value={aiPrompt}
                onChange={(e) => setAiPrompt(e.target.value)}
                rows={6}
                placeholder="Ex: Crée un parcours d'onboarding pour un développeur Java Spring Boot débutant dans le département IT. Il doit apprendre les microservices, Docker, Kubernetes, et les API REST. Durée 30 jours."
                disabled={generating}
                style={{ width: '100%', padding: '12px', fontSize: '14px' }}
              />
            </div>
            <div className="form-actions">
              <button 
                onClick={handleGenerateFullParcoursWithAI} 
                className="btn btn-primary"
                disabled={generating || !aiPrompt.trim()}
              >
                {generating ? '⏳ Génération en cours...' : '🤖 Générer le parcours'}
              </button>
              <button 
                type="button" 
                onClick={() => {
                  setShowAiForm(false);
                  setAiPrompt('');
                }} 
                className="btn btn-secondary"
                disabled={generating}
              >
                Annuler
              </button>
            </div>
            {generating && (
              <p style={{ marginTop: '10px', color: '#666', fontStyle: 'italic' }}>
                L'IA génère le parcours, cela peut prendre 15-30 secondes...
              </p>
            )}
          </div>
        </div>
      )}

      {/* Custom Confirmation Modal */}
      {showConfirmation && (
        <div className="confirmation-modal">
          <div className="confirmation-content">
            <div className="confirmation-icon">🗑️</div>
            <h2 className="confirmation-title">
              Delete {confirmationType === 'parcours' ? 'Parcours' : 'Etape'}?
            </h2>
            <p className="confirmation-message">
              Are you sure you want to delete "{itemName}"? This action cannot be undone.
            </p>
            <div className="confirmation-actions">
              <button onClick={confirmDelete} className="confirm-btn">
                Delete
              </button>
              <button onClick={cancelDelete} className="cancel-btn">
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ParcoursManagement;
