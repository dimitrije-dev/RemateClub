import { useQuery } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { Building2, CalendarDays, LayoutDashboard, Plus, RectangleHorizontal } from 'lucide-react';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { ButtonLink } from '../components/ui/Button';
import { remateApi } from '../services/remateApi';

export function OwnerDashboardPage() {
  const clubsQuery = useQuery({ queryKey: ['owner-clubs'], queryFn: remateApi.getOwnerClubs });
  const bookingsQuery = useQuery({ queryKey: ['owner-bookings'], queryFn: remateApi.getOwnerBookings });
  if (clubsQuery.isLoading || bookingsQuery.isLoading) return <section className="page-shell"><LoadingState label="Pripremamo vlasnički panel…" /></section>;
  if (clubsQuery.isError || bookingsQuery.isError) return <section className="page-shell"><ErrorState message="Vlasnički podaci trenutno nisu dostupni." onRetry={() => { void clubsQuery.refetch(); void bookingsQuery.refetch(); }} /></section>;

  const clubs = clubsQuery.data ?? [];
  const bookings = bookingsQuery.data ?? [];
  const confirmed = bookings.filter((booking) => booking.status === 'CONFIRMED').length;

  return (
    <section className="page-shell">
      <div className="page-heading"><div><p className="eyebrow">Owner workspace</p><h1>Vlasnički panel</h1><p>Klubovi, tereni i rezervacije — bez preskakanja važnih signala.</p></div><ButtonLink to="/owner/clubs/new" icon={<Plus size={18} />}>Dodaj klub</ButtonLink></div>
      <div className="mt-8 grid gap-4 sm:grid-cols-3">
        <Metric icon={<Building2 />} label="Moji klubovi" value={clubs.length} />
        <Metric icon={<CalendarDays />} label="Aktivne rezervacije" value={confirmed} />
        <Metric icon={<LayoutDashboard />} label="Čekaju odobrenje" value={clubs.filter((club) => club.status === 'PENDING_APPROVAL').length} />
      </div>
      {clubs.length === 0 ? <div className="mt-8"><EmptyState title="Dodaj prvi klub" message="Nakon kreiranja administrator će pregledati podatke pre javne objave." action={<ButtonLink to="/owner/clubs/new">Kreiraj klub</ButtonLink>} /></div> : (
        <div className="mt-8 grid gap-5 lg:grid-cols-2">
          <DashboardLink to="/owner/clubs" icon={<Building2 />} title="Moji klubovi" text="Uredi osnovne podatke i proveri status odobrenja." />
          <DashboardLink to="/owner/reservations" icon={<CalendarDays />} title="Kalendar rezervacija" text="Pregledaj termine kroz sve svoje klubove." />
        </div>
      )}
    </section>
  );
}

function Metric({ icon, label, value }: { icon: ReactNode; label: string; value: number }) {
  return <article className="metric-card"><span>{icon}</span><p>{label}</p><strong>{value}</strong></article>;
}

function DashboardLink({ to, icon, title, text }: { to: string; icon: ReactNode; title: string; text: string }) {
  return <ButtonLink to={to} variant="tertiary" className="!min-h-32 !items-start !justify-start !rounded-remate !bg-white !p-6 !text-left shadow-sm">{icon}<span><strong className="block text-lg">{title}</strong><small className="mt-1 block font-medium text-remate-muted">{text}</small></span><RectangleHorizontal className="ml-auto opacity-30" /></ButtonLink>;
}
