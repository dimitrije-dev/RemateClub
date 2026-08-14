import { useQuery } from '@tanstack/react-query';
import { CalendarPlus } from 'lucide-react';
import { BookingCard } from '../components/bookings/BookingCard';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { ButtonLink } from '../components/ui/Button';
import { getApiErrorMessage } from '../services/api';
import { remateApi } from '../services/remateApi';

export function MyBookingsPage() {
  const query = useQuery({ queryKey: ['my-bookings'], queryFn: remateApi.getMyBookings });

  return (
    <section className="page-shell">
      <div className="page-heading"><div><p className="eyebrow">Igrački prostor</p><h1>Moje rezervacije</h1><p>Svi tvoji potvrđeni, završeni i otkazani termini na jednom mestu.</p></div><ButtonLink to="/clubs" icon={<CalendarPlus size={18} />}>Nova rezervacija</ButtonLink></div>
      <div className="mt-8 space-y-4">
        {query.isLoading && <LoadingState label="Učitavamo rezervacije…" />}
        {query.isError && <ErrorState message={getApiErrorMessage(query.error, 'Rezervacije trenutno nisu dostupne.')} onRetry={() => void query.refetch()} />}
        {query.data?.length === 0 && <EmptyState title="Još nemaš rezervacije" message="Pronađi klub i izaberi svoj prvi termin." action={<ButtonLink to="/clubs">Pronađi teren</ButtonLink>} />}
        {query.data?.map((booking) => <BookingCard key={booking.id} booking={booking} />)}
      </div>
    </section>
  );
}
