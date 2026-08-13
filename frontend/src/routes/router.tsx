import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from '../layout/AppLayout';
import { LandingPage } from '../pages/LandingPage';
import { LoginPage } from '../pages/LoginPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { ProfilePage } from '../pages/ProfilePage';
import { RegisterPage } from '../pages/RegisterPage';
import { RoleAreaPage } from '../pages/RoleAreaPage';
import { UnauthorizedPage } from '../pages/UnauthorizedPage';
import { ProtectedRoute } from './ProtectedRoute';
import { RoleProtectedRoute } from './RoleProtectedRoute';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <LandingPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      { path: 'unauthorized', element: <UnauthorizedPage /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'profile', element: <ProfilePage /> },
          {
            element: <RoleProtectedRoute roles={['OWNER', 'ADMIN']} />,
            children: [{ path: 'owner', element: <RoleAreaPage title="Vlasnički prostor" /> }],
          },
          {
            element: <RoleProtectedRoute roles={['ADMIN']} />,
            children: [{ path: 'admin', element: <RoleAreaPage title="Administracija" /> }],
          },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
