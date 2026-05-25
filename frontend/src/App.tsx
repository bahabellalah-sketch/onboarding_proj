import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import AnalyticsDashboard from './components/AnalyticsDashboard';
import ProfileManagement from './components/Profile/ProfileManagement';
import UserManagement from './components/UserManagement';
import ParcoursManagement from './components/ParcoursManagement';
import AssignmentManagement from './components/AssignmentManagement';
import EmailVerification from './components/EmailVerification';
import ResetPassword from './components/ResetPassword';
import ForgotPassword from './components/ForgotPassword';
import NotificationsPage from './components/NotificationsPage';
import ReportManagement from './components/ReportManagement';
import CollaboratorTeamPage from './components/CollaboratorTeamPage';
import EvaluationManagementPage from './components/evaluations/EvaluationManagementPage';
import ManagerEvaluationsPage from './components/evaluations/ManagerEvaluationsPage';
import AuthenticatedLayout from './components/layout/AuthenticatedLayout';

function AppRoutes() {
  const { isAuthenticated, user, loading } = useAuth();

  const getDefaultRoute = () => {
    if (!isAuthenticated) return '/login';
    if (user?.role === 'COLLABORATEUR') return '/assignments';
    return '/dashboard';
  };

  if (loading) {
    return (
      <div className="app-auth-loading" role="status" aria-live="polite">
        <div className="app-auth-loading__panel">
          <div className="app-auth-loading__spinner" aria-hidden />
          <p className="app-auth-loading__text">Chargement…</p>
        </div>
      </div>
    );
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={!isAuthenticated ? <Login /> : <Navigate to={getDefaultRoute()} replace />}
      />

      <Route element={<AuthenticatedLayout />}>
        <Route
          path="/dashboard"
          element={
            isAuthenticated && user?.role !== 'COLLABORATEUR' ? (
              <Dashboard />
            ) : (
              <Navigate to={getDefaultRoute()} replace />
            )
          }
        />
        <Route path="/profile" element={<ProfileManagement />} />
        <Route path="/profile/:userId" element={<ProfileManagement />} />
        <Route
          path="/users"
          element={
            isAuthenticated && user?.role === 'ADMINISTRATEUR' ? (
              <UserManagement />
            ) : (
              <Navigate to={getDefaultRoute()} replace />
            )
          }
        />
        <Route path="/parcours" element={<ParcoursManagement />} />
        <Route path="/assignments" element={<AssignmentManagement />} />
        <Route
          path="/analytics"
          element={
            isAuthenticated && user?.role !== 'COLLABORATEUR' ? (
              <AnalyticsDashboard />
            ) : (
              <Navigate to={getDefaultRoute()} replace />
            )
          }
        />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route
          path="/reports"
          element={
            isAuthenticated && user?.role === 'ADMINISTRATEUR' ? (
              <ReportManagement />
            ) : (
              <Navigate to={getDefaultRoute()} replace />
            )
          }
        />
        <Route
          path="/team"
          element={
            isAuthenticated && (user?.role === 'COLLABORATEUR' || user?.role === 'MANAGER') ? (
              <CollaboratorTeamPage />
            ) : (
              <Navigate to={isAuthenticated ? getDefaultRoute() : '/login'} replace />
            )
          }
        />
        <Route
          path="/evaluations"
          element={
            isAuthenticated && user?.role === 'ADMINISTRATEUR' ? (
              <EvaluationManagementPage />
            ) : (
              <Navigate to={getDefaultRoute()} replace />
            )
          }
        />
        <Route
          path="/evaluations/pending"
          element={
            isAuthenticated && user?.role === 'MANAGER' ? (
              <ManagerEvaluationsPage />
            ) : (
              <Navigate to={getDefaultRoute()} replace />
            )
          }
        />
      </Route>

      <Route path="/verify-email" element={<EmailVerification />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/set-password" element={<ResetPassword />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/" element={<Navigate to={getDefaultRoute()} replace />} />
    </Routes>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="App">
          <AppRoutes />
        </div>
      </AuthProvider>
    </Router>
  );
}

export default App;
