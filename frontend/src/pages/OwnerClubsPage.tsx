import { useQuery } from '@tanstack/react-query';
import { Building2, MapPin, Pencil, Plus, RectangleHorizontal } from 'lucide-react';
import { Link } from 'react-router-dom';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { StatusBadge } from '../components/common/StatusBadge';
import { ButtonLink } from '../components/ui/Button';
import { remateApi } from '../services/remateApi';

export function OwnerClubsPage() {
  const query = useQuery({ queryKey: ['owner-clubs'], queryFn: remateApi.getOwnerClubs });
  return (
    <section className="page-shell">
      <div className="page-heading"><div><p className="eyebrow">Owner workspace</p><h1>Moji klubovi</h1><p>Upravljaj klubovima i terenima koje poseduješ.</p></div><ButtonLink to="/owner/clubs/new" icon={<Plus size={18} />}>Novi klub</ButtonLink></div>
      <div className="mt-8">
        {query.isLoading && <LoadingState label="Učitavamo tvoje klubove…" />}
        {query.isError && <ErrorState message="Klubovi trenutno nisu dostupni." onRetry={() => void query.refetch()} />}
        {query.data?.length === 0 && <EmptyState title="Nema klubova" message="Kreiraj prvi klub i pošalji ga na odobrenje." action={<ButtonLink to="/owner/clubs/new">Kreiraj klub</ButtonLink>} />}
        <div className="grid gap-5 lg:grid-cols-2">
          {query.data?.map((club) => (
            <article key={club.id} className="surface-card">
              <div className="flex items-start justify-between gap-3"><div className="grid h-11 w-11 place-items-center rounded-xl bg-remate-greenLight"><Building2 /></div><StatusBadge status={club.status} /></div>
              <h2 className="mt-5 text-2xl font-black">{club.name}</h2><p className="mt-2 inline-flex items-center gap-2 text-remate-muted"><MapPin size={17} />{club.city}</p>
              <div className="mt-6 flex flex-wrap gap-3"><ButtonLink to={`/owner/clubs/${club.id}/edit`} variant="tertiary" icon={<Pencil size={16} />}>Uredi</ButtonLink><ButtonLink to={`/owner/clubs/${club.id}/courts`} variant="secondary" icon={<RectangleHorizontal size={16} />}>Tereni</ButtonLink></div>
              {club.status === 'APPROVED' && <Link to={`/clubs/${club.id}`} className="mt-4 block text-sm font-black text-emerald-700">Pogledaj javnu stranicu →</Link>}
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
