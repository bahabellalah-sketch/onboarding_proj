import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate, useParams } from 'react-router-dom';
import { getToken } from '../../services/api';
import * as profileApi from '../../services/profileApi';
import '../../styles/DarkRedTheme.css';
import './ProfileManagement.css';
import PageHeader from '../layout/PageHeader';
import { API_BASE_URL, API_ORIGIN } from '../../config/apiConfig';

const ProfileManagement = () => {
  const { user: authUser } = useAuth();
  const navigate = useNavigate();
  const { userId: userIdParam } = useParams();

  const resolveCurrentUserId = useCallback(() => {
    if (authUser?.id) return Number(authUser.id);
    const token = getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.id ? Number(payload.id) : null;
    } catch {
      return null;
    }
  }, [authUser?.id]);

  const currentUserId = resolveCurrentUserId();
  const targetUserId = userIdParam ? Number(userIdParam) : currentUserId;
  const isOwnProfile = currentUserId != null && targetUserId === currentUserId;
  const isViewMode = !isOwnProfile;
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [activeSection, setActiveSection] = useState('profile');
  
  // Email verification state
  const [emailVerificationStatus, setEmailVerificationStatus] = useState({
    isVerified: false,
    hasPendingVerification: false,
    isExpired: false
  });
  const [isSendingVerification, setIsSendingVerification] = useState(false);
  
  // Profile update form
  const [profileForm, setProfileForm] = useState({
    prenom: '',
    nom: '',
    email: '',
    dateNaissance: '',
    telephone: '',
    adresse: '',
    poste: '',
    departement: '',
    cin: '',
    diplome: ''
  });
  
  // Password change form
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  useEffect(() => {
    if (targetUserId) {
      loadUserProfile(targetUserId);
    }
  }, [targetUserId]);

  const loadUserProfile = async (userId) => {
    setLoading(true);
    setError('');
    try {
      const token = getToken();
      if (!token) {
        setError('Utilisateur non connecté');
        return;
      }

      const data = await profileApi.getUserProfile(userId);
      
      setUser(data);
      setProfileForm({
        prenom: data.prenom || '',
        nom: data.nom || '',
        email: data.email || '',
        dateNaissance: data.dateNaissance ? String(data.dateNaissance).split('T')[0] : '',
        telephone: data.telephone || '',
        adresse: data.adresse || '',
        poste: data.poste || '',
        departement: data.departement || '',
        cin: data.cin || '',
        diplome: data.diplome || ''
      });
      
      const viewingOwn = currentUserId != null && userId === currentUserId;
      if (viewingOwn) {
        await fetchEmailVerificationStatus(userId);
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message;
      setError('Erreur lors du chargement du profil: ' + msg);
    } finally {
      setLoading(false);
    }
  };

  const fetchEmailVerificationStatus = async (userId) => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_BASE_URL}/email-verification/status/${userId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setEmailVerificationStatus(response.data);
    } catch (error) {
      console.error('Error fetching email verification status:', error);
    }
  };

  const handleSendVerificationEmail = async () => {
    setIsSendingVerification(true);
    try {
      const token = getToken();
      if (!token || !currentUserId) {
        setError('Utilisateur non connecté');
        return;
      }
      await axios.post(`${API_BASE_URL}/email-verification/send/${currentUserId}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSuccess('Email de vérification envoyé avec succès! Vérifiez votre boîte de réception.');
      await fetchEmailVerificationStatus(currentUserId);
    } catch (error) {
      setError('Erreur lors de l\'envoi de l\'email de vérification: ' + (error.response?.data || error.message));
    } finally {
      setIsSendingVerification(false);
    }
  };

  const handleResendVerificationEmail = async () => {
    setIsSendingVerification(true);
    try {
      const token = getToken();
      if (!token || !currentUserId) {
        setError('Utilisateur non connecté');
        return;
      }
      await axios.post(`${API_BASE_URL}/email-verification/resend/${currentUserId}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSuccess('Email de vérification renvoyé avec succès! Vérifiez votre boîte de réception.');
      await fetchEmailVerificationStatus(currentUserId);
    } catch (error) {
      setError('Erreur lors du renvoi de l\'email de vérification: ' + (error.response?.data || error.message));
    } finally {
      setIsSendingVerification(false);
    }
  };

  const handleProfileUpdate = async (e) => {
    e.preventDefault();
    if (!isOwnProfile) return;
    try {
      const data = await profileApi.updateUserProfile(targetUserId, profileForm);
      setUser(data);
      setSuccess('Profil mis à jour avec succès!');
      setTimeout(() => setSuccess(''), 3000);
    } catch (error) {
      setError('Erreur lors de la mise à jour du profil: ' + (error.response?.data || error.message));
    }
  };

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    if (!isOwnProfile) return;
    
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError('Les nouveaux mots de passe ne correspondent pas');
      return;
    }

    try {
      await profileApi.changePassword(
        targetUserId,
        passwordForm.currentPassword,
        passwordForm.newPassword
      );
      setSuccess('Mot de passe changé avec succès!');
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      setTimeout(() => setSuccess(''), 3000);
    } catch (error) {
      setError('Erreur lors du changement de mot de passe: ' + (error.response?.data || error.message));
    }
  };

  const handlePhotoUpload = async (e) => {
    if (!isOwnProfile) return;
    const file = e.target.files[0];
    if (!file) return;

    try {
      const photoUrl = await profileApi.uploadProfilePhoto(targetUserId, file);
      setUser(prev => ({ ...prev, profilePhotoUrl: photoUrl }));
      setSuccess('Photo de profil téléchargée avec succès!');
      setTimeout(() => setSuccess(''), 3000);
    } catch (error) {
      setError('Erreur lors du téléchargement de la photo: ' + (error.response?.data || error.message));
    }
  };

  if (loading) {
    return (
      <div className="profile-management">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>{isViewMode ? 'Chargement du profil...' : 'Chargement de votre profil...'}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-management">
      <PageHeader
        title={isViewMode ? 'Profil du collaborateur' : 'Mon profil'}
        subtitle={
          isViewMode
            ? `Consultation du profil de ${user?.prenom || ''} ${user?.nom || ''}`.trim()
            : 'Mettez à jour vos informations personnelles et votre sécurité.'
        }
        actions={
          isViewMode ? (
            <button type="button" className="btn btn-secondary btn-sm" onClick={() => navigate(-1)}>
              ← Retour
            </button>
          ) : undefined
        }
      />
      
      {error && (
        <div className="alert alert-error">
          <span className="alert-icon">⚠️</span>
          <span className="alert-message">{error}</span>
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          <span className="alert-icon">✅</span>
          <span className="alert-message">{success}</span>
        </div>
      )}

      {/* Profile Header with Photo */}
      <div className="profile-info-card">
        <div className="profile-avatar">
          {user?.profilePhotoUrl ? (
            <img 
              src={`${API_ORIGIN}${user.profilePhotoUrl}`} 
              alt="Profile" 
              className="profile-image"
            />
          ) : (
            <div className="profile-avatar-placeholder">
              {user?.prenom?.[0] || user?.email?.[0]?.toUpperCase()}
            </div>
          )}
          
          {isOwnProfile && (
            <div className="photo-upload">
              <input type="file" accept="image/*" onChange={handlePhotoUpload} id="photo-upload" />
              <label htmlFor="photo-upload" className="btn btn-secondary">
                📷 Changer la photo
              </label>
            </div>
          )}
        </div>
        
        <div className="user-details">
          <h2 className="user-name">
            {user?.prenom} {user?.nom}
          </h2>
          <p className="user-email">
            ✉️ {user?.email}
          </p>
          <div className="user-meta">
            <span className="user-role">
              👤 {user?.role}
            </span>
            <span className={`user-status ${user?.statut ? 'active' : 'inactive'}`}>
              {user?.statut ? '✅ Actif' : '⏸️ Inactif'}
            </span>
            {user?.managerName && (
              <span className="user-manager">👔 Manager : {user.managerName}</span>
            )}
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="profile-tabs">
        <button
          className={`profile-tab ${activeSection === 'profile' ? 'active' : ''}`}
          onClick={() => setActiveSection('profile')}
        >
          📋 Informations du Profil
        </button>
        
        {isOwnProfile && (
          <button
            className={`profile-tab ${activeSection === 'security' ? 'active' : ''}`}
            onClick={() => setActiveSection('security')}
          >
            🔒 Sécurité
          </button>
        )}
      </div>

      {/* Content Sections */}
      {activeSection === 'profile' && (
        <div className="profile-section">
          <h3>📋 Informations du Profil</h3>
          
          <form onSubmit={handleProfileUpdate} className={`profile-form ${isViewMode ? 'profile-form--readonly' : ''}`}>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Prénom</label>
                <input
                  type="text"
                  value={profileForm.prenom}
                  onChange={(e) => setProfileForm(prev => ({ ...prev, prenom: e.target.value }))}
                  className="form-input"
                  readOnly={isViewMode}
                  disabled={isViewMode}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Nom</label>
                <input
                  type="text"
                  value={profileForm.nom}
                  onChange={(e) => setProfileForm(prev => ({ ...prev, nom: e.target.value }))}
                  className="form-input"
                  readOnly={isViewMode}
                  disabled={isViewMode}
                />
              </div>
            </div>
            
            <div className="form-group">
              <label className="form-label">📧 Adresse Email</label>
              <div className="email-input-wrapper">
                <input
                  type="email"
                  value={profileForm.email}
                  onChange={(e) => setProfileForm(prev => ({ ...prev, email: e.target.value }))}
                  className="form-input email-input"
                  readOnly={isViewMode}
                  disabled={isViewMode}
                />
                {isOwnProfile && (
                  <div className="email-verification">
                    {emailVerificationStatus.isVerified ? (
                      <span className="verification-badge verified">
                        ✓ Vérifié
                      </span>
                    ) : emailVerificationStatus.hasPendingVerification ? (
                      <button
                        type="button"
                        onClick={handleResendVerificationEmail}
                        disabled={isSendingVerification}
                        className="btn btn-sm btn-warning"
                      >
                        {isSendingVerification ? 'Envoi...' : 'Renvoyer'}
                      </button>
                    ) : (
                      <button
                        type="button"
                        onClick={handleSendVerificationEmail}
                        disabled={isSendingVerification}
                        className="btn btn-sm btn-primary"
                      >
                        {isSendingVerification ? 'Envoi...' : 'Vérifier'}
                      </button>
                    )}
                  </div>
                )}
              </div>
              {isOwnProfile && !emailVerificationStatus.isVerified && (
                <div className="verification-message">
                  {emailVerificationStatus.isExpired 
                    ? '⚠️ Le lien de vérification a expiré. Veuillez en demander un nouveau.'
                    : '📧 Vérifiez votre email pour le lien de vérification.'
                  }
                </div>
              )}
            </div>
            
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">📅 Date de Naissance</label>
                <input
                  type="date"
                  value={profileForm.dateNaissance}
                  onChange={(e) => setProfileForm(prev => ({ ...prev, dateNaissance: e.target.value }))}
                  className="form-input"
                  readOnly={isViewMode}
                  disabled={isViewMode}
                />
              </div>
              <div className="form-group">
                <label className="form-label">📱 Téléphone</label>
                <input
                  type="tel"
                  value={profileForm.telephone}
                  onChange={(e) => setProfileForm(prev => ({ ...prev, telephone: e.target.value }))}
                  className="form-input"
                  readOnly={isViewMode}
                  disabled={isViewMode}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">🏠 Adresse</label>
              <input
                type="text"
                value={profileForm.adresse}
                onChange={(e) => setProfileForm(prev => ({ ...prev, adresse: e.target.value }))}
                className="form-input"
                readOnly={isViewMode}
                disabled={isViewMode}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label className="form-label">💼 Poste</label>
                <input
                  type="text"
                  value={profileForm.poste}
                  onChange={(e) => setProfileForm(prev => ({ ...prev, poste: e.target.value }))}
                  className="form-input"
                  readOnly={isViewMode}
                  disabled={isViewMode}
                />
              </div>
              <div className="form-group">
                <label className="form-label">🏢 Département</label>
                <input
                  type="text"
                  value={profileForm.departement}
                  onChange={(e) => setProfileForm(prev => ({ ...prev, departement: e.target.value }))}
                  className="form-input"
                  readOnly={isViewMode}
                  disabled={isViewMode}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label className="form-label">🆔 CIN</label>
                <input
                  type="text"
                  value={profileForm.cin}
                  onChange={(e) => setProfileForm(prev => ({ ...prev, cin: e.target.value }))}
                  className="form-input"
                  readOnly={isViewMode}
                  disabled={isViewMode}
                />
              </div>
              <div className="form-group">
                <label className="form-label">🎓 Diplôme</label>
                <input
                  type="text"
                  value={profileForm.diplome}
                  onChange={(e) => setProfileForm(prev => ({ ...prev, diplome: e.target.value }))}
                  className="form-input"
                  readOnly={isViewMode}
                  disabled={isViewMode}
                />
              </div>
            </div>
            
            {isOwnProfile && (
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">
                  💾 Mettre à jour le profil
                </button>
              </div>
            )}
          </form>
        </div>
      )}

      {isOwnProfile && activeSection === 'security' && (
        <div className="profile-section">
          <h3>🔒 Changer le mot de passe</h3>
          
          <form onSubmit={handlePasswordChange} className="profile-form">
            <div className="form-group">
              <label className="form-label">🔑 Mot de passe actuel</label>
              <input
                type="password"
                value={passwordForm.currentPassword}
                onChange={(e) => setPasswordForm(prev => ({ ...prev, currentPassword: e.target.value }))}
                required
                className="form-input"
              />
            </div>
            
            <div className="form-group">
              <label className="form-label">🔐 Nouveau mot de passe</label>
              <input
                type="password"
                value={passwordForm.newPassword}
                onChange={(e) => setPasswordForm(prev => ({ ...prev, newPassword: e.target.value }))}
                required
                className="form-input"
              />
            </div>
            
            <div className="form-group">
              <label className="form-label">🔐 Confirmer le nouveau mot de passe</label>
              <input
                type="password"
                value={passwordForm.confirmPassword}
                onChange={(e) => setPasswordForm(prev => ({ ...prev, confirmPassword: e.target.value }))}
                required
                className="form-input"
              />
            </div>
            
            <div className="form-actions">
              <button type="submit" className="btn btn-success">
                🔒 Changer le mot de passe
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default ProfileManagement;
