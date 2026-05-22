import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import apiService from '../services/api';

const primaryBtn: React.CSSProperties = {
  width: '100%',
  padding: '12px',
  backgroundColor: '#007bff',
  color: 'white',
  border: 'none',
  borderRadius: '4px',
  fontSize: '16px',
  cursor: 'pointer',
  fontWeight: 500,
};

const secondaryBtn: React.CSSProperties = {
  ...primaryBtn,
  backgroundColor: '#6c757d',
};

const EmailVerification = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    const token = searchParams.get('token');

    if (!token) {
      setStatus('error');
      setMessage('Lien de vérification invalide. Aucun jeton fourni.');
      return;
    }

    const verify = async () => {
      try {
        setStatus('loading');
        setMessage('Vérification de votre adresse email en cours...');

        const result = await apiService.verifyEmail(token);

        if (result.success) {
          setStatus('success');
          setMessage(result.message || 'Votre adresse email a été vérifiée avec succès.');
        } else {
          setStatus('error');
          setMessage(result.message || 'Le lien de vérification est invalide ou a expiré.');
        }
      } catch (error: unknown) {
        setStatus('error');
        const err = error as { response?: { data?: { message?: string } } };
        setMessage(
          err.response?.data?.message ||
            'Impossible de vérifier votre email. Le lien est peut-être expiré ou déjà utilisé.'
        );
      }
    };

    verify();
  }, [searchParams]);

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
        <div style={{ fontSize: '48px', marginBottom: '20px' }}>
          {status === 'loading' && '⏳'}
          {status === 'success' && '✅'}
          {status === 'error' && '❌'}
        </div>
        <h2 style={{ marginBottom: '16px', color: '#333' }}>
          {status === 'loading' && 'Vérification en cours'}
          {status === 'success' && 'Email vérifié'}
          {status === 'error' && 'Échec de la vérification'}
        </h2>
        <p
          style={{
            fontSize: '16px',
            color: status === 'success' ? '#28a745' : status === 'error' ? '#dc3545' : '#6c757d',
            marginBottom: '24px',
            lineHeight: 1.5,
          }}
        >
          {message}
        </p>
        {status === 'success' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <button type="button" onClick={() => navigate('/profile')} style={primaryBtn}>
              Aller à mon profil
            </button>
            <button type="button" onClick={() => navigate('/login')} style={secondaryBtn}>
              Aller à la connexion
            </button>
          </div>
        )}
        {status === 'error' && (
          <>
            <button type="button" onClick={() => navigate('/profile')} style={primaryBtn}>
              Retourner au profil
            </button>
            <p style={{ marginTop: '20px', fontSize: '14px', color: '#6c757d' }}>
              Vous pouvez demander un nouvel email de vérification depuis votre page de profil.
            </p>
          </>
        )}
      </div>
    </div>
  );
};

export default EmailVerification;
