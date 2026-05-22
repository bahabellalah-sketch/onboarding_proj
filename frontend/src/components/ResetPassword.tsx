import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import apiService from '../services/api';

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<'form' | 'success' | 'error'>('form');
  const [message, setMessage] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  useEffect(() => {
    const token = searchParams.get('token');
    if (!token) {
      setStatus('error');
      setMessage('Lien invalide. Aucun jeton fourni.');
    }
  }, [searchParams]);

  const handleResetPassword = async () => {
    const token = searchParams.get('token');
    if (!token) {
      setStatus('error');
      setMessage('Token invalide');
      return;
    }

    if (!newPassword || !confirmPassword) {
      setStatus('error');
      setMessage('Veuillez remplir tous les champs');
      return;
    }

    if (!/[A-Z]/.test(newPassword)) {
      setStatus('error');
      setMessage('Le mot de passe doit contenir au moins une majuscule');
      return;
    }

    if (newPassword !== confirmPassword) {
      setStatus('error');
      setMessage('Les mots de passe ne correspondent pas');
      return;
    }

    try {
      await apiService.resetPassword(token, newPassword);
      setStatus('success');
      setMessage('Mot de passe défini avec succès !');
    } catch {
      setStatus('error');
      setMessage('La réinitialisation du mot de passe a échoué. Le lien est peut-être expiré.');
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f5f5f5',
        padding: '20px',
      }}
    >
      <div
        style={{
          backgroundColor: 'white',
          padding: '40px',
          borderRadius: '10px',
          boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
          width: '100%',
          maxWidth: '500px',
          textAlign: 'center',
        }}
      >
        <h2 style={{ marginBottom: '16px', color: '#333' }}>Définir votre mot de passe</h2>

        <p
          style={{
            fontSize: '16px',
            color: status === 'success' ? '#28a745' : status === 'error' ? '#dc3545' : '#6c757d',
            marginBottom: '24px',
            lineHeight: 1.5,
          }}
        >
          {status === 'form'
            ? 'Choisissez un nouveau mot de passe pour votre compte.'
            : message}
        </p>

        {status === 'form' && (
          <div style={{ marginBottom: '20px' }}>
            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', textAlign: 'left' }}>Nouveau mot de passe:</label>
              <input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="Entrez votre nouveau mot de passe"
                style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '16px' }}
              />
            </div>
            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', textAlign: 'left' }}>Confirmer le mot de passe:</label>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Confirmez votre nouveau mot de passe"
                style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '16px' }}
              />
            </div>
            <button
              type="button"
              onClick={handleResetPassword}
              style={{
                width: '100%',
                padding: '12px',
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                fontSize: '16px',
                cursor: 'pointer',
                fontWeight: 500,
              }}
            >
              Enregistrer le mot de passe
            </button>
          </div>
        )}

        {status !== 'form' && (
          <button
            type="button"
            onClick={() => navigate('/login')}
            style={{
              padding: '12px 30px',
              backgroundColor: status === 'success' ? '#28a745' : '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '5px',
              fontSize: '16px',
              cursor: 'pointer',
              fontWeight: 500,
            }}
          >
            Aller à la page de connexion
          </button>
        )}
      </div>
    </div>
  );
};

export default ResetPassword;
