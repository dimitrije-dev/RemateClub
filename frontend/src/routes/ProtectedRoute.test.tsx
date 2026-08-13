import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { TestAuthProvider, player } from '../test/authTestUtils';
import { ProtectedRoute } from './ProtectedRoute';
import { RoleProtectedRoute } from './RoleProtectedRoute';

describe('auth route guards', () => {
  it('redirects a guest to login', () => {
    render(
      <MemoryRouter initialEntries={['/profile']}>
        <TestAuthProvider>
          <Routes>
            <Route path="/login" element={<p>Login destination</p>} />
            <Route element={<ProtectedRoute />}><Route path="/profile" element={<p>Private profile</p>} /></Route>
          </Routes>
        </TestAuthProvider>
      </MemoryRouter>,
    );

    expect(screen.getByText('Login destination')).toBeInTheDocument();
  });

  it('allows authenticated users through ProtectedRoute', () => {
    render(
      <MemoryRouter initialEntries={['/profile']}>
        <TestAuthProvider user={player}>
          <Routes><Route element={<ProtectedRoute />}><Route path="/profile" element={<p>Private profile</p>} /></Route></Routes>
        </TestAuthProvider>
      </MemoryRouter>,
    );

    expect(screen.getByText('Private profile')).toBeInTheDocument();
  });

  it('redirects a player away from an owner route', () => {
    render(
      <MemoryRouter initialEntries={['/owner']}>
        <TestAuthProvider user={player}>
          <Routes>
            <Route path="/unauthorized" element={<p>Unauthorized destination</p>} />
            <Route element={<RoleProtectedRoute roles={['OWNER']} />}><Route path="/owner" element={<p>Owner area</p>} /></Route>
          </Routes>
        </TestAuthProvider>
      </MemoryRouter>,
    );

    expect(screen.getByText('Unauthorized destination')).toBeInTheDocument();
  });
});
