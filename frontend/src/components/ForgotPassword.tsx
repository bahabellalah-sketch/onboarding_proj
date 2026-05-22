import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';
import '../styles/ModernGlassTheme.css';
import './ForgotPassword.css';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [status, setStatus] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatus('loading');

    try {
      const result = await apiService.forgotPassword(email);
      
      if (result) {
        setStatus('success');
        setMessage('Un email de réinitialisation a été envoyé à votre adresse email');
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      } else {
        setStatus('error');
        setMessage('Une erreur est survenue');
      }
    } catch (error) {
      setStatus('error');
      setMessage('Une erreur est survenue. Veuillez réessayer plus tard.');
      console.error('Forgot password error:', error);
    }
  };

  return (
    <div className="forgot-password-container">
      <h2>Mot de passe oublié</h2>
      <p>Entrez votre adresse email pour recevoir un lien de réinitialisation</p>
      
      <form onSubmit={handleSubmit} className="forgot-password-form">
        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            placeholder="votre.email@example.com"
          />
        </div>
        
        <div className="form-group">
          <button type="submit" disabled={status === 'loading'} className="forgot-password-button">
            {status === 'loading' ? 'Envoi en cours...' : 'Envoyer le lien'}
          </button>
        </div>
        
        {message && (
          <div className={`message ${status}`}>
            {message}
          </div>
        )}
      </form>
    </div>
  );
};

export default ForgotPassword;
