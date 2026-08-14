import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';

export type UserRole = 'PLAYER' | 'OWNER' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'DISABLED';

export type AuthUser = {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  status: UserStatus;
};

export type AuthResponse = {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string;
  refreshToken: string;
  refreshTokenExpiresAt: string;
  user: AuthUser;
};

export type LoginPayload = {
  email: string;
  password: string;
};

export type RegisterPayload = LoginPayload & {
  firstName: string;
  lastName: string;
  role: Exclude<UserRole, 'ADMIN'>;
};

type ApiErrorBody = {
  message?: string;
  fields?: Array<{ field: string; message: string }>;
};

type RetryableRequest = InternalAxiosRequestConfig & { _retry?: boolean };

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';
const REFRESH_TOKEN_KEY = 'remate-club.refresh-token';

let accessToken: string | null = null;
let refreshPromise: Promise<string> | null = null;
let authFailureHandler: (() => void) | null = null;

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

const refreshClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

export function setAccessToken(token: string | null) {
  accessToken = token;
}

export function resolveApiAssetUrl(path: string) {
  if (/^https?:\/\//i.test(path)) return path;
  const apiUrl = new URL(API_BASE_URL, window.location.origin);
  return new URL(path, apiUrl.origin).toString();
}

export function getRefreshToken() {
  return window.localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function storeSession(response: AuthResponse) {
  setAccessToken(response.accessToken);
  window.localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
}

export function clearSession() {
  setAccessToken(null);
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function setAuthFailureHandler(handler: (() => void) | null) {
  authFailureHandler = handler;
}

export async function refreshSession(): Promise<AuthResponse> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error('Nema aktivne sesije.');
  }

  const { data } = await refreshClient.post<AuthResponse>('/auth/refresh', { refreshToken });
  storeSession(data);
  return data;
}

export function getApiErrorMessage(error: unknown, fallback = 'Došlo je do greške. Pokušaj ponovo.') {
  if (!axios.isAxiosError<ApiErrorBody>(error)) {
    return error instanceof Error ? error.message : fallback;
  }

  const fieldMessage = error.response?.data.fields?.[0]?.message;
  return fieldMessage ?? error.response?.data.message ?? fallback;
}

api.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiErrorBody>) => {
    const request = error.config as RetryableRequest | undefined;
    const isAuthEndpoint = request?.url?.startsWith('/auth/login')
      || request?.url?.startsWith('/auth/register')
      || request?.url?.startsWith('/auth/refresh');

    if (error.response?.status !== 401 || !request || request._retry || isAuthEndpoint || !getRefreshToken()) {
      return Promise.reject(error);
    }

    request._retry = true;

    try {
      refreshPromise ??= refreshSession()
        .then((response) => response.accessToken)
        .finally(() => {
          refreshPromise = null;
        });
      const renewedAccessToken = await refreshPromise;
      request.headers.Authorization = `Bearer ${renewedAccessToken}`;
      return api(request);
    } catch (refreshError) {
      clearSession();
      authFailureHandler?.();
      return Promise.reject(refreshError);
    }
  },
);
