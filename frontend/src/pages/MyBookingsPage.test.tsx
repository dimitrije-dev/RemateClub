import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, vi } from 'vitest';
import { remateApi, type Booking } from '../services/remateApi';
import { MyBookingsPage } from './MyBookingsPage';

afterEach(() => vi.restoreAllMocks());

describe('MyBookingsPage', () => {
  it('separates upcoming, completed and cancelled bookings', async () => {
    vi.spyOn(remateApi, 'getMyBookings').mockResolvedValue([
      booking('upcoming', '2030-08-20T08:00:00Z', 'CONFIRMED'),
      booking('past', '2020-08-20T08:00:00Z', 'CONFIRMED'),
      booking('cancelled', '2030-08-21T08:00:00Z', 'CANCELLED'),
    ]);
    const user = userEvent.setup();
    renderPage();

    expect(await screen.findByText('#upcoming')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Promeni termin/ })).toBeInTheDocument();
    await user.click(screen.getByRole('tab', { name: /Završene/ }));
    expect(screen.getByText('#past')).toBeInTheDocument();
    await user.click(screen.getByRole('tab', { name: /Otkazane/ }));
    expect(screen.getByText('#cancelle')).toBeInTheDocument();
  });
});

function booking(id: string, startAt: string, status: Booking['status']): Booking {
  return {
    id,
    playerId: 'player-1',
    courtId: 'court-1',
    clubId: 'club-1',
    courtName: 'Centralni teren',
    clubName: 'Remate Arena',
    clubCity: 'Beograd',
    clubAddress: 'Dunavski kej 23',
    startAt,
    endAt: new Date(new Date(startAt).getTime() + 3_600_000).toISOString(),
    status,
    totalPrice: 3000,
    currency: 'RSD',
    createdAt: '2026-08-14T08:00:00Z',
    updatedAt: '2026-08-14T08:00:00Z',
  };
}

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter><MyBookingsPage /></MemoryRouter>
    </QueryClientProvider>,
  );
}
