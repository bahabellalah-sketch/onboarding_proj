import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/DarkRedTheme.css';
import '../styles/ModernGlassTheme.css';
import './Login.css';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login, loading, error, clearError } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();
    await login({ email, password });
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h2>Plateforme Onboarding</h2>
        <p>Connectez-vous à votre compte</p>
        
        {error && (
          <div className="error-message">
            {typeof error === 'string' ? error : 'Erreur de connexion'}
          </div>
        )}
        
        <form onSubmit={handleSubmit} className="login-form">
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
            <label htmlFor="password">Mot de passe</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="••••••••"
            />
          </div>
          
          <button type="submit" disabled={loading} className="login-button">
            {loading ? 'Connexion en cours...' : 'Se connecter'}
          </button>
        </form>
        
        <div className="login-footer">
          <a
            href="#forgot-password"
            className="forgot-password-link"
            onClick={(e) => {
              e.preventDefault();
              navigate('/forgot-password');
            }}
          >
            Mot de passe oublié?
          </a>
        </div>
      </div>
    </div>
  );
};

export default Login;
