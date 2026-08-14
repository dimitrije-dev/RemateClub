import { useQuery } from '@tanstack/react-query';
import { CalendarDays, ChevronLeft, ChevronRight, LocateFixed, MapPin, RotateCcw, Search, SlidersHorizontal } from 'lucide-react';
import { useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { ClubCard } from '../components/clubs/ClubCard';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { getApiErrorMessage } from '../services/api';
import { remateApi, type Club, type CourtEnvironment } from '../services/remateApi';
import { localIsoDate } from '../utils/format';

const PAGE_SIZE = 6;
type SortOption = 'earliest' | 'price' | 'rating' | 'distance' | 'name';

export function ClubsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [locationError, setLocationError] = useState('');
  const search = searchParams.get('search') ?? '';
  const city = searchParams.get('city') ?? '';
  const date = searchParams.get('date') ?? localIsoDate();
  const environment = (searchParams.get('environment') ?? '') as CourtEnvironment | '';
  const minPrice = searchParams.get('minPrice') ?? '';
  const maxPrice = searchParams.get('maxPrice') ?? '';
  const availableToday = searchParams.get('availableToday') === '1';
  const sort = (searchParams.get('sort') ?? 'earliest') as SortOption;
  const latitude = numberParam(searchParams.get('lat'));
  const longitude = numberParam(searchParams.get('lng'));
  const requestedPage = Number.parseInt(searchParams.get('page') ?? '1', 10);
  const page = Number.isFinite(requestedPage) && requestedPage > 0 ? requestedPage : 1;
  const query = useQuery({ queryKey: ['clubs', date], queryFn: () => remateApi.getClubs(date) });

  const filtered = useMemo(() => {
    const minimum = numberParam(minPrice);
    const maximum = numberParam(maxPrice);
    const normalizedSearch = search.toLocaleLowerCase('sr');
    const normalizedCity = city.toLocaleLowerCase('sr');
    return (query.data ?? [])
      .filter((club) => {
        const price = club.minimumHourlyPrice;
        return club.name.toLocaleLowerCase('sr').includes(normalizedSearch)
          && club.city.toLocaleLowerCase('sr').includes(normalizedCity)
          && (!environment || club.environments?.includes(environment))
          && (minimum == null || (price != null && price >= minimum))
          && (maximum == null || (price != null && price <= maximum))
          && (!availableToday || Boolean(club.earliestAvailableAt));
      })
      .sort((first, second) => compareClubs(first, second, sort, latitude, longitude));
  }, [availableToday, city, environment, latitude, longitude, maxPrice, minPrice, query.data, search, sort]);

  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage = Math.min(page, pageCount);
  const visible = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  function updateParam(key: string, value: string) {
    const next = new URLSearchParams(searchParams);
    value ? next.set(key, value) : next.delete(key);
    if (key !== 'page') next.delete('page');
    setSearchParams(next);
  }

  function toggleAvailableToday(checked: boolean) {
    const next = new URLSearchParams(searchParams);
    if (checked) {
      next.set('availableToday', '1');
      next.set('date', localIsoDate());
    } else {
      next.delete('availableToday');
    }
    next.delete('page');
    setSearchParams(next);
  }

  function changeDate(value: string) {
    const next = new URLSearchParams(searchParams);
    next.set('date', value);
    if (value !== localIsoDate()) next.delete('availableToday');
    next.delete('page');
    setSearchParams(next);
  }

  function useCurrentLocation() {
    setLocationError('');
    if (!navigator.geolocation) {
      setLocationError('Pregledač ne podržava određivanje lokacije.');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        const next = new URLSearchParams(searchParams);
        next.set('lat', coords.latitude.toFixed(6));
        next.set('lng', coords.longitude.toFixed(6));
        next.set('sort', 'distance');
        next.delete('page');
        setSearchParams(next);
      },
      () => setLocationError('Lokacija nije dostupna. Dozvoli pristup lokaciji i pokušaj ponovo.'),
      { enableHighAccuracy: false, timeout: 8_000 },
    );
  }

  return (
    <section className="page-shell">
      <div className="page-heading">
        <div><p className="eyebrow">Javni katalog</p><h1>Pronađi svoj teren</h1><p>Izaberi datum i pronađi klub koji odgovara lokaciji, budžetu i načinu igre.</p></div>
        <span className="result-pill">{filtered.length} klubova</span>
      </div>

      <div className="mt-8 rounded-remate bg-white p-5 shadow-sm">
        <div className="flex items-center justify-between gap-4"><div className="flex items-center gap-2"><SlidersHorizontal size={19} /><h2 className="font-black">Filteri pretrage</h2></div><Button variant="tertiary" icon={<RotateCcw size={16} />} onClick={() => setSearchParams({ date: localIsoDate() })}>Resetuj</Button></div>
        <div className="mt-5 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <Input label="Naziv kluba" value={search} onChange={(event) => updateParam('search', event.target.value)} placeholder="Pretraži klubove" leadingIcon={<Search size={18} />} />
          <Input label="Grad" value={city} onChange={(event) => updateParam('city', event.target.value)} placeholder="Svi gradovi" leadingIcon={<MapPin size={18} />} />
          <Input label="Datum igre" type="date" min={localIsoDate()} value={date} onChange={(event) => changeDate(event.target.value)} leadingIcon={<CalendarDays size={18} />} />
          <label className="block text-sm font-extrabold">Indoor / outdoor<select value={environment} onChange={(event) => updateParam('environment', event.target.value)} className="form-select"><option value="">Svi tereni</option><option value="INDOOR">Indoor</option><option value="OUTDOOR">Outdoor</option></select></label>
          <Input label="Minimalna cena" type="number" min="0" step="100" value={minPrice} onChange={(event) => updateParam('minPrice', event.target.value)} placeholder="RSD" />
          <Input label="Maksimalna cena" type="number" min="0" step="100" value={maxPrice} onChange={(event) => updateParam('maxPrice', event.target.value)} placeholder="RSD" />
          <label className="block text-sm font-extrabold">Sortiranje<select value={sort} onChange={(event) => updateParam('sort', event.target.value)} className="form-select"><option value="earliest">Najraniji termin</option><option value="price">Najniža cena</option><option value="rating">Najbolja ocena</option><option value="distance">Najbliži klub</option><option value="name">Naziv kluba</option></select></label>
          <div className="flex flex-col justify-end gap-2">
            <label className="flex min-h-12 items-center gap-3 rounded-xl bg-remate-bg px-4 text-sm font-bold"><input type="checkbox" checked={availableToday} onChange={(event) => toggleAvailableToday(event.target.checked)} className="h-5 w-5 accent-emerald-600" /> Slobodno danas</label>
            <button type="button" onClick={useCurrentLocation} className="inline-flex items-center gap-2 text-xs font-black text-emerald-700 hover:text-remate-navy"><LocateFixed size={15} />{latitude == null ? 'Koristi moju lokaciju' : 'Lokacija je uključena'}</button>
          </div>
        </div>
        {locationError && <p className="auth-error mt-4">{locationError}</p>}
        <p className="mt-4 text-xs text-remate-muted">Filteri se čuvaju u adresi stranice, pa rezultat možeš da sačuvaš ili podeliš.</p>
      </div>

      <div className="mt-8">
        {query.isLoading && <LoadingState label="Proveravamo klubove i slobodne termine…" />}
        {query.isError && <ErrorState message={getApiErrorMessage(query.error, 'Klubovi trenutno nisu dostupni.')} onRetry={() => void query.refetch()} />}
        {!query.isLoading && !query.isError && visible.length === 0 && <EmptyState title="Nema odgovarajućih klubova" message="Promeni datum, cenu ili lokaciju i pokušaj ponovo." />}
        {visible.length > 0 && <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">{visible.map((club) => <ClubCard key={club.id} club={club} selectedDate={date} distanceKm={clubDistance(club, latitude, longitude)} />)}</div>}
      </div>
      {pageCount > 1 && (
        <nav className="mt-8 flex items-center justify-center gap-3" aria-label="Stranice klubova">
          <Button variant="tertiary" disabled={safePage <= 1} icon={<ChevronLeft size={17} />} onClick={() => updateParam('page', String(safePage - 1))}>Prethodna</Button>
          <span className="text-sm font-bold text-remate-muted">{safePage} / {pageCount}</span>
          <Button variant="tertiary" disabled={safePage >= pageCount} icon={<ChevronRight size={17} />} onClick={() => updateParam('page', String(safePage + 1))}>Sledeća</Button>
        </nav>
      )}
    </section>
  );
}

