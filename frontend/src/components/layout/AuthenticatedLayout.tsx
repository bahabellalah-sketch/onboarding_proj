import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import ProtectedRoute from '../ProtectedRoute';
import AppLayout from './AppLayout';

/**
 * Layout pour toutes les pages connectées : vérification email + menu latéral.
 */
const AuthenticatedLayout: React.FC = () => {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <ProtectedRoute>
      <AppLayout />
    </ProtectedRoute>
  );
};

export default AuthenticatedLayout;
