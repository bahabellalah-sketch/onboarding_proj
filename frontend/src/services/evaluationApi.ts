import { apiClient } from './api';

export type EvaluationType = 'ETAPE' | 'PARCOURS_COLLAB' | 'PARCOURS_MANAGER';

export interface Evaluation {
  id: number;
  evaluationType?: EvaluationType;
  checklistId?: number;
  checklistTitre?: string;
  assignmentId?: number;
  parcoursNom?: string;
  collaborateurId?: number;
  collaborateurNom?: string;
  collaborateurEmail?: string;
  evaluatorId?: number;
  evaluatorNom?: string;
  evaluatorEmail?: string;
  evaluatorRole?: string;
  rating: number;
  comment?: string;
  recommendation?: string;
  dateEvaluation: string;
}

export interface EvaluationRequest {
  checklistId: number;
  userId: number;
  rating: number;
  comment?: string;
}

export interface AssignmentEvaluationRequest {
  assignmentId: number;
  evaluationType: 'PARCOURS_COLLAB' | 'PARCOURS_MANAGER';
  rating: number;
  comment?: string;
  recommendation?: string;
}

export interface AssignmentEvaluationSummary {
  assignmentId: number;
  canEvaluateCollab: boolean;
  canEvaluateManager: boolean;
  collabEvaluation?: Evaluation;
  managerEvaluation?: Evaluation;
  averageStepRating: number;
  stepEvaluationCount: number;
}

export interface PendingManagerEvaluation {
  assignmentId: number;
  collaborateurId: number;
  collaborateurNom: string;
  collaborateurEmail: string;
  parcoursNom: string;
  pourcentageAvancement: number;
  statut: string;
  dateFinReelle?: string;
}

export const evaluationApi = {
  createEvaluation: async (evaluation: EvaluationRequest): Promise<Evaluation> => {
    const response = await apiClient.post('/evaluations', evaluation);
    return response.data;
  },

  createAssignmentEvaluation: async (data: AssignmentEvaluationRequest): Promise<Evaluation> => {
    const response = await apiClient.post('/evaluations/assignment', data);
    return response.data;
  },

  getAssignmentSummary: async (assignmentId: number): Promise<AssignmentEvaluationSummary> => {
    const response = await apiClient.get(`/evaluations/assignments/${assignmentId}/summary`);
    return response.data;
  },

  getPendingForManager: async (): Promise<PendingManagerEvaluation[]> => {
    const response = await apiClient.get('/evaluations/pending/manager');
    return response.data;
  },

  getDashboard: async (type?: string): Promise<Evaluation[]> => {
    const response = await apiClient.get('/evaluations/dashboard', {
      params: type ? { type } : {},
    });
    return response.data;
  },

  getEvaluationByUserAndChecklist: async (
    userId: number,
    checklistId: number
  ): Promise<Evaluation | null> => {
    try {
      const response = await apiClient.get(
        `/evaluations/user/${userId}/checklist/${checklistId}`
      );
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  canUserEvaluate: async (checklistId: number, userId: number): Promise<boolean> => {
    const response = await apiClient.get(
      `/evaluations/checklist/${checklistId}/can-evaluate/${userId}`
    );
    return response.data.canEvaluate;
  },

  getEvaluationsByChecklist: async (checklistId: number): Promise<Evaluation[]> => {
    const response = await apiClient.get(`/evaluations/checklist/${checklistId}`);
    return response.data;
  },

  getAverageRating: async (checklistId: number): Promise<number> => {
    const response = await apiClient.get(`/evaluations/checklist/${checklistId}/average`);
    return response.data.averageRating;
  },
};
