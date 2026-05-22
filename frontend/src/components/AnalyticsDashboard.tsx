import React, { useState, useEffect } from 'react';
import { analyticsApi, GlobalProgressMetrics, OverdueOnboarding, DepartmentStats, RealTimeMetrics, AssignmentAnalytics, AssignmentFilters, FilterOptions } from '../services/analyticsApi';
import Charts from './Charts';
import './AnalyticsDashboard.css';
import PageHeader from './layout/PageHeader';

interface AnalyticsDashboardProps {
  user?: any;
}

const AnalyticsDashboard: React.FC<AnalyticsDashboardProps> = ({ user }) => {
  const [globalMetrics, setGlobalMetrics] = useState<GlobalProgressMetrics | null>(null);
  const [overdueOnboardings, setOverdueOnboardings] = useState<OverdueOnboarding[]>([]);
  const [departmentStats, setDepartmentStats] = useState<Record<string, DepartmentStats>>({});
  const [realTimeMetrics, setRealTimeMetrics] = useState<RealTimeMetrics | null>(null);
  const [filteredAssignments, setFilteredAssignments] = useState<AssignmentAnalytics[]>([]);
  const [filterOptions, setFilterOptions] = useState<FilterOptions | null>(null);
  const [filters, setFilters] = useState<AssignmentFilters>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Load analytics data
  useEffect(() => {
    loadAnalyticsData();
  }, []);

  const loadAnalyticsData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Load all analytics data
      const [summary, options] = await Promise.all([
        analyticsApi.getAnalyticsSummary(),
        analyticsApi.getFilterOptions()
      ]);

      setGlobalMetrics(summary.globalProgress);
      setOverdueOnboardings(summary.overdueOnboardings);
      setDepartmentStats(summary.departmentStats);
      setRealTimeMetrics(summary.realTimeMetrics);
      setFilterOptions(options);
      setFilteredAssignments([]);

    } catch (err: any) {
      console.error('Error loading analytics data:', err);
      setError('Failed to load analytics data');
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = async () => {
    try {
      const assignments = await analyticsApi.getFilteredAssignments(filters);
      setFilteredAssignments(assignments);
    } catch (err: any) {
      console.error('Error applying filters:', err);
      setError('Failed to apply filters');
    }
  };

  const resetFilters = () => {
    setFilters({});
    setFilteredAssignments([]);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('fr-FR');
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'TERMINE': return '#10b981';
      case 'EN_COURS': return '#3b82f6';
      case 'EN_RETARD': return '#ef4444';
      case 'EN_ATTENTE': return '#f59e0b';
      case 'EN_PAUSE': return '#8b5cf6';
      case 'ANNULE': return '#6b7280';
      default: return '#6b7280';
    }
  };

  const getCompletionColor = (percentage: number) => {
    if (percentage >= 80) return '#10b981';
    if (percentage >= 60) return '#3b82f6';
    if (percentage >= 40) return '#f59e0b';
    return '#ef4444';
  };

  // Prepare chart data
  const getProgressChartData = () => {
    if (!globalMetrics) return null;
    
    return {
      labels: ['Terminées', 'En Cours', 'En Retard', 'En Attente'],
      datasets: [{
        label: 'Assignations par statut',
        data: [
          globalMetrics.completedAssignments,
          globalMetrics.inProgressAssignments,
          globalMetrics.overdueAssignments,
          globalMetrics.waitingAssignments
        ],
        backgroundColor: [
          'rgba(16, 185, 129, 0.8)',
          'rgba(59, 130, 246, 0.8)',
          'rgba(239, 68, 68, 0.8)',
          'rgba(139, 92, 246, 0.8)'
        ],
        borderColor: [
          'rgba(16, 185, 129, 1)',
          'rgba(59, 130, 246, 1)',
          'rgba(239, 68, 68, 1)',
          'rgba(139, 92, 246, 1)'
        ],
        borderWidth: 2
      }]
    };
  };

  const getDepartmentChartData = () => {
    if (!departmentStats || Object.keys(departmentStats).length === 0) return null;
    
    const departments = Object.keys(departmentStats);
    const completionRates = departments.map(dept => departmentStats[dept].averageCompletion);
    
    return {
      labels: departments,
      datasets: [{
        label: 'Taux de complétion moyen (%)',
        data: completionRates,
        backgroundColor: 'rgba(59, 130, 246, 0.2)',
        borderColor: 'rgba(59, 130, 246, 1)',
        borderWidth: 2,
        tension: 0.4
      }]
    };
  };

  const getOverdueTrendData = () => {
    if (!overdueOnboardings || overdueOnboardings.length === 0) return null;
    
    // Group overdue by days overdue ranges
    const ranges = {
      '1-7 jours': 0,
      '8-14 jours': 0,
      '15-30 jours': 0,
      '30+ jours': 0
    };
    
    overdueOnboardings.forEach(overdue => {
      if (overdue.daysOverdue <= 7) ranges['1-7 jours']++;
      else if (overdue.daysOverdue <= 14) ranges['8-14 jours']++;
      else if (overdue.daysOverdue <= 30) ranges['15-30 jours']++;
      else ranges['30+ jours']++;
    });
    
    return {
      labels: Object.keys(ranges),
      datasets: [{
        label: 'Nombre d\'assignations en retard',
        data: Object.values(ranges),
        backgroundColor: 'rgba(239, 68, 68, 0.8)',
        borderColor: 'rgba(239, 68, 68, 1)',
        borderWidth: 2
      }]
    };
  };

  if (loading) {
    return (
      <div className="dashboard-container">
        <div className="loading">Loading analytics...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-container">
        <div className="error-message">Error: {error}</div>
        <button onClick={loadAnalyticsData} className="btn btn-primary btn-sm">Retry</button>
      </div>
    );
  }

  return (
    <div className="analytics-dashboard">
      <PageHeader
        title="Analytique"
        subtitle={
          realTimeMetrics
            ? `Dernière mise à jour : ${formatDate(realTimeMetrics.lastUpdated)}`
            : 'Indicateurs de progression des assignations'
        }
      />

      {/* Global Progress Metrics */}
      <div className="metrics-grid">
        <div className="metric-card total">
          <h3>Total des Assignations</h3>
          <div className="metric-value">{globalMetrics?.totalAssignments || 0}</div>
        </div>
        <div className="metric-card completed">
          <h3>Terminées</h3>
          <div className="metric-value">{globalMetrics?.completedAssignments || 0}</div>
          <div className="metric-percentage">
            {globalMetrics ? ((globalMetrics.completedAssignments / globalMetrics.totalAssignments) * 100).toFixed(1) : 0}%
          </div>
        </div>
        <div className="metric-card in-progress">
          <h3>En Cours</h3>
          <div className="metric-value">{globalMetrics?.inProgressAssignments || 0}</div>
        </div>
        <div className="metric-card overdue">
          <h3>En Retard</h3>
          <div className="metric-value">{globalMetrics?.overdueAssignments || 0}</div>
        </div>
        <div className="metric-card waiting">
          <h3>En Attente</h3>
          <div className="metric-value">{globalMetrics?.waitingAssignments || 0}</div>
        </div>
        <div className="metric-card completion">
          <h3>Taux d'Achèvement Moyen</h3>
          <div className="metric-value">{globalMetrics?.averageCompletionPercentage.toFixed(1) || 0}%</div>
        </div>
      </div>

      {/* Charts Section */}
      <div className="chart-grid">
        {getProgressChartData() && (
          <div className="chart-container">
            <Charts 
              type="doughnut" 
              data={getProgressChartData()} 
              title="Répartition des assignations"
              height={350}
            />
          </div>
        )}
        
        {getDepartmentChartData() && (
          <div className="chart-container">
            <Charts 
              type="bar" 
              data={getDepartmentChartData()} 
              title="Taux de complétion par département"
              height={350}
            />
          </div>
        )}
        
        {getOverdueTrendData() && (
          <div className="chart-container">
            <Charts 
              type="bar" 
              data={getOverdueTrendData()} 
              title="Assignations en retard par période"
              height={350}
            />
          </div>
        )}
      </div>

      {/* Real-time Metrics */}
      <div className="realtime-section">
        <h2>Données Temps Réel</h2>
        <div className="realtime-grid">
          <div className="realtime-card">
            <h3>Utilisateurs Actifs Aujourd'hui</h3>
            <div className="realtime-value">{realTimeMetrics?.activeUsersToday || 0}</div>
          </div>
          <div className="realtime-card">
            <h3>Assignations Créées Cette Semaine</h3>
            <div className="realtime-value">{realTimeMetrics?.assignmentsCreatedThisWeek || 0}</div>
          </div>
          <div className="realtime-card">
            <h3>Checklists Terminées Aujourd'hui</h3>
            <div className="realtime-value">{realTimeMetrics?.checklistsCompletedToday || 0}</div>
          </div>
        </div>
      </div>

      {/* Overdue Onboardings */}
      <div className="overdue-section">
        <h2>Identification des Onboardings en Retard</h2>
        {overdueOnboardings.length === 0 ? (
          <div className="no-overdue">Aucun onboarding en retard</div>
        ) : (
          <div className="overdue-table">
            <table>
              <thead>
                <tr>
                  <th>Collaborateur</th>
                  <th>Département</th>
                  <th>Date d'échéance</th>
                  <th>Jours de retard</th>
                  <th>Progression</th>
                </tr>
              </thead>
              <tbody>
                {overdueOnboardings.map((overdue) => (
                  <tr key={overdue.assignmentId}>
                    <td>{overdue.collaboratorName}</td>
                    <td>{overdue.department || 'N/A'}</td>
                    <td>{formatDate(overdue.dueDate)}</td>
                    <td className="overdue-days">{overdue.daysOverdue}</td>
                    <td>
                      <div className="progress-bar">
                        <div 
                          className="progress-fill" 
                          style={{ 
                            width: `${overdue.completionPercentage}%`,
                            backgroundColor: getCompletionColor(overdue.completionPercentage)
                          }}
                        />
                        <span className="progress-text">{overdue.completionPercentage.toFixed(1)}%</span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Department Statistics */}
      <div className="department-section">
        <h2>Statistiques par Département</h2>
        <div className="department-grid">
          {Object.entries(departmentStats).map(([dept, stats]) => (
            <div key={dept} className="department-card">
              <h3>{dept}</h3>
              <div className="dept-stats">
                <div className="dept-stat">
                  <span className="label">Total:</span>
                  <span className="value">{stats.totalAssignments}</span>
                </div>
                <div className="dept-stat">
                  <span className="label">Terminées:</span>
                  <span className="value completed">{stats.completedAssignments}</span>
                </div>
                <div className="dept-stat">
                  <span className="label">En retard:</span>
                  <span className="value overdue">{stats.overdueAssignments}</span>
                </div>
                <div className="dept-stat">
                  <span className="label">Progression moyenne:</span>
                  <span className="value">{stats.averageCompletion.toFixed(1)}%</span>
                </div>
              </div>
              <div className="dept-progress">
                <div 
                  className="dept-progress-fill" 
                  style={{ 
                    width: `${stats.averageCompletion}%`,
                    backgroundColor: getCompletionColor(stats.averageCompletion)
                  }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Filters and Filtered Assignments */}
      <div
        className={
          filteredAssignments.length > 0
            ? 'filters-section filters-section--has-results'
            : 'filters-section'
        }
      >
        <h2>Filtres par Statut, Département, Collaborateur</h2>
        <div className="filters-container">
          <div className="filter-row">
            <div className="filter-group">
              <label>Statut:</label>
              <select 
                multiple 
                value={filters.status || []}
                onChange={(e) => setFilters({
                  ...filters,
                  status: Array.from(e.target.selectedOptions, option => option.value)
                })}
              >
                {filterOptions?.statusOptions.map(status => (
                  <option key={status} value={status}>{status}</option>
                ))}
              </select>
            </div>
            <div className="filter-group">
              <label>Département:</label>
              <select 
                value={filters.department || ''}
                onChange={(e) => setFilters({
                  ...filters,
                  department: e.target.value || undefined
                })}
              >
                <option value="">Tous les départements</option>
                {filterOptions?.departmentOptions.map(dept => (
                  <option key={dept} value={dept}>{dept}</option>
                ))}
              </select>
            </div>
            <div className="filter-group">
              <label>Collaborateur:</label>
              <select 
                value={filters.collaboratorId || ''}
                onChange={(e) => setFilters({
                  ...filters,
                  collaboratorId: e.target.value ? parseInt(e.target.value) : undefined
                })}
              >
                <option value="">Tous les collaborateurs</option>
                {filterOptions?.collaboratorOptions.map(collab => (
                  <option key={collab.id} value={collab.id}>
                    {collab.name} ({collab.department})
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="filter-actions">
            <button onClick={applyFilters} className="btn btn-primary btn-sm">Appliquer les filtres</button>
            <button onClick={resetFilters} className="btn btn-secondary btn-sm">Réinitialiser</button>
          </div>
        </div>

        {/* Filtered Results */}
        {filteredAssignments.length > 0 && (
          <div className="filtered-results">
            <h3>Résultats ({filteredAssignments.length})</h3>
            <ul className="filter-result-cards" role="list">
              {filteredAssignments.map((assignment) => (
                <li key={assignment.assignmentId} className="filter-result-card">
                  <div className="filter-result-card__head">
                    <span className="filter-result-card__name">{assignment.collaboratorName}</span>
                    <span
                      className="filter-result-card__status"
                      style={{ backgroundColor: getStatusColor(assignment.status) }}
                    >
                      {assignment.status}
                    </span>
                  </div>
                  <div className="filter-result-card__grid">
                    <div className="filter-result-card__field">
                      <span className="filter-result-card__label">Département</span>
                      <span className="filter-result-card__value">{assignment.department || 'N/A'}</span>
                    </div>
                    <div className="filter-result-card__field">
                      <span className="filter-result-card__label">Date de début</span>
                      <span className="filter-result-card__value">{formatDate(assignment.startDate)}</span>
                    </div>
                    <div className="filter-result-card__field">
                      <span className="filter-result-card__label">Date d&apos;échéance</span>
                      <span className="filter-result-card__value">{formatDate(assignment.dueDate)}</span>
                    </div>
                    <div className="filter-result-card__field">
                      <span className="filter-result-card__label">Checklists</span>
                      <span className="filter-result-card__value">{assignment.totalChecklists}</span>
                    </div>
                  </div>
                  <div className="filter-result-card__progress">
                    <span className="filter-result-card__progress-label">Progression</span>
                    <div className="filter-result-card__progress-bar">
                      <div
                        className="filter-result-card__progress-fill"
                        style={{
                          width: `${Math.min(100, Math.max(0, assignment.completionPercentage))}%`,
                          backgroundColor: getCompletionColor(assignment.completionPercentage),
                        }}
                      />
                    </div>
                    <span className="filter-result-card__progress-pct">
                      {assignment.completionPercentage.toFixed(1)}%
                    </span>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {/* Refresh Button */}
      <div className="refresh-section">
        <button onClick={loadAnalyticsData} className="btn btn-info btn-sm">
          🔄 Rafraîchir les données
        </button>
      </div>
    </div>
  );
};

export default AnalyticsDashboard;
