import axios from 'axios';

import { API_BASE_URL } from '../config/apiConfig';

const API_BASE = `${API_BASE_URL}/team-chat`;
const MODIFY_WINDOW_MS = 15 * 60 * 1000;

const authHeaders = () => {
  const token = localStorage.getItem('token');
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
};

export interface TeamMessage {
  id: number;
  teamKey: string;
  senderId: number;
  senderName: string;
  senderRole: string;
  content: string;
  sentAt: string;
  editedAt?: string | null;
  canModify?: boolean;
}

export const canModifyMessage = (msg: TeamMessage, currentUserId?: string | number): boolean => {
  if (currentUserId == null || String(msg.senderId) !== String(currentUserId)) {
    return false;
  }
  if (msg.canModify !== undefined) {
    return msg.canModify;
  }
  const sent = new Date(msg.sentAt).getTime();
  if (Number.isNaN(sent)) {
    return false;
  }
  return Date.now() - sent < MODIFY_WINDOW_MS;
};

export const teamChatApi = {
  getMessages: async (): Promise<TeamMessage[]> => {
    const response = await axios.get(`${API_BASE}/messages`, { headers: authHeaders() });
    return response.data;
  },

  sendMessage: async (content: string): Promise<TeamMessage> => {
    const response = await axios.post(
      `${API_BASE}/messages`,
      { content },
      { headers: authHeaders() }
    );
    return response.data;
  },

  updateMessage: async (id: number, content: string): Promise<TeamMessage> => {
    const response = await axios.put(
      `${API_BASE}/messages/${id}`,
      { content },
      { headers: authHeaders() }
    );
    return response.data;
  },

  deleteMessage: async (id: number): Promise<void> => {
    await axios.delete(`${API_BASE}/messages/${id}`, {
      headers: authHeaders(),
      validateStatus: (status) => status === 204 || (status >= 200 && status < 300),
    });
  },
};
