import apiService from './api';

// Analytics API Types
export interface GlobalProgressMetrics {
  totalAssignments: number;
  completedAssignments: number;
  inProgressAssignments: number;
  overdueAssignments: number;
  waitingAssignments: number;
  averageCompletionPercentage: number;
}

export interface OverdueOnboarding {
  assignmentId: number;
  collaboratorName: string;
  department: string;
  dueDate: string;
  daysOverdue: number;
  completionPercentage: number;
}

export interface AssignmentAnalytics {
  assignmentId: number;
  collaboratorName: string;
  department: string;
  status: string;
  startDate: string;
  dueDate: string;
  completionPercentage: number;
  totalChecklists: number;
}

export interface AssignmentFilters {
  status?: string[];
  department?: string;
  collaboratorId?: number;
  startDate?: string;
  endDate?: string;
}

export interface DepartmentStats {
  department: string;
  totalAssignments: number;
  completedAssignments: number;
  overdueAssignments: number;
  averageCompletion: number;
}

export interface RealTimeMetrics {
  activeUsersToday: number;
  assignmentsCreatedThisWeek: number;
  checklistsCompletedToday: number;
  lastUpdated: string;
}

export interface AnalyticsSummary {
  globalProgress: GlobalProgressMetrics;
  overdueOnboardings: OverdueOnboarding[];
  departmentStats: Record<string, DepartmentStats>;
  realTimeMetrics: RealTimeMetrics;
}

export interface FilterOptions {
  statusOptions: string[];
  departmentOptions: string[];
  collaboratorOptions: CollaboratorOption[];
}

export interface CollaboratorOption {
  id: number;
  name: string;
  department: string;
}

// Analytics API
export const analyticsApi = {
  // Get global progress metrics
  getGlobalProgress: async (): Promise<GlobalProgressMetrics> => {
    const response = await apiService.apiClient.get('/analytics/global-progress');
    return response.data;
  },

  // Get overdue onboardings
  getOverdueOnboardings: async (): Promise<OverdueOnboarding[]> => {
    const response = await apiService.apiClient.get('/analytics/overdue');
    return response.data;
  },

  // Get filtered assignments
  getFilteredAssignments: async (filters: AssignmentFilters): Promise<AssignmentAnalytics[]> => {
    const response = await apiService.apiClient.post('/analytics/assignments', filters);
    return response.data;
  },

  // Get department statistics
  getDepartmentStats: async (): Promise<Record<string, DepartmentStats>> => {
    const response = await apiService.apiClient.get('/analytics/departments');
    return response.data;
  },

  // Get real-time metrics
  getRealTimeMetrics: async (): Promise<RealTimeMetrics> => {
    const response = await apiService.apiClient.get('/analytics/realtime');
    return response.data;
  },

  // Get analytics summary
  getAnalyticsSummary: async (): Promise<AnalyticsSummary> => {
    const response = await apiService.apiClient.get('/analytics/summary');
    return response.data;
  },

  // Get filter options
  getFilterOptions: async (): Promise<FilterOptions> => {
    const response = await apiService.apiClient.get('/analytics/filter-options');
    return response.data;
  }
};
