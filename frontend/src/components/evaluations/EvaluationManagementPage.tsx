import React, { useState, useEffect } from 'react';
import { evaluationApi, Evaluation, EvaluationType } from '../../services/evaluationApi';
import PageHeader from '../layout/PageHeader';
import './EvaluationPages.css';

const typeLabel: Record<string, string> = {
  ETAPE: 'Étape',
  PARCOURS_COLLAB: 'Bilan collaborateur',
  PARCOURS_MANAGER: 'Évaluation manager',
};

const EvaluationManagementPage: React.FC = () => {
  const [evaluations, setEvaluations] = useState<Evaluation[]>([]);
  const [filter, setFilter] = useState<string>('ALL');
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const type = filter === 'ALL' ? undefined : filter;
      const data = await evaluationApi.getDashboard(type as EvaluationType | undefined);
      setEvaluations(data);
    } catch {
      setEvaluations([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [filter]);

  const exportCsv = () => {
    const headers = [
      'ID',
      'Type',
      'Parcours',
      'Collaborateur',
      'Évaluateur',
      'Note',
      'Recommandation',
      'Commentaire',
      'Date',
    ];
    const rows = evaluations.map((e) => [
      e.id,
      typeLabel[e.evaluationType || 'ETAPE'] || e.evaluationType,
      e.parcoursNom || '',
      e.collaborateurNom || '',
      e.evaluatorNom || '',
      e.rating,
      e.recommendation || '',
      (e.comment || '').replace(/"/g, '""'),
      e.dateEvaluation,
    ]);
    const csv = [headers, ...rows]
      .map((row) => row.map((cell) => `"${cell}"`).join(','))
      .join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `evaluations_${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="evaluation-management">
      <PageHeader
        title="Évaluations"
        subtitle="Consultez tous les retours sur les parcours et exportez les données."
        actions={
          <button type="button" className="btn btn-secondary btn-sm" onClick={exportCsv} disabled={!evaluations.length}>
            Exporter CSV
          </button>
        }
      />

      <div className="eval-filters">
        <label htmlFor="eval-type-filter">Type :</label>
        <select
          id="eval-type-filter"
          className="form-select form-select-sm"
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
        >
          <option value="ALL">Tous</option>
          <option value="ETAPE">Étape</option>
          <option value="PARCOURS_COLLAB">Bilan collaborateur</option>
          <option value="PARCOURS_MANAGER">Évaluation manager</option>
        </select>
        <button type="button" className="btn btn-secondary btn-sm" onClick={load}>
          Actualiser
        </button>
      </div>

      {loading && <p>Chargement…</p>}

      {!loading && evaluations.length === 0 && (
        <div className="eval-panel"><p>Aucune évaluation enregistrée.</p></div>
      )}

      {!loading && evaluations.length > 0 && (
        <div className="eval-table-wrap">
          <table className="eval-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Type</th>
                <th>Parcours</th>
                <th>Collaborateur</th>
                <th>Évaluateur</th>
                <th>Note</th>
                <th>Recommandation</th>
                <th>Commentaire</th>
              </tr>
            </thead>
            <tbody>
              {evaluations.map((e) => (
                <tr key={e.id}>
                  <td>{new Date(e.dateEvaluation).toLocaleString('fr-FR')}</td>
                  <td>
                    <span
                      className={`eval-badge eval-badge--${
                        e.evaluationType === 'PARCOURS_MANAGER'
                          ? 'manager'
                          : e.evaluationType === 'PARCOURS_COLLAB'
                            ? 'collab'
                            : 'step'
                      }`}
                    >
                      {typeLabel[e.evaluationType || 'ETAPE'] || e.evaluationType}
                    </span>
                  </td>
                  <td>{e.parcoursNom || e.checklistTitre || '—'}</td>
                  <td>{e.collaborateurNom || '—'}</td>
                  <td>{e.evaluatorNom}</td>
                  <td>{e.rating}/5</td>
                  <td>{e.recommendation || '—'}</td>
                  <td>{e.comment ? (e.comment.length > 60 ? `${e.comment.slice(0, 60)}…` : e.comment) : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default EvaluationManagementPage;
