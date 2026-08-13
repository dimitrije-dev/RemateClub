import type { ReactNode } from 'react';
import { AuthContext } from '../auth/AuthContext';
import type { AuthUser } from '../services/api';

export const player: AuthUser = {
  id: '11111111-1111-1111-1111-111111111111',
  email: 'player@example.com',
  firstName: 'Nikola',
  lastName: 'Jokić',
  role: 'PLAYER',
  status: 'ACTIVE',
};

export function TestAuthProvider({
  children,
  user = null,
  isLoading = false,
}: {
  children: ReactNode;
  user?: AuthUser | null;
  isLoading?: boolean;
}) {
  return (
    <AuthContext.Provider value={{
      user,
      isAuthenticated: user !== null,
      isLoading,
      login: async () => user ?? player,
      register: async () => user ?? player,
      logout: () => undefined,
    }}>
      {children}
    </AuthContext.Provider>
  );
}
