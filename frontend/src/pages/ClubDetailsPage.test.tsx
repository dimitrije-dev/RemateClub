import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, vi } from 'vitest';
import { TestAuthProvider, player } from '../test/authTestUtils';
import { remateApi } from '../services/remateApi';
import { ClubDetailsPage } from './ClubDetailsPage';

const club = {
  id: 'club-1',
  ownerId: 'owner-1',
  name: 'Remate Arena',
  city: 'Beograd',
  status: 'APPROVED' as const,
  createdAt: '2026-08-14T08:00:00Z',
  updatedAt: '2026-08-14T08:00:00Z',
};

const court = {
  id: 'court-1',
  clubId: club.id,
  name: 'Centralni teren',
  type: 'PANORAMIC' as const,
  active: true,
  hourlyPrice: 3000,
  createdAt: '2026-08-14T08:00:00Z',
  updatedAt: '2026-08-14T08:00:00Z',
};

describe('ClubDetailsPage booking validation', () => {
  beforeEach(() => {
    vi.spyOn(remateApi, 'getClub').mockResolvedValue(club);
    vi.spyOn(remateApi, 'getPublicCourts').mockResolvedValue([court]);
    vi.spyOn(remateApi, 'getClubImages').mockResolvedValue([]);
    vi.spyOn(remateApi, 'getAvailability').mockResolvedValue({
      courtId: court.id,
      date: '2026-08-20',
      zoneId: 'Europe/Belgrade',
      slots: [
        { startAt: '2026-08-20T08:00:00Z', endAt: '2026-08-20T09:00:00Z', available: true, price: 3000, currency: 'RSD' },
        { startAt: '2026-08-20T09:00:00Z', endAt: '2026-08-20T10:00:00Z', available: false, price: 3000, currency: 'RSD' },
      ],
    });
  });

  afterEach(() => vi.restoreAllMocks());

  it('prevents owners and unavailable slots from creating a player booking', async () => {
    const createBooking = vi.spyOn(remateApi, 'createBooking');
    const owner = { ...player, id: 'owner-1', email: 'owner@example.com', role: 'OWNER' as const };
    const user = userEvent.setup();
    renderPage(owner);

    const unavailable = await screen.findByRole('button', { name: /Nije dostupno/ });
    expect(unavailable).toBeDisabled();

    await user.click(screen.getByRole('button', { name: /Izaberi/ }));
    await user.click(screen.getByRole('button', { name: 'Potvrdi rezervaciju' }));
    expect(await screen.findByText('Rezervacije mogu da kreiraju samo nalozi igrača.')).toBeInTheDocument();
    expect(createBooking).not.toHaveBeenCalled();
  });

  it('redirects a guest to login before creating a booking', async () => {
    const createBooking = vi.spyOn(remateApi, 'createBooking');
    const user = userEvent.setup();
    renderPage(null);

    await user.click(await screen.findByRole('button', { name: /Izaberi/ }));
    await user.click(screen.getByRole('button', { name: 'Potvrdi rezervaciju' }));

    expect(await screen.findByText('Login destination')).toBeInTheDocument();
    expect(createBooking).not.toHaveBeenCalled();
  });

  it('shows confirmation details and a dedicated success screen for a player', async () => {
    vi.spyOn(remateApi, 'createBooking').mockResolvedValue({
      id: 'booking-1',
      playerId: player.id,
      courtId: court.id,
      clubId: club.id,
      courtName: court.name,
      clubName: club.name,
      clubCity: club.city,
      startAt: '2026-08-20T08:00:00Z',
      endAt: '2026-08-20T09:00:00Z',
      status: 'CONFIRMED',
      totalPrice: 3000,
      currency: 'RSD',
      createdAt: '2026-08-14T08:00:00Z',
      updatedAt: '2026-08-14T08:00:00Z',
    });
    const user = userEvent.setup();
    renderPage(player);

    await user.click(await screen.findByRole('button', { name: /Izaberi/ }));
    await user.click(screen.getByRole('button', { name: 'Potvrdi rezervaciju' }));

    expect(screen.getByRole('dialog', { name: 'Potvrdi rezervaciju' })).toBeInTheDocument();
    expect(screen.getByText(/Remate Arena · Centralni teren/)).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Rezerviši i potvrdi' }));
    expect(await screen.findByRole('heading', { name: 'Vidimo se na terenu!' })).toBeInTheDocument();
  });
});

function renderPage(user: typeof player | null) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/clubs/club-1']}>
        <TestAuthProvider user={user}>
          <Routes>
            <Route path="/clubs/:clubId" element={<ClubDetailsPage />} />
            <Route path="/login" element={<p>Login destination</p>} />
          </Routes>
        </TestAuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}
