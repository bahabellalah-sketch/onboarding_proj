import { apiClient } from './api';

export interface Evaluation {
  id: number;
  checklistId: number;
  userId: number;
  rating: number;
  comment?: string;
  dateEvaluation: string;
}

export interface EvaluationRequest {
  checklistId: number;
  userId: number;
  rating: number;
  comment?: string;
}

export const evaluationApi = {
  // Create a new evaluation
  createEvaluation: async (evaluation: EvaluationRequest): Promise<Evaluation> => {
    const response = await apiClient.post('/evaluations', evaluation);
    return response.data;
  },

  // Get evaluation by user and checklist item
  getEvaluationByUserAndChecklist: async (userId: number, checklistId: number): Promise<Evaluation | null> => {
    try {
      const response = await apiClient.get(`/evaluations/user/${userId}/checklist/${checklistId}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  // Check if user can evaluate a checklist item
  canUserEvaluate: async (checklistId: number, userId: number): Promise<boolean> => {
    const response = await apiClient.get(`/evaluations/checklist/${checklistId}/can-evaluate/${userId}`);
    return response.data.canEvaluate;
  },

  // Get all evaluations for a checklist item
  getEvaluationsByChecklist: async (checklistId: number): Promise<Evaluation[]> => {
    const response = await apiClient.get(`/evaluations/checklist/${checklistId}`);
    return response.data;
  },

  // Get average rating for a checklist item
  getAverageRating: async (checklistId: number): Promise<number> => {
    const response = await apiClient.get(`/evaluations/checklist/${checklistId}/average`);
    return response.data.averageRating;
  }
};
