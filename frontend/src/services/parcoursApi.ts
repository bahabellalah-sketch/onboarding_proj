import axios from 'axios';

import { API_BASE_URL } from '../config/apiConfig';

export const parcoursApiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

parcoursApiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token');
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface ParcoursRequest {
  nom: string;
  description?: string;
  categorieCible: string;
  departementCible: string;
  dureeGlobaleEstimee?: number;
  deadlineGlobaleParDefaut?: number;
  statut?: 'ACTIF' | 'DESACTIVE';
}

export interface Parcours {
  id: number;
  nom: string;
  description?: string;
  categorieCible: string;
  departementCible: string;
  dureeGlobaleEstimee?: number;
  deadlineGlobaleParDefaut?: number;
  statut: 'ACTIF' | 'DESACTIVE';
  dateCreation: string;
  dateModification?: string;
  etapes?: Etape[];
  /** Présent sur la réponse POST /parcours/ai-generate */
  etapeCount?: number;
}

export interface Etape {
  id: number;
  nom: string;
  description?: string;
  resourceLinks?: string;
  type: 'ADMINISTRATIF' | 'TECHNIQUE' | 'HUMAIN';
  dureeEstimee?: number;
  ordreExecution?: number;
  requiertDocument?: boolean;
  parcoursId: number;
  dateCreation: string;
  dateModification?: string;
}

export interface EtapeRequest {
  nom: string;
  description?: string;
  resourceLinks?: string;
  type: 'ADMINISTRATIF' | 'TECHNIQUE' | 'HUMAIN';
  dureeEstimee?: number;
  ordreExecution?: number;
  requiertDocument?: boolean;
  parcoursId: number;
}

class ParcoursApiService {
  // Parcours endpoints
  async getAllParcours(): Promise<Parcours[]> {
    try {
      const response = await parcoursApiClient.get('/parcours');
      return response.data;
    } catch (error: any) {
      console.error('API Error in getAllParcours:', error);
      throw error;
    }
  }

  async getParcoursById(id: number): Promise<Parcours> {
    try {
      const response = await parcoursApiClient.get(`/parcours/${id}`);
      return response.data;
    } catch (error: any) {
      console.error('API Error in getParcoursById:', error);
      throw error;
    }
  }

  async createParcours(parcours: ParcoursRequest): Promise<Parcours> {
    try {
      const response = await parcoursApiClient.post('/parcours', parcours);
      return response.data;
    } catch (error: any) {
      console.error('API Error in createParcours:', error);
      throw error;
    }
  }

  async updateParcours(id: number, parcours: ParcoursRequest): Promise<Parcours> {
    try {
      const response = await parcoursApiClient.put(`/parcours/${id}`, parcours);
      return response.data;
    } catch (error: any) {
      console.error('API Error in updateParcours:', error);
      throw error;
    }
  }

  async deleteParcours(id: number): Promise<void> {
    try {
      await parcoursApiClient.delete(`/parcours/${id}`);
    } catch (error: any) {
      console.error('API Error in deleteParcours:', error);
      throw error;
    }
  }

  // Etapes endpoints
  async getAllEtapes(): Promise<Etape[]> {
    try {
      const response = await parcoursApiClient.get('/etapes');
      return response.data;
    } catch (error: any) {
      console.error('API Error in getAllEtapes:', error);
      throw error;
    }
  }

  async getEtapesByParcoursId(parcoursId: number): Promise<Etape[]> {
    try {
      const response = await parcoursApiClient.get(`/etapes/parcours/${parcoursId}`);
      return response.data;
    } catch (error: any) {
      console.error('API Error in getEtapesByParcoursId:', error);
      throw error;
    }
  }

  async createEtape(etape: EtapeRequest): Promise<Etape> {
    try {
      const response = await parcoursApiClient.post('/etapes', etape);
      return response.data;
    } catch (error: any) {
      console.error('API Error in createEtape:', error);
      throw error;
    }
  }

  async updateEtape(id: number, etape: EtapeRequest): Promise<Etape> {
    try {
      const response = await parcoursApiClient.put(`/etapes/${id}`, etape);
      return response.data;
    } catch (error: any) {
      console.error('API Error in updateEtape:', error);
      throw error;
    }
  }

  async deleteEtape(id: number): Promise<void> {
    try {
      await parcoursApiClient.delete(`/etapes/${id}`);
    } catch (error: any) {
      console.error('API Error in deleteEtape:', error);
      throw error;
    }
  }

  async generateEtapesWithAI(parcoursId: number): Promise<Etape[]> {
    try {
      const response = await parcoursApiClient.post(`/parcours/${parcoursId}/generate-etapes-ai`);
      return response.data;
    } catch (error: any) {
      console.error('API Error in generateEtapesWithAI:', error);
      throw error;
    }
  }

  async generateFullParcoursWithAI(prompt: string): Promise<Parcours> {
    try {
      const response = await parcoursApiClient.post('/parcours/ai-generate', { prompt });
      return response.data;
    } catch (error: any) {
      console.error('API Error in generateFullParcoursWithAI:', error);
      throw error;
    }
  }
}

const parcoursApiService = new ParcoursApiService();

export default parcoursApiService;
