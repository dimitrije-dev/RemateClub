import { useMutation, useQuery } from '@tanstack/react-query';
import { ArrowLeft, CalendarDays, CheckCircle2, Clock3, MapPin, Sparkles } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { Button } from '../components/ui/Button';
import { getApiErrorMessage, resolveApiAssetUrl } from '../services/api';
import { remateApi, type AvailabilitySlot } from '../services/remateApi';
import { formatMoney, formatTime, localIsoDate } from '../utils/format';

const courtTypeLabels = { STANDARD: 'Standard', PANORAMIC: 'Panoramski', SINGLE: 'Single' } as const;

export function ClubDetailsPage() {
  const { clubId = '' } = useParams();
  const [date, setDate] = useState(localIsoDate(1));
  const [selectedCourtId, setSelectedCourtId] = useState('');
  const [message, setMessage] = useState('');
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const clubQuery = useQuery({ queryKey: ['club', clubId], queryFn: () => remateApi.getClub(clubId), enabled: Boolean(clubId) });
  const courtsQuery = useQuery({ queryKey: ['club-courts', clubId], queryFn: () => remateApi.getPublicCourts(clubId), enabled: Boolean(clubId) });
  const imagesQuery = useQuery({ queryKey: ['club-images', clubId], queryFn: () => remateApi.getClubImages(clubId), enabled: Boolean(clubId) });

  useEffect(() => {
    if (!selectedCourtId && courtsQuery.data?.[0]) setSelectedCourtId(courtsQuery.data[0].id);
  }, [courtsQuery.data, selectedCourtId]);

  const availabilityQuery = useQuery({
    queryKey: ['availability', selectedCourtId, date],
    queryFn: () => remateApi.getAvailability(selectedCourtId, date),
    enabled: Boolean(selectedCourtId && date),
  });
  const bookingMutation = useMutation({
    mutationFn: (slot: AvailabilitySlot) => remateApi.createBooking(selectedCourtId, slot.startAt, slot.endAt),
    onSuccess: (booking) => navigate(`/bookings/${booking.id}`),
    onError: (error) => setMessage(getApiErrorMessage(error, 'Termin više nije slobodan. Osveži dostupnost.')),
  });

  function book(slot: AvailabilitySlot) {
    setMessage('');
    if (!isAuthenticated) {
      navigate('/login', { state: { from: location } });
      return;
    }
    if (user?.role !== 'PLAYER') {
      setMessage('Rezervacije mogu da kreiraju samo nalozi igrača.');
      return;
    }
    bookingMutation.mutate(slot);
  }

  if (clubQuery.isLoading) return <section className="page-shell"><LoadingState label="Učitavamo klub…" /></section>;
  if (clubQuery.isError || !clubQuery.data) return <section className="page-shell"><ErrorState message={getApiErrorMessage(clubQuery.error, 'Klub nije dostupan.')} onRetry={() => void clubQuery.refetch()} /></section>;
  const club = clubQuery.data;
  const coverImage = imagesQuery.data?.find((image) => image.cover) ?? imagesQuery.data?.[0];

  return (
    <section className="page-shell">
      <Link to="/clubs" className="back-link"><ArrowLeft size={17} /> Svi klubovi</Link>
      <div className="relative mt-5 min-h-72 overflow-hidden rounded-[30px] bg-remate-navy p-7 text-white shadow-soft sm:p-10">
        {coverImage && <img src={resolveApiAssetUrl(coverImage.url)} alt={coverImage.altText} className="absolute inset-0 h-full w-full object-cover" />}
        {coverImage && <div className="absolute inset-0 bg-gradient-to-r from-remate-navy via-remate-navy/75 to-remate-navy/20" />}
        <div className="relative flex min-h-52 flex-col justify-end"><p className="eyebrow !text-remate-greenLight">Odobren padel klub</p>
        <div className="mt-4 flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
          <div><h1 className="text-4xl font-black sm:text-5xl">{club.name}</h1><p className="mt-3 inline-flex items-center gap-2 text-white/70"><MapPin size={18} />{club.city}</p></div>
          <span className="inline-flex items-center gap-2 rounded-full bg-remate-green px-4 py-2 text-sm font-black text-remate-navy"><CheckCircle2 size={17} /> Proveren klub</span>
        </div>
        </div>
      </div>

      {imagesQuery.data && imagesQuery.data.length > 1 && <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-4">{imagesQuery.data.slice(0, 8).map((image) => <figure key={image.id} className="aspect-[4/3] overflow-hidden rounded-2xl bg-slate-200"><img src={resolveApiAssetUrl(image.url)} alt={image.altText} loading="lazy" className="h-full w-full object-cover transition hover:scale-105" /></figure>)}</div>}

      <div className="mt-8 grid gap-6 lg:grid-cols-[320px_1fr]">
        <aside className="space-y-4">
          <div className="surface-card"><p className="eyebrow">Tereni</p><h2 className="mt-2 text-2xl font-black">Izaberi teren</h2></div>
          {courtsQuery.isLoading && <LoadingState label="Učitavamo terene…" />}
          {courtsQuery.isError && <ErrorState message="Tereni trenutno nisu dostupni." onRetry={() => void courtsQuery.refetch()} />}
          {courtsQuery.data?.map((court) => (
            <button key={court.id} onClick={() => setSelectedCourtId(court.id)} className={`w-full rounded-remate border p-4 text-left transition ${selectedCourtId === court.id ? 'border-remate-green bg-remate-green/10 ring-2 ring-remate-green/20' : 'border-white bg-white hover:border-remate-green/50'}`}>
              <div className="flex items-start justify-between gap-2"><strong>{court.name}</strong><Sparkles size={17} className="text-emerald-600" /></div>
              <p className="mt-1 text-sm text-remate-muted">{courtTypeLabels[court.type]} · {formatMoney(court.hourlyPrice)}/h</p>
            </button>
          ))}
        </aside>

        <div className="surface-card">
          <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
            <div><p className="eyebrow">Slobodni termini</p><h2 className="mt-2 text-2xl font-black">Rezerviši za minut</h2></div>
            <label className="text-sm font-black">Datum<input type="date" min={localIsoDate()} value={date} onChange={(event) => setDate(event.target.value)} className="ml-3 rounded-xl border border-slate-300 bg-white px-3 py-2 outline-none focus:border-remate-green" /></label>
          </div>
          {message && <p className="auth-error mt-5">{message}</p>}
          <div className="mt-6">
            {!selectedCourtId && <EmptyState title="Nema aktivnih terena" message="Klub još nema teren dostupan za online rezervaciju." />}
            {availabilityQuery.isLoading && <LoadingState label="Proveravamo dostupnost…" />}
            {availabilityQuery.isError && <ErrorState message={getApiErrorMessage(availabilityQuery.error, 'Dostupnost trenutno nije moguće proveriti.')} onRetry={() => void availabilityQuery.refetch()} />}
            {availabilityQuery.data && (
              <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                {availabilityQuery.data.slots.map((slot) => (
                  <button key={slot.startAt} disabled={!slot.available || bookingMutation.isPending} onClick={() => book(slot)} className={`slot-button ${slot.available ? 'slot-available' : 'slot-unavailable'}`}>
                    <span className="inline-flex items-center gap-2 font-black"><Clock3 size={17} />{formatTime(slot.startAt)}–{formatTime(slot.endAt)}</span>
                    <small>{slot.available ? `${formatMoney(slot.price)} · Rezerviši` : 'Nije dostupno'}</small>
                  </button>
                ))}
              </div>
            )}
          </div>
          <p className="mt-5 flex items-center gap-2 text-xs text-remate-muted"><CalendarDays size={15} /> Termini se prikazuju u zoni Europe/Belgrade i proveravaju ponovo pri rezervaciji.</p>
        </div>
      </div>
    </section>
  );
}
