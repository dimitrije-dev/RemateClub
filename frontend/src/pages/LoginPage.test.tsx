import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { LoginPage } from './LoginPage';
import { TestAuthProvider } from '../test/authTestUtils';

describe('LoginPage', () => {
  it('shows Serbian validation errors for an empty form', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <TestAuthProvider><LoginPage /></TestAuthProvider>
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: 'Prijavi se' }));

    expect(await screen.findByText('Unesi email adresu.')).toBeInTheDocument();
    expect(await screen.findByText('Unesi lozinku.')).toBeInTheDocument();
  });
});
