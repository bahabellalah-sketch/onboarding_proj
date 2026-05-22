import { apiClient, getToken } from './api';

export interface UserProfile {
  id: number;
  prenom: string;
  nom: string;
  email: string;
  role: string;
  telephone?: string;
  adresse?: string;
  poste?: string;
  departement?: string;
  cin?: string;
  diplome?: string;
  dateNaissance?: string;
  profilePhotoUrl?: string;
  emailVerified?: boolean;
  statut?: boolean;
  dateCreation?: string;
  dateEmbauche?: string;
  managerName?: string;
}

function authHeaders() {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function getUserProfile(userId: number): Promise<UserProfile> {
  const response = await apiClient.get(`/profile/${userId}`, {
    headers: authHeaders(),
  });
  return response.data;
}

export async function updateUserProfile(
  userId: number,
  data: Partial<UserProfile>
): Promise<UserProfile> {
  const response = await apiClient.put(`/profile/${userId}`, data, {
    headers: authHeaders(),
  });
  return response.data;
}

export async function changePassword(
  userId: number,
  currentPassword: string,
  newPassword: string
): Promise<void> {
  await apiClient.post(
    `/profile/${userId}/change-password`,
    { currentPassword, newPassword },
    { headers: authHeaders() }
  );
}

export async function uploadProfilePhoto(
  userId: number,
  file: File
): Promise<string> {
  const formData = new FormData();
  formData.append('file', file);
  const response = await apiClient.post(`/profile/${userId}/upload-photo`, formData, {
    headers: {
      ...authHeaders(),
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
}