function numberParam(value: string | null) {
  if (!value) return null;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

function compareClubs(first: Club, second: Club, sort: SortOption, lat: number | null, lng: number | null) {
  if (sort === 'price') return nullableNumber(first.minimumHourlyPrice) - nullableNumber(second.minimumHourlyPrice);
  if (sort === 'rating') return (second.averageRating ?? 0) - (first.averageRating ?? 0);
  if (sort === 'distance') return compareNullableDistance(clubDistance(first, lat, lng), clubDistance(second, lat, lng));
  if (sort === 'name') return first.name.localeCompare(second.name, 'sr');
  return nullableTime(first.earliestAvailableAt) - nullableTime(second.earliestAvailableAt);
}

function nullableNumber(value: number | null | undefined) {
  return value == null ? Number.POSITIVE_INFINITY : value;
}

function nullableTime(value: string | null | undefined) {
  return value ? new Date(value).getTime() : Number.POSITIVE_INFINITY;
}

function clubDistance(club: Club, latitude: number | null, longitude: number | null) {
  if (latitude == null || longitude == null || club.latitude == null || club.longitude == null) return Number.POSITIVE_INFINITY;
  const earthRadius = 6371;
  const dLat = toRadians(club.latitude - latitude);
  const dLng = toRadians(club.longitude - longitude);
  const firstLat = toRadians(latitude);
  const secondLat = toRadians(club.latitude);
  const value = Math.sin(dLat / 2) ** 2 + Math.cos(firstLat) * Math.cos(secondLat) * Math.sin(dLng / 2) ** 2;
  return earthRadius * 2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value));
}

function compareNullableDistance(first: number, second: number) {
  if (!Number.isFinite(first) && !Number.isFinite(second)) return 0;
  return first - second;
}

function toRadians(value: number) {
  return value * Math.PI / 180;
}
