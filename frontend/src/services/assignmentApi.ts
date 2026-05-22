import axios from 'axios';

import { API_BASE_URL } from '../config/apiConfig';

// Interfaces TypeScript pour les assignations
export interface Assignment {
  id: number;
  userId: number;
  parcoursId: number;
  userName: string;
  userPrenom: string;
  userEmail: string;
  parcoursNom: string;
  dateDebut: string;
  dateFinPrevisionnelle: string;
  dateFinReelle?: string;
  statut: string;
  pourcentageAvancement: number;
  dateCreation: string;
  dateModification?: string;
  assignePar: string;
}

export interface Checklist {
  id: number;
  assignmentId: number;
  titre: string;
  description: string;
  statut: string;
  ordre: number;
  obligatoire: boolean;
  requiertDocument?: boolean;
  dateCreation: string;
  dateRealisation?: string;
  creePar: string;
  unlocked: boolean;
  lockedCompleted: boolean;
  etapeId?: number;
}

export interface AssignmentRequest {
  userId: number;
  parcoursId: number;
  dateDebut: string;
}

export interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: string;
}

export interface Parcours {
  id: number;
  nom: string;
  description?: string;
  categorieCible: string;
  departementCible: string;
  dureeGlobaleEstimee?: number;
  deadlineGlobaleParDefaut?: number;
  statut: string;
}

// Configuration d'axios avec intercepteur pour le JWT
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur pour ajouter le token JWT
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Intercepteur pour gérer les erreurs
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// API Assignment
export const assignmentApi = {
  // Récupérer toutes les assignations (admin/manager uniquement)
  getAllAssignments: async (): Promise<Assignment[]> => {
    try {
      const response = await api.get('/assignments');
      return response.data;
    } catch (error) {
      console.error('Error fetching assignments:', error);
      throw error;
    }
  },

  // Récupérer les assignations de l'utilisateur connecté (collaborateurs)
  getMyAssignments: async (): Promise<Assignment[]> => {
    try {
      const response = await api.get('/assignments/my-assignments');
      return response.data;
    } catch (error) {
      console.error('Error fetching my assignments:', error);
      throw error;
    }
  },

  // Récupérer une assignation par ID
  getAssignmentById: async (id: number): Promise<Assignment> => {
    try {
      const response = await api.get(`/assignments/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching assignment:', error);
      throw error;
    }
  },

  // Récupérer les assignations par utilisateur
  getAssignmentsByUserId: async (userId: number): Promise<Assignment[]> => {
    try {
      const response = await api.get(`/assignments/user/${userId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching user assignments:', error);
      throw error;
    }
  },

  // Récupérer les assignations par parcours
  getAssignmentsByParcoursId: async (parcoursId: number): Promise<Assignment[]> => {
    try {
      const response = await api.get(`/assignments/parcours/${parcoursId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching parcours assignments:', error);
      throw error;
    }
  },

  // Récupérer les assignations en retard
  getAssignmentsEnRetard: async (): Promise<Assignment[]> => {
    try {
      const response = await api.get('/assignments/retard');
      return response.data;
    } catch (error) {
      console.error('Error fetching late assignments:', error);
      throw error;
    }
  },

  // Récupérer les assignations proches de l'échéance
  getAssignmentsProchesEcheance: async (): Promise<Assignment[]> => {
    try {
      const response = await api.get('/assignments/echeance-proche');
      return response.data;
    } catch (error) {
      console.error('Error fetching upcoming assignments:', error);
      throw error;
    }
  },

  // Créer une nouvelle assignation
  createAssignment: async (assignment: AssignmentRequest): Promise<Assignment> => {
    try {
      const response = await api.post('/assignments', assignment);
      return response.data;
    } catch (error) {
      console.error('Error creating assignment:', error);
      throw error;
    }
  },

  // Assigner un parcours à un collaborateur
  assignerParcours: async (request: AssignmentRequest): Promise<Assignment> => {
    try {
      const response = await api.post('/assignments/assigner', request);
      return response.data;
    } catch (error) {
      console.error('Error assigning parcours:', error);
      throw error;
    }
  },

  // Mettre à jour une assignation
  updateAssignment: async (id: number, assignment: Partial<Assignment>): Promise<Assignment> => {
    try {
      const response = await api.put(`/assignments/${id}`, assignment);
      return response.data;
    } catch (error) {
      console.error('Error updating assignment:', error);
      throw error;
    }
  },

  // Mettre à jour le statut d'une assignation
  updateStatutAssignment: async (id: number, statut: string): Promise<Assignment> => {
    try {
      const response = await api.patch(`/assignments/${id}/statut`, { statut }, {
        headers: {
          'Content-Type': 'application/json',
        },
      });
      return response.data;
    } catch (error) {
      console.error('Error updating assignment status:', error);
      throw error;
    }
  },

  // Mettre à jour l'avancement
  updateAvancement: async (id: number, pourcentage: number): Promise<Assignment> => {
    try {
      const response = await api.patch(`/assignments/${id}/avancement`, pourcentage, {
        headers: {
          'Content-Type': 'application/json',
        },
      });
      return response.data;
    } catch (error) {
      console.error('Error updating assignment progress:', error);
      throw error;
    }
  },

  // Récupérer les checklists d'une assignation
  getChecklistsByAssignmentId: async (assignmentId: number): Promise<Checklist[]> => {
    try {
      const response = await api.get(`/assignments/${assignmentId}/checklists`);
      return response.data;
    } catch (error) {
      console.error('Error fetching checklists:', error);
      throw error;
    }
  },

  // Mettre à jour le statut d'une checklist
  updateChecklistStatut: async (checklistId: number, statut: string): Promise<Checklist> => {
    try {
      const response = await api.patch(`/assignments/checklists/${checklistId}/statut`, { statut }, {
        headers: {
          'Content-Type': 'application/json',
        },
      });
      return response.data;
    } catch (error) {
      console.error('Error updating checklist status:', error);
      throw error;
    }
  },

  // Supprimer une assignation
  deleteAssignment: async (id: number): Promise<void> => {
    try {
      await api.delete(`/assignments/${id}`);
    } catch (error) {
      console.error('Error deleting assignment:', error);
      throw error;
    }
  },
};

// API Users (pour la sélection des collaborateurs)
export const usersApi = {
  // Récupérer tous les utilisateurs
  getAllUsers: async (): Promise<User[]> => {
    try {
      const response = await api.get('/users');
      return response.data;
    } catch (error) {
      console.error('Error fetching users:', error);
      throw error;
    }
  },

  // Récupérer les collaborateurs uniquement
  getCollaborateurs: async (): Promise<User[]> => {
    try {
      const response = await api.get('/users');
      return response.data.filter((user: User) => user.role === 'COLLABORATEUR');
    } catch (error) {
      console.error('Error fetching collaborators:', error);
      throw error;
    }
  },
};

// API Parcours (pour la sélection des parcours)
export const parcoursApiForAssignment = {
  // Récupérer tous les parcours actifs
  getAllParcoursActifs: async (): Promise<Parcours[]> => {
    try {
      const response = await api.get('/parcours');
      return response.data.filter((parcours: Parcours) => parcours.statut === 'ACTIF');
    } catch (error) {
      console.error('Error fetching active parcours:', error);
      throw error;
    }
  },
};
