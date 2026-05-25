import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import {
  evaluationApi,
  AssignmentEvaluationSummary,
  Evaluation,
} from '../../services/evaluationApi';
import StarRatingInput from './StarRatingInput';
import './EvaluationPages.css';

interface AssignmentEvaluationPanelProps {
  assignmentId: number;
  parcoursNom: string;
  pourcentageAvancement: number;
  statut: string;
  onCompleted?: () => void;
}

const AssignmentEvaluationPanel: React.FC<AssignmentEvaluationPanelProps> = ({
  assignmentId,
  parcoursNom,
  pourcentageAvancement,
  statut,
  onCompleted,
}) => {
  const { user } = useAuth();
  const [summary, setSummary] = useState<AssignmentEvaluationSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [recommendation, setRecommendation] = useState('OUI');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadSummary = useCallback(async () => {
    setLoading(true);
    try {
      const data = await evaluationApi.getAssignmentSummary(assignmentId);
      setSummary(data);
    } catch {
      setSummary(null);
    } finally {
      setLoading(false);
    }
  }, [assignmentId]);

  useEffect(() => {
    loadSummary();
  }, [loadSummary]);

  const isComplete =
    pourcentageAvancement >= 100 || statut === 'TERMINE';

  const submitCollab = async (e: React.FormEvent) => {
    e.preventDefault();
    if (rating < 1) {
      setError('Veuillez choisir une note.');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      await evaluationApi.createAssignmentEvaluation({
        assignmentId,
        evaluationType: 'PARCOURS_COLLAB',
        rating,
        comment: comment.trim() || undefined,
      });
      setSuccess('Bilan enregistré. Merci !');
      setRating(0);
      setComment('');
      loadSummary();
      onCompleted?.();
    } catch (err: any) {
      setError(err.response?.data?.error || err.response?.data?.message || 'Erreur');
    } finally {
      setSubmitting(false);
    }
  };

  const submitManager = async (e: React.FormEvent) => {
    e.preventDefault();
    if (rating < 1) {
      setError('Veuillez choisir une note.');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      await evaluationApi.createAssignmentEvaluation({
        assignmentId,
        evaluationType: 'PARCOURS_MANAGER',
        rating,
        comment: comment.trim() || undefined,
        recommendation,
      });
      setSuccess('Évaluation enregistrée.');
      setRating(0);
      setComment('');
      loadSummary();
      onCompleted?.();
    } catch (err: any) {
      setError(err.response?.data?.error || err.response?.data?.message || 'Erreur');
    } finally {
      setSubmitting(false);
    }
  };

  const renderReadonly = (ev: Evaluation, label: string) => (
    <div className="eval-readonly">
      <strong>{label}</strong>
      <p>Note : {ev.rating}/5</p>
      {ev.recommendation && <p>Recommandation : {ev.recommendation}</p>}
      {ev.comment && <p>{ev.comment}</p>}
      <p className="eval-panel__meta">
        Par {ev.evaluatorNom} — {new Date(ev.dateEvaluation).toLocaleString('fr-FR')}
      </p>
    </div>
  );

  if (loading) {
    return <div className="eval-panel"><p>Chargement des évaluations…</p></div>;
  }

  if (!isComplete) {
    return (
      <div className="eval-panel">
        <p className="eval-panel__meta">
          Le bilan de fin de parcours sera disponible à 100% de progression ou lorsque le parcours est terminé.
        </p>
      </div>
    );
  }

  const showCollabForm =
    user?.role === 'COLLABORATEUR' && summary?.canEvaluateCollab;
  const showManagerForm =
    (user?.role === 'MANAGER' || user?.role === 'ADMINISTRATEUR') && summary?.canEvaluateManager;

  return (
    <div className="eval-panel">
      <h4 className="eval-panel__title">Évaluation du parcours — {parcoursNom}</h4>
      {summary && summary.stepEvaluationCount > 0 && (
        <p className="eval-panel__meta">
          Moyenne des étapes : {summary.averageStepRating.toFixed(1)}/5 ({summary.stepEvaluationCount} avis)
        </p>
      )}

      {success && <div className="alert alert-success">{success}</div>}
      {error && <div className="alert alert-error">{error}</div>}

      {summary?.collabEvaluation &&
        renderReadonly(summary.collabEvaluation, 'Bilan du collaborateur')}
      {summary?.managerEvaluation &&
        renderReadonly(summary.managerEvaluation, 'Évaluation manager')}

      {showCollabForm && (
        <form onSubmit={submitCollab} className="eval-form">
          <h5>Votre bilan de fin de parcours</h5>
          <p className="eval-panel__meta">
            Partagez votre ressenti global sur ce parcours d&apos;onboarding.
          </p>
          <div className="form-group">
            <label>Note globale *</label>
            <StarRatingInput value={rating} onChange={setRating} />
          </div>
          <div className="form-group">
            <label>Commentaire</label>
            <textarea
              className="form-input"
              rows={3}
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="Points forts, difficultés, suggestions…"
            />
          </div>
          <button type="submit" className="btn btn-primary btn-sm" disabled={submitting}>
            {submitting ? 'Envoi…' : 'Envoyer mon bilan'}
          </button>
        </form>
      )}

      {showManagerForm && (
        <form onSubmit={submitManager} className="eval-form">
          <h5>Évaluer le collaborateur</h5>
          <div className="form-group">
            <label>Note globale *</label>
            <StarRatingInput value={rating} onChange={setRating} />
          </div>
          <div className="form-group">
            <label>Recommandation *</label>
            <select
              className="form-select"
              value={recommendation}
              onChange={(e) => setRecommendation(e.target.value)}
            >
              <option value="OUI">Prêt pour autonomie complète</option>
              <option value="AVEC_ACCOMPAGNEMENT">Avec accompagnement</option>
              <option value="NON">Non — besoin de suivi renforcé</option>
            </select>
          </div>
          <div className="form-group">
            <label>Commentaire</label>
            <textarea
              className="form-input"
              rows={3}
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="Intégration, autonomie, points d'attention…"
            />
          </div>
          <button type="submit" className="btn btn-primary btn-sm" disabled={submitting}>
            {submitting ? 'Envoi…' : 'Enregistrer l\'évaluation'}
          </button>
        </form>
      )}
    </div>
  );
};

export default AssignmentEvaluationPanel;
