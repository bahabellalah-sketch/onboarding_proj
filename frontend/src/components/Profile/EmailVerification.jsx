import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import profileService from '../../services/profileService';
import './EmailVerification.css';

const EmailVerification = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    const token = searchParams.get('token');
    
    if (!token) {
      setStatus('error');
      setError('Token de vérification manquant');
      return;
    }

    verifyEmail(token);
  }, [searchParams]);

  const verifyEmail = async (token) => {
    try {
      setStatus('loading');
      setMessage('Vérification de votre email en cours...');

      const response = await profileService.verifyEmail(token);
      
      setStatus('success');
      setMessage(response.message);
      
      // Rediriger vers la page de profil après 3 secondes
      setTimeout(() => {
        navigate('/profile');
      }, 3000);
      
    } catch (err) {
      setStatus('error');
      setError(err.message);
    }
  };

  const handleRetry = () => {
    navigate('/profile');
  };

  const renderContent = () => {
    switch (status) {
      case 'loading':
        return (
          <div className="verification-loading">
            <div className="loading-spinner">
              <div className="spinner"></div>
            </div>
            <h2>Vérification en cours...</h2>
            <p>{message}</p>
          </div>
        );

      case 'success':
        return (
          <div className="verification-success">
            <div className="success-icon">✅</div>
            <h2>Email vérifié avec succès !</h2>
            <p>{message}</p>
            <div className="redirect-info">
              <p>Vous allez être redirigé vers votre profil dans quelques instants...</p>
              <button className="btn btn-primary" onClick={() => navigate('/profile')}>
                Aller à mon profil
              </button>
            </div>
          </div>
        );

      case 'error':
        return (
          <div className="verification-error">
            <div className="error-icon">❌</div>
            <h2>Échec de la vérification</h2>
            <p>{error}</p>
            <div className="error-actions">
              <button className="btn btn-primary" onClick={handleRetry}>
                Retourner au profil
              </button>
              <button className="btn btn-secondary" onClick={() => window.location.reload()}>
                Réessayer
              </button>
            </div>
            <div className="error-help">
              <h3>Problèmes possibles :</h3>
              <ul>
                <li>Le lien de vérification a expiré (24 heures)</li>
                <li>Le token a déjà été utilisé</li>
                <li>Le token est invalide</li>
              </ul>
              <p>
                Si vous rencontrez toujours des problèmes, veuillez retourner à votre profil 
                pour demander un nouvel email de vérification.
              </p>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="email-verification">
      <div className="verification-container">
        <div className="verification-header">
          <h1>Vérification d'Email</h1>
          <p>Plateforme d'Onboarding</p>
        </div>
        
        <div className="verification-content">
          {renderContent()}
        </div>
      </div>
    </div>
  );
};

export default EmailVerification;
