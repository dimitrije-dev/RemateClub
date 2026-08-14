import { useQuery } from '@tanstack/react-query';
import { ChevronLeft, ChevronRight, MapPin, Search } from 'lucide-react';
import { useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { ClubCard } from '../components/clubs/ClubCard';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { getApiErrorMessage } from '../services/api';
import { remateApi } from '../services/remateApi';

const PAGE_SIZE = 6;

export function ClubsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const search = searchParams.get('search') ?? '';
  const city = searchParams.get('city') ?? '';
  const requestedPage = Number.parseInt(searchParams.get('page') ?? '1', 10);
  const page = Number.isFinite(requestedPage) && requestedPage > 0 ? requestedPage : 1;
  const query = useQuery({ queryKey: ['clubs'], queryFn: remateApi.getClubs });

  const filtered = useMemo(() => (query.data ?? []).filter((club) => {
    const matchesSearch = club.name.toLocaleLowerCase('sr').includes(search.toLocaleLowerCase('sr'));
    const matchesCity = club.city.toLocaleLowerCase('sr').includes(city.toLocaleLowerCase('sr'));
    return matchesSearch && matchesCity;
  }), [city, query.data, search]);
  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const visible = filtered.slice((Math.min(page, pageCount) - 1) * PAGE_SIZE, Math.min(page, pageCount) * PAGE_SIZE);

  function updateParam(key: string, value: string) {
    const next = new URLSearchParams(searchParams);
    value ? next.set(key, value) : next.delete(key);
    if (key !== 'page') next.delete('page');
    setSearchParams(next);
  }

  return (
    <section className="page-shell">
      <div className="page-heading">
        <div><p className="eyebrow">Javni katalog</p><h1>Pronađi svoj teren</h1><p>Prikazujemo samo proverene i odobrene padel klubove.</p></div>
        <span className="result-pill">{filtered.length} klubova</span>
      </div>
      <div className="mt-8 grid gap-3 rounded-remate bg-white p-4 shadow-sm md:grid-cols-2">
        <Input label="Naziv kluba" value={search} onChange={(event) => updateParam('search', event.target.value)} placeholder="Pretraži klubove" leadingIcon={<Search size={18} />} />
        <Input label="Grad" value={city} onChange={(event) => updateParam('city', event.target.value)} placeholder="Svi gradovi" leadingIcon={<MapPin size={18} />} />
      </div>
      <div className="mt-8">
        {query.isLoading && <LoadingState label="Učitavamo klubove…" />}
        {query.isError && <ErrorState message={getApiErrorMessage(query.error, 'Klubovi trenutno nisu dostupni.')} onRetry={() => void query.refetch()} />}
        {!query.isLoading && !query.isError && visible.length === 0 && <EmptyState title="Nema pronađenih klubova" message="Promeni naziv ili grad i pokušaj ponovo." />}
        {visible.length > 0 && <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">{visible.map((club) => <ClubCard key={club.id} club={club} />)}</div>}
      </div>
      {pageCount > 1 && (
        <nav className="mt-8 flex items-center justify-center gap-3" aria-label="Stranice klubova">
          <Button variant="tertiary" disabled={page <= 1} icon={<ChevronLeft size={17} />} onClick={() => updateParam('page', String(page - 1))}>Prethodna</Button>
          <span className="text-sm font-bold text-remate-muted">{page} / {pageCount}</span>
          <Button variant="tertiary" disabled={page >= pageCount} icon={<ChevronRight size={17} />} onClick={() => updateParam('page', String(page + 1))}>Sledeća</Button>
        </nav>
      )}
    </section>
  );
}
