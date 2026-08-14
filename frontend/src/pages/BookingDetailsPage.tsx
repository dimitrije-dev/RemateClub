import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { ArrowLeft, Ban, CalendarDays, Clock3, CreditCard, MapPin, ShieldCheck } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { StatusBadge } from '../components/common/StatusBadge';
import { Button, ButtonLink } from '../components/ui/Button';
import { getApiErrorMessage } from '../services/api';
import { remateApi } from '../services/remateApi';
import { formatDateTime, formatMoney } from '../utils/format';

export function BookingDetailsPage() {
  const { bookingId = '' } = useParams();
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['booking', bookingId], queryFn: () => remateApi.getBooking(bookingId), enabled: Boolean(bookingId) });
  const cancelMutation = useMutation({
    mutationFn: () => remateApi.cancelBooking(bookingId),
    onSuccess: (booking) => {
      queryClient.setQueryData(['booking', bookingId], booking);
      void queryClient.invalidateQueries({ queryKey: ['my-bookings'] });
    },
  });

  if (query.isLoading) return <section className="page-shell"><LoadingState label="Učitavamo rezervaciju…" /></section>;
  if (query.isError || !query.data) return <section className="page-shell"><ErrorState message={getApiErrorMessage(query.error, 'Rezervacija nije dostupna.')} onRetry={() => void query.refetch()} /></section>;
  const booking = query.data;

  return (
    <section className="page-shell max-w-4xl">
      <Link to="/bookings" className="back-link"><ArrowLeft size={17} /> Moje rezervacije</Link>
      <div className="mt-5 rounded-[30px] bg-remate-navy p-7 text-white shadow-soft sm:p-10">
        <div className="flex flex-wrap items-center justify-between gap-4"><div><p className="eyebrow !text-remate-greenLight">Rezervacija #{booking.id.slice(0, 8)}</p><h1 className="mt-2 text-4xl font-black">Detalji termina</h1></div><StatusBadge status={booking.status} /></div>
        <div className="mt-8 grid gap-4 sm:grid-cols-2">
          <Info icon={<CalendarDays />} label="Početak" value={formatDateTime(booking.startAt)} />
          <Info icon={<Clock3 />} label="Kraj" value={formatDateTime(booking.endAt)} />
          <Info icon={<CreditCard />} label="Ukupna cena" value={formatMoney(booking.totalPrice, booking.currency)} />
          <Info icon={<MapPin />} label="Teren" value={booking.courtId} />
        </div>
      </div>
      <div className="mt-6 surface-card">
        <div className="flex items-start gap-3"><ShieldCheck className="mt-1 text-emerald-600" /><div><h2 className="text-xl font-black">Rezervacija je evidentirana na serveru</h2><p className="mt-1 text-remate-muted">Cena i dostupnost su provereni u trenutku kreiranja.</p></div></div>
        {cancelMutation.isError && <p className="auth-error mt-5">{getApiErrorMessage(cancelMutation.error, 'Rezervaciju nije moguće otkazati.')}</p>}
        <div className="mt-6 flex flex-wrap gap-3">
          {booking.status === 'CONFIRMED' && <Button variant="danger" icon={<Ban size={18} />} isLoading={cancelMutation.isPending} onClick={() => cancelMutation.mutate()}>Otkaži rezervaciju</Button>}
          <ButtonLink to={`/clubs/${booking.clubId}`} variant="tertiary">Nazad na klub</ButtonLink>
        </div>
      </div>
      {booking.status === 'CANCELLED' && <div className="mt-6"><EmptyState title="Termin je ponovo slobodan" message="Otkazana rezervacija više ne blokira ovaj termin." /></div>}
    </section>
  );
}

function Info({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return <div className="rounded-2xl bg-white/10 p-4"><span className="text-remate-greenLight">{icon}</span><p className="mt-3 text-xs font-black uppercase tracking-wider text-white/55">{label}</p><p className="mt-1 break-all font-bold">{value}</p></div>;
}
