import axios from 'axios';
import { API_BASE_URL } from '../config/apiConfig';

// ===================================================================
// CONSOLIDATED API CLIENT — Single axios instance for the whole app
// ===================================================================

// Create axios instance with timeout and proper headers
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 second timeout
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

// ---------- Token management (single source of truth) ----------
let cachedToken: string | null = null;

export function getToken(): string | null {
  if (!cachedToken) {
    cachedToken = localStorage.getItem('token') || sessionStorage.getItem('token');
  }
  return cachedToken;
}

function setToken(token: string): void {
  cachedToken = token;
  localStorage.setItem('token', token);
  sessionStorage.setItem('token', token);
}

function clearToken(): void {
  cachedToken = null;
  localStorage.removeItem('token');
  sessionStorage.removeItem('token');
}

// ---------- Request interceptor (attach auth token) ----------
apiClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ---------- Response interceptor (handle 401 globally) ----------
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearToken();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ===================================================================
// TYPES
// ===================================================================

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  role: string;
  prenom: string;
  nom: string;
  id?: number;
}

export interface User {
  id: number;
  prenom: string;
  nom: string;
  email: string;
  role: string;
  poste?: string;
  departement?: string;
  manager?: {
    id: number;
    nom: string;
    prenom: string;
  };
  dateEmbauche?: string;
  typeContrat?: string;
  adresse?: string;
  telephone?: string;
  cin?: string;
  diplome?: string;
  statut: boolean;
  dateCreation: string;
  dateModification?: string;
}

export interface UserCreationRequest {
  prenom: string;
  nom: string;
  email: string;
  password: string;
  role: string;
  poste?: string;
  departement?: string;
  managerId?: number;
  dateEmbauche?: string;
  typeContrat?: string;
  adresse?: string;
  telephone?: string;
  cin?: string;
  diplome?: string;
}

// ===================================================================
// API SERVICE — Stateless module (not a class) built on apiClient
// ===================================================================

// ---- Authentication ----
export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  const response = await apiClient.post('/auth/login', credentials);
  const { token, email, role, prenom, nom } = response.data;
  setToken(token);
  return { token, email, role, prenom, nom };
}

export function logout(): void {
  clearToken();
}

export async function forgotPassword(email: string): Promise<string> {
  const response = await apiClient.post('/auth/forgot-password', email, {
    headers: { 'Content-Type': 'text/plain' }
  });
  return response.data;
}

export async function verifyEmail(token: string): Promise<{ message: string; success: boolean }> {
  const response = await apiClient.get('/email-verification/verify', {
    params: { token },
  });
  return response.data;
}

export async function resetPassword(token: string, password: string): Promise<string> {
  try {
    console.log('Resetting password with token:', token);
    const response = await apiClient.post('/auth/reset-password', {
      token,
      password
    });
    console.log('Password reset response:', response);
    return response.data;
  } catch (error: any) {
    console.error('Password reset API error:', error);
    console.error('Error response:', error.response?.data);
    throw error;
  }
}

// ---- User management (US 1.1 & US 1.2) ----
export async function getAllUsers(): Promise<User[]> {
  try {
    const response = await apiClient.get('/users', {
      maxContentLength: 1000000,
      maxBodyLength: 1000000
    });
    return response.data;
  } catch (error: any) {
    console.error('API Error in getAllUsers:', error);
    if (error.code === 'ECONNABORTED') {
      throw new Error('Request timeout - please try again');
    }
    throw error;
  }
}

export async function createUser(userData: UserCreationRequest): Promise<string> {
  const payload: any = { ...userData };

  Object.keys(payload).forEach((key) => {
    if (payload[key] === '') {
      delete payload[key];
    }
  });

  if (typeof payload.dateEmbauche === 'string') {
    const v = payload.dateEmbauche.trim();
    if (v) {
      payload.dateEmbauche = `${v}T00:00:00`;
    } else {
      delete payload.dateEmbauche;
    }
  }

  const response = await apiClient.post('/users', payload);
  return response.data;
}

export async function updateUserRole(id: number, newRole: string): Promise<string> {
  const response = await apiClient.put(`/users/${id}/role`, null, {
    params: { role: newRole }
  });
  return response.data;
}

export async function updateUserStatus(id: number, newStatus: boolean): Promise<User> {
  const response = await apiClient.put(`/users/${id}/status`, null, {
    params: { status: newStatus }
  });
  return response.data;
}

export async function deleteUser(id: number): Promise<void> {
  await apiClient.delete(`/users/${id}`);
}

// ---- Notifications ----
export const notificationsApi = {
  getNotifications: async () => {
    const response = await apiClient.get('/notifications');
    return response.data;
  },

  markNotificationAsRead: async (id: number) => {
    const response = await apiClient.put(`/notifications/${id}/read`);
    return response.data;
  },

  deleteNotification: async (id: number) => {
    const response = await apiClient.delete(`/notifications/${id}`);
    return response.data;
  },

  markAllAsRead: async () => {
    const response = await apiClient.put('/notifications/read-all');
    return response.data;
  },
};

// ---- Legacy default export (for backward compat with existing imports) ----
const apiService = {
  apiClient,
  login,
  logout,
  forgotPassword,
  verifyEmail,
  resetPassword,
  getAllUsers,
  createUser,
  updateUserRole,
  updateUserStatus,
  deleteUser,
  getToken,
  setToken,
  clearToken,
};

export default apiService;