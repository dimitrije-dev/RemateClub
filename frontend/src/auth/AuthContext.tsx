import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import {
  api,
  clearSession,
  getRefreshToken,
  refreshSession,
  setAuthFailureHandler,
  storeSession,
  type AuthResponse,
  type AuthUser,
  type LoginPayload,
  type RegisterPayload,
} from '../services/api';

type AuthContextValue = {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (payload: LoginPayload) => Promise<AuthUser>;
  register: (payload: RegisterPayload) => Promise<AuthUser>;
  logout: () => void;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const logout = useCallback(() => {
    clearSession();
    setUser(null);
  }, []);

  useEffect(() => {
    setAuthFailureHandler(logout);
    return () => setAuthFailureHandler(null);
  }, [logout]);

  useEffect(() => {
    let active = true;

    async function restoreSession() {
      if (!getRefreshToken()) {
        setIsLoading(false);
        return;
      }

      try {
        const response = await refreshSession();
        if (active) setUser(response.user);
      } catch {
        clearSession();
      } finally {
        if (active) setIsLoading(false);
      }
    }

    void restoreSession();
    return () => {
      active = false;
    };
  }, []);

  const authenticate = useCallback(async (path: '/auth/login' | '/auth/register', payload: LoginPayload | RegisterPayload) => {
    const { data } = await api.post<AuthResponse>(path, payload);
    storeSession(data);
    setUser(data.user);
    return data.user;
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    isAuthenticated: user !== null,
    isLoading,
    login: (payload) => authenticate('/auth/login', payload),
    register: (payload) => authenticate('/auth/register', payload),
    logout,
  }), [authenticate, isLoading, logout, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth mora biti korišćen unutar AuthProvider-a.');
  return context;
}
