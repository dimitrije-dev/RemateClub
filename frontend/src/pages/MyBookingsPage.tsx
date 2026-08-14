import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { CalendarPlus } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';
import { BookingCard } from '../components/bookings/BookingCard';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { ButtonLink } from '../components/ui/Button';
import { getApiErrorMessage } from '../services/api';
import { remateApi, type Booking } from '../services/remateApi';

type BookingTab = 'upcoming' | 'completed' | 'cancelled';
const tabs: { id: BookingTab; label: string }[] = [
  { id: 'upcoming', label: 'Predstojeće' },
  { id: 'completed', label: 'Završene' },
  { id: 'cancelled', label: 'Otkazane' },
];

export function MyBookingsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const requestedTab = searchParams.get('tab');
  const activeTab: BookingTab = tabs.some((tab) => tab.id === requestedTab) ? requestedTab as BookingTab : 'upcoming';
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['my-bookings'], queryFn: remateApi.getMyBookings });
  const cancelMutation = useMutation({
    mutationFn: (bookingId: string) => remateApi.cancelBooking(bookingId),
    onSuccess: (updated) => {
      queryClient.setQueryData<Booking[]>(['my-bookings'], (current = []) => current.map((booking) => booking.id === updated.id ? updated : booking));
    },
  });
  const groups = groupBookings(query.data ?? []);
  const visible = groups[activeTab];

  function cancel(booking: Booking) {
    if (window.confirm(`Otkaži rezervaciju u klubu ${booking.clubName}? Termin će ponovo postati slobodan.`)) {
      cancelMutation.mutate(booking.id);
    }
  }

  return (
    <section className="page-shell">
      <div className="page-heading"><div><p className="eyebrow">Igrački prostor</p><h1>Moje rezervacije</h1><p>Predstojeći termini, istorija i sve brze akcije na jednom mestu.</p></div><ButtonLink to="/clubs" icon={<CalendarPlus size={18} />}>Nova rezervacija</ButtonLink></div>
      <div className="mt-8 flex overflow-x-auto rounded-2xl bg-white p-1.5 shadow-sm" role="tablist" aria-label="Status rezervacije">
        {tabs.map((tab) => <button key={tab.id} type="button" role="tab" aria-selected={activeTab === tab.id} onClick={() => setSearchParams(tab.id === 'upcoming' ? {} : { tab: tab.id })} className={`min-w-fit flex-1 rounded-xl px-4 py-3 text-sm font-black transition ${activeTab === tab.id ? 'bg-remate-navy text-white shadow-sm' : 'text-remate-muted hover:bg-remate-bg'}`}>{tab.label}<span className={`ml-2 rounded-full px-2 py-0.5 text-xs ${activeTab === tab.id ? 'bg-white/15' : 'bg-remate-bg'}`}>{groups[tab.id].length}</span></button>)}
      </div>
      {cancelMutation.isError && <p className="auth-error mt-5">{getApiErrorMessage(cancelMutation.error, 'Rezervaciju nije moguće otkazati.')}</p>}
      <div className="mt-6 space-y-4">
        {query.isLoading && <LoadingState label="Učitavamo rezervacije…" />}
        {query.isError && <ErrorState message={getApiErrorMessage(query.error, 'Rezervacije trenutno nisu dostupne.')} onRetry={() => void query.refetch()} />}
        {!query.isLoading && !query.isError && visible.length === 0 && <EmptyState title={emptyTitle(activeTab)} message={activeTab === 'upcoming' ? 'Pronađi klub i izaberi svoj sledeći termin.' : 'Ovde će se prikazati rezervacije iz ove grupe.'} action={activeTab === 'upcoming' ? <ButtonLink to="/clubs">Pronađi teren</ButtonLink> : undefined} />}
        {visible.map((booking) => <BookingCard key={booking.id} booking={booking} onCancel={cancel} cancelling={cancelMutation.isPending && cancelMutation.variables === booking.id} />)}
      </div>
    </section>
  );
}

function groupBookings(bookings: Booking[]) {
  const now = Date.now();
  return bookings.reduce<Record<BookingTab, Booking[]>>((groups, booking) => {
    if (booking.status === 'CANCELLED') groups.cancelled.push(booking);
    else if (booking.status === 'COMPLETED' || new Date(booking.endAt).getTime() < now) groups.completed.push(booking);
    else groups.upcoming.push(booking);
    return groups;
  }, { upcoming: [], completed: [], cancelled: [] });
}

function emptyTitle(tab: BookingTab) {
  if (tab === 'upcoming') return 'Nema predstojećih rezervacija';
  if (tab === 'completed') return 'Nema završenih rezervacija';
  return 'Nema otkazanih rezervacija';
}
