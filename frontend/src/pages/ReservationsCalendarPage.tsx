import { useQuery } from '@tanstack/react-query';
import { CalendarDays, Clock3 } from 'lucide-react';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { StatusBadge } from '../components/common/StatusBadge';
import { remateApi } from '../services/remateApi';
import { formatDateTime, formatMoney } from '../utils/format';

export function ReservationsCalendarPage() {
  const query = useQuery({ queryKey: ['owner-bookings'], queryFn: remateApi.getOwnerBookings });
  return (
    <section className="page-shell">
      <div className="page-heading"><div><p className="eyebrow">Owner workspace</p><h1>Kalendar rezervacija</h1><p>Hronološki pregled termina kroz sve tvoje klubove.</p></div><span className="result-pill">{query.data?.length ?? 0} termina</span></div>
      <div className="mt-8 space-y-4">
        {query.isLoading && <LoadingState label="Učitavamo kalendar…" />}
        {query.isError && <ErrorState message="Kalendar trenutno nije dostupan." onRetry={() => void query.refetch()} />}
        {query.data?.length === 0 && <EmptyState title="Kalendar je prazan" message="Nove rezervacije će se pojaviti ovde čim igrači potvrde termine." />}
        {query.data?.map((booking) => (
          <article key={booking.id} className="surface-card grid gap-4 sm:grid-cols-[1fr_auto] sm:items-center">
            <div><div className="flex flex-wrap items-center gap-3"><StatusBadge status={booking.status} /><span className="text-xs font-bold text-remate-muted">Klub {booking.clubId.slice(0, 8)}</span></div><h2 className="mt-3 text-lg font-black">Teren {booking.courtId.slice(0, 8)}</h2><p className="mt-2 flex items-center gap-2 text-sm text-remate-muted"><CalendarDays size={16} />{formatDateTime(booking.startAt)}</p></div>
            <div className="rounded-xl bg-remate-bg px-4 py-3 text-right"><Clock3 className="ml-auto text-emerald-600" size={18} /><strong className="mt-1 block">{formatMoney(booking.totalPrice, booking.currency)}</strong></div>
          </article>
        ))}
      </div>
    </section>
  );
}
