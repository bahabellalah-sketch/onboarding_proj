import React from 'react';
import useEmailVerification from '../hooks/useEmailVerification';

const ProtectedRoute = ({ children }) => {
  const { isLoading } = useEmailVerification();

  if (isLoading) {
    return (
      <div className="app-auth-loading" role="status" aria-live="polite">
        <div className="app-auth-loading__panel">
          <div className="app-auth-loading__spinner" aria-hidden />
          <p className="app-auth-loading__text">Vérification de l&apos;email…</p>
        </div>
      </div>
    );
  }

  // If not verified, the hook will automatically redirect
  // If verified, render children
  return children;
};

export default ProtectedRoute;
