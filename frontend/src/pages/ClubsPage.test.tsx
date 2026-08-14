import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, useLocation } from 'react-router-dom';
import { afterEach, vi } from 'vitest';
import { remateApi } from '../services/remateApi';
import { ClubsPage } from './ClubsPage';

afterEach(() => vi.restoreAllMocks());

describe('ClubsPage advanced filters', () => {
  it('filters by environment and stores the choice in the URL', async () => {
    vi.spyOn(remateApi, 'getClubs').mockResolvedValue([
      club('indoor', 'Indoor centar', 'INDOOR', 2500, '2026-08-20T08:00:00Z'),
      club('outdoor', 'Outdoor arena', 'OUTDOOR', 3200, '2026-08-20T09:00:00Z'),
    ]);
    const user = userEvent.setup();
    renderPage();

    expect(await screen.findByRole('heading', { name: 'Indoor centar' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Outdoor arena' })).toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText('Indoor / outdoor'), 'OUTDOOR');

    expect(screen.queryByRole('heading', { name: 'Indoor centar' })).not.toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Outdoor arena' })).toBeInTheDocument();
    expect(screen.getByTestId('location')).toHaveTextContent('environment=OUTDOOR');
  });
});

function club(id: string, name: string, environment: 'INDOOR' | 'OUTDOOR', price: number, earliestAvailableAt: string) {
  return {
    id,
    ownerId: 'owner-1',
    name,
    city: 'Beograd',
    status: 'APPROVED' as const,
    averageRating: 4.5,
    reviewCount: 10,
    createdAt: '2026-08-14T08:00:00Z',
    updatedAt: '2026-08-14T08:00:00Z',
    minimumHourlyPrice: price,
    environments: [environment],
    earliestAvailableAt,
  };
}

function LocationProbe() {
  const location = useLocation();
  return <span data-testid="location">{location.search}</span>;
}

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/clubs?date=2026-08-20']}>
        <ClubsPage />
        <LocationProbe />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}
