import type { ReactNode } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import type { UserRole } from '../services/api';

export function RoleProtectedRoute({ roles, children }: { roles: UserRole[]; children?: ReactNode }) {
  const { user, isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <div className="grid min-h-[60vh] place-items-center text-remate-muted">Provera dozvola…</div>;
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  if (!roles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children ?? <Outlet />;
}
