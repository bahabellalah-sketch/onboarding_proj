import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';
import api, { LoginRequest, LoginResponse } from '../services/api';

interface AuthState {
  user: LoginResponse | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
}

interface AuthContextType extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  clearError: () => void;
}

type AuthAction =
  | { type: 'LOGIN_START' }
  | { type: 'LOGIN_SUCCESS'; payload: LoginResponse }
  | { type: 'LOGIN_FAILURE'; payload: string }
  | { type: 'LOGOUT' }
  | { type: 'CLEAR_ERROR' };

const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
  loading: false,
  error: null,
};

const authReducer = (state: AuthState, action: AuthAction): AuthState => {
  switch (action.type) {
    case 'LOGIN_START':
      return { ...state, loading: true, error: null };
    case 'LOGIN_SUCCESS':
      return {
        ...state,
        user: action.payload,
        isAuthenticated: true,
        loading: false,
        error: null,
      };
    case 'LOGIN_FAILURE':
      return {
        ...state,
        user: null,
        isAuthenticated: false,
        loading: false,
        error: action.payload,
      };
    case 'LOGOUT':
      return {
        ...state,
        user: null,
        isAuthenticated: false,
        loading: false,
        error: null,
      };
    case 'CLEAR_ERROR':
      return { ...state, error: null };
    default:
      return state;
  }
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  const login = async (credentials: LoginRequest) => {
    dispatch({ type: 'LOGIN_START' });
    try {
      const user = await api.login(credentials);
      dispatch({ type: 'LOGIN_SUCCESS', payload: user });
    } catch (error: any) {
      const errorMessage = error.response?.data || 'Login failed';
      dispatch({ type: 'LOGIN_FAILURE', payload: errorMessage as string });
    }
  };

  const logout = () => {
    api.logout();
    dispatch({ type: 'LOGOUT' });
  };

  const clearError = () => {
    dispatch({ type: 'CLEAR_ERROR' });
  };

  // Vérifier et restaurer la session au démarrage
  const checkAuthSession = () => {
    const token = api.getToken();
    if (token) {
      try {
        // Décoder le token pour vérifier l'expiration
        const payload = JSON.parse(atob(token.split('.')[1]));
        const currentTime = Date.now() / 1000;
        
        if (payload.exp > currentTime) {
          // Token valide, restaurer la session
          dispatch({ 
            type: 'LOGIN_SUCCESS', 
            payload: {
              token,
              email: payload.sub,
              role: payload.role,
              prenom: payload.prenom || '',
              nom: payload.nom || '',
              id: payload.id || 0
            }
          });
        } else {
          // Token expiré, nettoyer
          api.logout();
          dispatch({ type: 'LOGOUT' });
        }
      } catch (error) {
        // Token invalide, nettoyer
        api.logout();
        dispatch({ type: 'LOGOUT' });
      }
    }
  };

  // Vérifier la session au montage du composant
  React.useEffect(() => {
    checkAuthSession();
  }, []);

  const value: AuthContextType = {
    ...state,
    login,
    logout,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
