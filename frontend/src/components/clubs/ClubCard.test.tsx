import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ClubCard } from './ClubCard';

describe('ClubCard', () => {
  it('renders club information and a details link', () => {
    render(
      <MemoryRouter>
        <ClubCard club={{
          id: 'club-123',
          ownerId: 'owner-123',
          name: 'Remate Arena',
          city: 'Beograd',
          status: 'APPROVED',
          createdAt: '2026-08-14T10:00:00Z',
          updatedAt: '2026-08-14T10:00:00Z',
        }} />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { name: 'Remate Arena' })).toBeInTheDocument();
    expect(screen.getByText('Beograd')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Detalji/ })).toHaveAttribute('href', '/clubs/club-123');
  });

  it('renders the uploaded cover image when one is available', () => {
    render(
      <MemoryRouter>
        <ClubCard club={{
          id: 'club-cover',
          ownerId: 'owner-123',
          name: 'Klub sa slikom',
          city: 'Novi Sad',
          status: 'APPROVED',
          createdAt: '2026-08-14T10:00:00Z',
          updatedAt: '2026-08-14T10:00:00Z',
          coverImageUrl: '/api/club-images/image-1/content',
        }} />
      </MemoryRouter>,
    );

    expect(screen.getByRole('img', { name: 'Glavna fotografija kluba Klub sa slikom' }))
      .toHaveAttribute('src', 'http://localhost:8080/api/club-images/image-1/content');
  });
});
