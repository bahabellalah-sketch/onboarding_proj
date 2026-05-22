import axios from 'axios';

import { API_BASE_URL } from '../config/apiConfig';

// Create axios instance for report API
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add authorization interceptor
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface UserReportDTO {
  reportedUserId: number;
  reportType: 'INAPPROPRIATE_BEHAVIOR' | 'POLICY_VIOLATION' | 'SECURITY_CONCERN' | 'PERFORMANCE_ISSUE' | 'OTHER';
  reason: string;
}

export interface UserReport {
  id: number;
  reportedUserId: number;
  reportedUserName: string;
  reportedUserEmail: string;
  reporterId: number;
  reporterName: string;
  reporterEmail: string;
  reportType: string;
  reason: string;
  status: 'PENDING' | 'IN_REVIEW' | 'RESOLVED' | 'DISMISSED';
  createdAt: string;
  resolvedAt?: string;
  resolvedById?: number;
  resolvedByName?: string;
  adminNotes?: string;
}

class ReportApiService {
  async createReport(reportData: UserReportDTO): Promise<string> {
    const response = await apiClient.post('/reports', reportData);
    return response.data;
  }

  async getPendingReports(): Promise<UserReport[]> {
    const response = await apiClient.get('/reports/pending');
    return response.data;
  }

  async getReportsByUser(userId: number): Promise<UserReport[]> {
    const response = await apiClient.get(`/reports/user/${userId}`);
    return response.data;
  }

  async getAllReports(): Promise<UserReport[]> {
    const response = await apiClient.get('/reports');
    return response.data;
  }

  async resolveReport(
    reportId: number,
    status: 'PENDING' | 'IN_REVIEW' | 'RESOLVED' | 'DISMISSED',
    adminNotes?: string
  ): Promise<string> {
    const params = new URLSearchParams();
    params.append('status', status);
    if (adminNotes) {
      params.append('adminNotes', adminNotes);
    }

    const response = await apiClient.put(`/reports/${reportId}/resolve?${params.toString()}`);
    return response.data;
  }
}

export const reportApi = new ReportApiService();
