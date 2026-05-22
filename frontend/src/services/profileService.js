import { apiClient } from './api';

const PROFILE_API_BASE = '/api/profiles';

class ProfileService {
  // Créer un profil utilisateur
  async createProfile(userId, email) {
    try {
      const formData = new FormData();
      if (userId) {
        formData.append('userId', userId);
      }
      formData.append('email', email);
      
      const response = await apiClient.post(`${PROFILE_API_BASE}/create`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // Vérifier si un profil existe
  async checkProfileExists(email) {
    try {
      const response = await apiClient.get(`${PROFILE_API_BASE}/check/${email}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  

  // Gestion des erreurs
  handleError(error) {
    if (error.response) {
      // Erreur du serveur
      const message = error.response.data.message || error.response.data.error || 'Erreur serveur';
      return new Error(message);
    } else if (error.request) {
      // Erreur réseau
      return new Error('Erreur de connexion au serveur');
    } else {
      // Erreur autre
      return new Error('Erreur inconnue: ' + error.message);
    }
  }
}

const profileService = new ProfileService();
export default profileService;
