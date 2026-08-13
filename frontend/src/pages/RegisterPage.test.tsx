import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { RegisterPage } from './RegisterPage';
import { TestAuthProvider } from '../test/authTestUtils';

describe('RegisterPage', () => {
  it('validates password confirmation and offers player/owner roles', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <TestAuthProvider><RegisterPage /></TestAuthProvider>
      </MemoryRouter>,
    );

    expect(screen.getByText('Igrač')).toBeInTheDocument();
    expect(screen.getByText('Vlasnik kluba')).toBeInTheDocument();
    await user.type(screen.getByLabelText('Lozinka'), 'strongPassword123');
    await user.type(screen.getByLabelText('Ponovi lozinku'), 'differentPassword');
    await user.click(screen.getByRole('button', { name: 'Kreiraj nalog' }));

    expect(await screen.findByText('Lozinke se ne podudaraju.')).toBeInTheDocument();
  });
});
