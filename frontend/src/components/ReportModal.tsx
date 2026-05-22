import React, { useState } from 'react';
import { reportApi, UserReportDTO } from '../services/reportApi';
import '../styles/DarkRedTheme.css';
import './ReportModal.css';

interface ReportModalProps {
  isOpen: boolean;
  onClose: () => void;
  userId: number;
  userName: string;
}

const ReportModal: React.FC<ReportModalProps> = ({ isOpen, onClose, userId, userName }) => {
  const [reportType, setReportType] = useState<'INAPPROPRIATE_BEHAVIOR' | 'POLICY_VIOLATION' | 'SECURITY_CONCERN' | 'PERFORMANCE_ISSUE' | 'OTHER'>('OTHER');
  const [reason, setReason] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reason.trim()) {
      alert('Veuillez fournir une raison pour le signalement');
      return;
    }

    setIsSubmitting(true);
    try {
      const reportData: UserReportDTO = {
        reportedUserId: userId,
        reportType,
        reason: reason.trim()
      };

      const response = await reportApi.createReport(reportData);
      alert('Signalement créé avec succès! ' + response);
      
      // Reset form
      setReason('');
      setReportType('OTHER');
      onClose();
    } catch (error: any) {
      alert('Erreur lors de la création du signalement: ' + (error.response?.data || error.message));
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h2>Signaler un utilisateur</h2>
          <button className="close-btn" onClick={onClose}>&times;</button>
        </div>
        
        <div className="modal-body">
          <p><strong>Utilisateur:</strong> {userName}</p>
          
          <form onSubmit={handleSubmit} className="report-form">
            <div className="form-group">
              <label htmlFor="reportType">Type de signalement *</label>
              <select
                id="reportType"
                value={reportType}
                onChange={(e) => setReportType(e.target.value as any)}
                required
              >
                <option value="INAPPROPRIATE_BEHAVIOR">Comportement inapproprié</option>
                <option value="POLICY_VIOLATION">Violation de politique</option>
                <option value="SECURITY_CONCERN">Préoccupation de sécurité</option>
                <option value="PERFORMANCE_ISSUE">Problème de performance</option>
                <option value="OTHER">Autre</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="reason">Raison du signalement *</label>
              <textarea
                id="reason"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                required
                rows={4}
                placeholder="Décrivez en détail la raison du signalement..."
              />
            </div>

            <div className="form-actions">
              <button type="button" onClick={onClose} className="cancel-btn">
                Annuler
              </button>
              <button type="submit" disabled={isSubmitting} className="submit-btn">
                {isSubmitting ? 'Envoi en cours...' : 'Signaler'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ReportModal;
