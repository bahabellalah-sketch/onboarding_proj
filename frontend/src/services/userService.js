import { apiClient } from './api';

const USER_API_BASE = '/users';

class UserService {
  // Récupérer tous les utilisateurs
  async getAllUsers() {
    try {
      const response = await apiClient.get(`${USER_API_BASE}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // Récupérer les utilisateurs par rôle
  async getUsersByRole(role) {
    try {
      const response = await apiClient.get(`${USER_API_BASE}/role/${role}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // Récupérer les utilisateurs par statut
  async getUsersByStatus(status) {
    try {
      const response = await apiClient.get(`${USER_API_BASE}/status/${status}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // Rechercher des utilisateurs
  async searchUsers(keyword) {
    try {
      const response = await apiClient.get(`${USER_API_BASE}/search?keyword=${keyword}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // Activer un utilisateur
  async activateUser(userId) {
    try {
      const response = await apiClient.put(`${USER_API_BASE}/${userId}/activate`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // Désactiver un utilisateur
  async deactivateUser(userId) {
    try {
      const response = await apiClient.put(`${USER_API_BASE}/${userId}/deactivate`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // Vérifier le statut d'un utilisateur
  async checkUserStatus(email) {
    try {
      const response = await apiClient.get(`${USER_API_BASE}/check-status/${email}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // Supprimer un utilisateur
  async deleteUser(userId) {
    try {
      const response = await apiClient.delete(`${USER_API_BASE}/${userId}`);
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

const userService = new UserService();
export default userService;
