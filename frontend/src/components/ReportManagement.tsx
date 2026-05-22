import React, { useState, useEffect } from 'react';
import { reportApi, UserReport } from '../services/reportApi';
import './ReportManagement.css';
import PageHeader from './layout/PageHeader';

const ReportManagement: React.FC = () => {
  const [reports, setReports] = useState<UserReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState<'ALL' | 'PENDING' | 'IN_REVIEW' | 'RESOLVED' | 'DISMISSED'>('ALL');
  const [selectedReport, setSelectedReport] = useState<UserReport | null>(null);
  const [resolveStatus, setResolveStatus] = useState<'RESOLVED' | 'DISMISSED'>('RESOLVED');
  const [adminNotes, setAdminNotes] = useState('');
  const [isResolving, setIsResolving] = useState(false);

  // Check user role
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log('ReportManagement: User role:', payload.role);
        console.log('ReportManagement: User email:', payload.sub);
      } catch (e) {
        console.log('ReportManagement: Error parsing token:', e);
      }
    }
  }, []);

  useEffect(() => {
    loadReports();
  }, []);

  const loadReports = async () => {
    try {
      setLoading(true);
      const data = await reportApi.getAllReports();
      console.log('ReportManagement: API returned data:', data);
      console.log('ReportManagement: Data type:', typeof data);
      console.log('ReportManagement: Is array?', Array.isArray(data));
      if (Array.isArray(data) && data.length > 0) {
        console.log('ReportManagement: First report structure:', data[0]);
        console.log('ReportManagement: First report keys:', Object.keys(data[0]));
      }
      setReports(Array.isArray(data) ? data : []);
    } catch (err: any) {
      console.log('ReportManagement: Error loading reports:', err);
      setError('Erreur lors du chargement des signalements: ' + (err.response?.data || err.message));
      setReports([]);
    } finally {
      setLoading(false);
    }
  };

  const handleResolve = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedReport) return;

    setIsResolving(true);
    try {
      await reportApi.resolveReport(selectedReport.id, resolveStatus, adminNotes || undefined);
      setSuccess('Signalement résolu avec succès!');
      setSelectedReport(null);
      setAdminNotes('');
      loadReports();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError('Erreur lors de la résolution du signalement: ' + (err.response?.data || err.message));
    } finally {
      setIsResolving(false);
    }
  };

  const [success, setSuccess] = useState('');

  const filteredReports = reports.filter(report => {
    if (filter === 'ALL') return true;
    return report.status === filter;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return '#ffc107';
      case 'IN_REVIEW': return '#17a2b8';
      case 'RESOLVED': return '#28a745';
      case 'DISMISSED': return '#dc3545';
      default: return '#6c757d';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'PENDING': return 'En attente';
      case 'IN_REVIEW': return 'En cours';
      case 'RESOLVED': return 'Résolu';
      case 'DISMISSED': return 'Rejeté';
      default: return status;
    }
  };

  const getReportTypeLabel = (type: string) => {
    switch (type) {
      case 'INAPPROPRIATE_BEHAVIOR': return 'Comportement inapproprié';
      case 'POLICY_VIOLATION': return 'Violation de politique';
      case 'SECURITY_CONCERN': return 'Préoccupation de sécurité';
      case 'PERFORMANCE_ISSUE': return 'Problème de performance';
      case 'OTHER': return 'Autre';
      default: return type;
    }
  };

  if (loading) {
    return (
      <div className="report-management">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p className="loading-text">Chargement des signalements...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="report-management">
      <PageHeader
        title="Signalements"
        subtitle="Traitez les rapports soumis par les utilisateurs."
        actions={
          <>
            <label className="report-filter-label" htmlFor="report-status-filter">
              Statut
            </label>
            <select
              id="report-status-filter"
              value={filter}
              onChange={(e) => setFilter(e.target.value as any)}
              className="filter-select"
            >
              <option value="ALL">Tous</option>
              <option value="PENDING">En attente</option>
              <option value="IN_REVIEW">En cours</option>
              <option value="RESOLVED">Résolu</option>
              <option value="DISMISSED">Rejeté</option>
            </select>
            <button type="button" onClick={loadReports} className="btn btn-secondary btn-sm">
              Actualiser
            </button>
          </>
        }
      />

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {success && (
        <div className="success-message">
          {success}
        </div>
      )}

      <div className="reports-container">
        {filteredReports.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">📋</div>
            <p className="empty-state-text">Aucun signalement trouvé</p>
          </div>
        ) : (
          filteredReports.map((report) => (
            <div
              key={report.id}
              className="report-card"
              onClick={() => setSelectedReport(report)}
            >
              <div className="report-header-info">
                <div>
                  <h3 className="report-id">Signalement #{report.id}</h3>
                  <p className="report-date">
                    {new Date(report.createdAt).toLocaleString('fr-FR')}
                  </p>
                </div>
                <span className={`status-badge ${report.status.toLowerCase()}`}>
                  {getStatusLabel(report.status)}
                </span>
              </div>

              <div className="report-details">
                <div className="report-detail-group">
                  <p className="report-detail-label">Signalé par:</p>
                  <p className="report-detail-value">{report.reporterName}</p>
                  <p className="report-detail-subtitle">{report.reporterEmail}</p>
                </div>
                <div className="report-detail-group">
                  <p className="report-detail-label">Utilisateur signalé:</p>
                  <p className="report-detail-value">{report.reportedUserName}</p>
                  <p className="report-detail-subtitle">{report.reportedUserEmail}</p>
                </div>
              </div>

              <div className="report-type-section">
                <p className="report-type-label">Type de signalement:</p>
                <p className="report-type-value">
                  {getReportTypeLabel(report.reportType)}
                </p>
              </div>

              <div className="report-reason-section">
                <p className="report-reason-label">Raison:</p>
                <p className="report-reason-value">{report.reason}</p>
              </div>

              {report.adminNotes && (
                <div className="admin-notes">
                  <p className="admin-notes-label">Notes de l'administrateur:</p>
                  <p className="admin-notes-value">{report.adminNotes}</p>
                </div>
              )}

              {report.resolvedByName && (
                <div className="resolved-info">
                  Résolu par: {report.resolvedByName} - {report.resolvedAt ? new Date(report.resolvedAt).toLocaleString('fr-FR') : ''}
                </div>
              )}
            </div>
          ))
        )}
      </div>

      {selectedReport && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h2 className="modal-title">Résoudre le Signalement #{selectedReport.id}</h2>
              <button
                onClick={() => setSelectedReport(null)}
                className="modal-close"
              >
                ×
              </button>
            </div>

            <form onSubmit={handleResolve} className="modal-form">
              <div className="form-group">
                <label className="form-label">
                  Statut:
                </label>
                <select
                  value={resolveStatus}
                  onChange={(e) => setResolveStatus(e.target.value as 'RESOLVED' | 'DISMISSED')}
                  className="form-select"
                  required
                >
                  <option value="RESOLVED">Résolu</option>
                  <option value="DISMISSED">Rejeté</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">
                  Notes de l'administrateur (optionnel):
                </label>
                <textarea
                  value={adminNotes}
                  onChange={(e) => setAdminNotes(e.target.value)}
                  className="form-textarea"
                  placeholder="Ajouter des notes sur la résolution..."
                />
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  onClick={() => setSelectedReport(null)}
                  className="modal-btn modal-btn-secondary"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  disabled={isResolving}
                  className="modal-btn modal-btn-primary"
                >
                  {isResolving ? 'Résolution en cours...' : 'Résoudre'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReportManagement;
