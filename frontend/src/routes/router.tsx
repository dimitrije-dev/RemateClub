import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from '../layout/AppLayout';
import { LandingPage } from '../pages/LandingPage';
import { ClubsPage } from '../pages/ClubsPage';
import { ClubDetailsPage } from '../pages/ClubDetailsPage';
import { LoginPage } from '../pages/LoginPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { ProfilePage } from '../pages/ProfilePage';
import { RegisterPage } from '../pages/RegisterPage';
import { MyBookingsPage } from '../pages/MyBookingsPage';
import { BookingDetailsPage } from '../pages/BookingDetailsPage';
import { OwnerDashboardPage } from '../pages/OwnerDashboardPage';
import { OwnerClubsPage } from '../pages/OwnerClubsPage';
import { ClubEditorPage } from '../pages/ClubEditorPage';
import { CourtsManagementPage } from '../pages/CourtsManagementPage';
import { ReservationsCalendarPage } from '../pages/ReservationsCalendarPage';
import { PendingClubsPage } from '../pages/PendingClubsPage';
import { UnauthorizedPage } from '../pages/UnauthorizedPage';
import { ProtectedRoute } from './ProtectedRoute';
import { RoleProtectedRoute } from './RoleProtectedRoute';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <LandingPage /> },
      { path: 'clubs', element: <ClubsPage /> },
      { path: 'clubs/:clubId', element: <ClubDetailsPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      { path: 'unauthorized', element: <UnauthorizedPage /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'profile', element: <ProfilePage /> },
          {
            element: <RoleProtectedRoute roles={['PLAYER']} />,
            children: [
              { path: 'bookings', element: <MyBookingsPage /> },
              { path: 'bookings/:bookingId', element: <BookingDetailsPage /> },
            ],
          },
          {
            element: <RoleProtectedRoute roles={['OWNER']} />,
            children: [
              { path: 'owner', element: <OwnerDashboardPage /> },
              { path: 'owner/clubs', element: <OwnerClubsPage /> },
              { path: 'owner/clubs/new', element: <ClubEditorPage /> },
              { path: 'owner/clubs/:clubId/edit', element: <ClubEditorPage /> },
              { path: 'owner/clubs/:clubId/courts', element: <CourtsManagementPage /> },
              { path: 'owner/reservations', element: <ReservationsCalendarPage /> },
            ],
          },
          {
            element: <RoleProtectedRoute roles={['ADMIN']} />,
            children: [
              { path: 'admin', element: <PendingClubsPage /> },
              { path: 'admin/clubs/pending', element: <PendingClubsPage /> },
            ],
          },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
