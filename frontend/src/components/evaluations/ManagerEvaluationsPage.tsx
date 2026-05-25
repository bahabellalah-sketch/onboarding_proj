import React, { useState, useEffect } from 'react';
import {
  evaluationApi,
  PendingManagerEvaluation,
} from '../../services/evaluationApi';
import PageHeader from '../layout/PageHeader';
import AssignmentEvaluationPanel from './AssignmentEvaluationPanel';
import './EvaluationPages.css';

const ManagerEvaluationsPage: React.FC = () => {
  const [pending, setPending] = useState<PendingManagerEvaluation[]>([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const loadPending = async () => {
    setLoading(true);
    try {
      const data = await evaluationApi.getPendingForManager();
      setPending(data);
    } catch {
      setPending([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPending();
  }, []);

  return (
    <div className="evaluation-management">
      <PageHeader
        title="Évaluations à réaliser"
        subtitle="Évaluez vos collaborateurs après la fin de leur parcours d'onboarding."
      />

      {loading && <p>Chargement…</p>}

      {!loading && pending.length === 0 && (
        <div className="eval-panel">
          <p>Aucune évaluation en attente. Tous les parcours terminés de votre équipe ont été évalués.</p>
        </div>
      )}

      {pending.map((item) => (
        <div key={item.assignmentId} className="eval-pending-card">
          <div className="eval-pending-card__header">
            <div>
              <h3>{item.collaborateurNom}</h3>
              <p className="eval-panel__meta">{item.collaborateurEmail}</p>
              <p>
                <strong>{item.parcoursNom}</strong> — {item.pourcentageAvancement}% — {item.statut}
              </p>
            </div>
            <button
              type="button"
              className="btn btn-primary btn-sm"
              onClick={() =>
                setExpandedId(expandedId === item.assignmentId ? null : item.assignmentId)
              }
            >
              {expandedId === item.assignmentId ? 'Fermer' : 'Évaluer'}
            </button>
          </div>
          {expandedId === item.assignmentId && (
            <AssignmentEvaluationPanel
              assignmentId={item.assignmentId}
              parcoursNom={item.parcoursNom}
              pourcentageAvancement={item.pourcentageAvancement}
              statut={item.statut}
              onCompleted={() => {
                setExpandedId(null);
                loadPending();
              }}
            />
          )}
        </div>
      ))}
    </div>
  );
};

export default ManagerEvaluationsPage;
