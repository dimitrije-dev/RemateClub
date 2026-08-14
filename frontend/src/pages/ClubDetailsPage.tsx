import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, CalendarDays, CheckCircle2, Clock3, CreditCard, MapPin, ShieldCheck, Sparkles, X } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { Button, ButtonLink } from '../components/ui/Button';
import { getApiErrorMessage, resolveApiAssetUrl } from '../services/api';
import { remateApi, type AvailabilitySlot, type Booking } from '../services/remateApi';
import { formatDateTime, formatMoney, formatTime, localIsoDate } from '../utils/format';

const courtTypeLabels = { STANDARD: 'Standard', PANORAMIC: 'Panoramski', SINGLE: 'Single' } as const;

export function ClubDetailsPage() {
  const { clubId = '' } = useParams();
  const [searchParams] = useSearchParams();
  const initialDate = searchParams.get('date');
  const rescheduleBookingId = searchParams.get('reschedule');
  const [date, setDate] = useState(initialDate && initialDate >= localIsoDate() ? initialDate : localIsoDate(1));
  const [selectedCourtId, setSelectedCourtId] = useState('');
  const [selectedSlot, setSelectedSlot] = useState<AvailabilitySlot | null>(null);
  const [confirmationOpen, setConfirmationOpen] = useState(false);
  const [completedBooking, setCompletedBooking] = useState<Booking | null>(null);
  const [message, setMessage] = useState('');
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const queryClient = useQueryClient();
  const clubQuery = useQuery({ queryKey: ['club', clubId], queryFn: () => remateApi.getClub(clubId), enabled: Boolean(clubId) });
  const courtsQuery = useQuery({ queryKey: ['club-courts', clubId], queryFn: () => remateApi.getPublicCourts(clubId), enabled: Boolean(clubId) });
  const imagesQuery = useQuery({ queryKey: ['club-images', clubId], queryFn: () => remateApi.getClubImages(clubId), enabled: Boolean(clubId) });

  useEffect(() => {
    if (!selectedCourtId && courtsQuery.data?.[0]) setSelectedCourtId(courtsQuery.data[0].id);
  }, [courtsQuery.data, selectedCourtId]);

  useEffect(() => setSelectedSlot(null), [date, selectedCourtId]);

  const availabilityQuery = useQuery({
    queryKey: ['availability', selectedCourtId, date],
    queryFn: () => remateApi.getAvailability(selectedCourtId, date),
    enabled: Boolean(selectedCourtId && date),
  });
  const bookingMutation = useMutation({
    mutationFn: (slot: AvailabilitySlot) => rescheduleBookingId
      ? remateApi.rescheduleBooking(rescheduleBookingId, slot.startAt, slot.endAt)
      : remateApi.createBooking(selectedCourtId, slot.startAt, slot.endAt),
    onSuccess: async (booking) => {
      setCompletedBooking(booking);
      setConfirmationOpen(false);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['my-bookings'] }),
        queryClient.invalidateQueries({ queryKey: ['availability', selectedCourtId, date] }),
      ]);
    },
    onError: (error) => {
      setConfirmationOpen(false);
      setMessage(getApiErrorMessage(error, 'Termin više nije slobodan. Osveži dostupnost.'));
      void availabilityQuery.refetch();
    },
  });

  const selectedCourt = useMemo(
    () => courtsQuery.data?.find((court) => court.id === selectedCourtId),
    [courtsQuery.data, selectedCourtId],
  );

  function prepareConfirmation() {
    setMessage('');
    if (!selectedSlot) return;
    if (!isAuthenticated) {
      navigate('/login', { state: { from: location } });
      return;
    }
    if (user?.role !== 'PLAYER') {
      setMessage('Rezervacije mogu da kreiraju samo nalozi igrača.');
      return;
    }
    setConfirmationOpen(true);
  }

  if (clubQuery.isLoading) return <section className="page-shell"><LoadingState label="Učitavamo klub…" /></section>;
  if (clubQuery.isError || !clubQuery.data) return <section className="page-shell"><ErrorState message={getApiErrorMessage(clubQuery.error, 'Klub nije dostupan.')} onRetry={() => void clubQuery.refetch()} /></section>;
  const club = clubQuery.data;
  const coverImage = imagesQuery.data?.find((image) => image.cover) ?? imagesQuery.data?.[0];

  if (completedBooking) {
    return (
      <section className="page-shell max-w-3xl">
        <div className="overflow-hidden rounded-[30px] bg-white shadow-soft">
          <div className="bg-remate-navy p-8 text-white sm:p-12"><span className="grid h-16 w-16 place-items-center rounded-full bg-remate-green text-remate-navy"><CheckCircle2 size={34} /></span><p className="eyebrow mt-7 !text-remate-greenLight">{rescheduleBookingId ? 'Termin je promenjen' : 'Rezervacija je potvrđena'}</p><h1 className="mt-2 text-4xl font-black">Vidimo se na terenu!</h1><p className="mt-3 text-white/70">Server je ponovo proverio dostupnost i sačuvao novu cenu termina.</p></div>
          <div className="grid gap-4 p-7 sm:grid-cols-2 sm:p-10">
            <SuccessInfo label="Klub" value={club.name} />
            <SuccessInfo label="Teren" value={selectedCourt?.name ?? completedBooking.courtName} />
            <SuccessInfo label="Termin" value={formatDateTime(completedBooking.startAt)} />
            <SuccessInfo label="Ukupna cena" value={formatMoney(completedBooking.totalPrice, completedBooking.currency)} />
            <div className="flex flex-wrap gap-3 sm:col-span-2"><ButtonLink to={`/bookings/${completedBooking.id}`}>Detalji rezervacije</ButtonLink><ButtonLink to="/bookings" variant="tertiary">Moje rezervacije</ButtonLink><Button variant="tertiary" onClick={() => setCompletedBooking(null)}>Rezerviši još jedan termin</Button></div>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="page-shell">
      <Link to={`/clubs?date=${date}`} className="back-link"><ArrowLeft size={17} /> Svi klubovi</Link>
      <div className="relative mt-5 min-h-72 overflow-hidden rounded-[30px] bg-remate-navy p-7 text-white shadow-soft sm:p-10">
        {coverImage && <img src={resolveApiAssetUrl(coverImage.url)} alt={coverImage.altText} className="absolute inset-0 h-full w-full object-cover" />}
        {coverImage && <div className="absolute inset-0 bg-gradient-to-r from-remate-navy via-remate-navy/75 to-remate-navy/20" />}
        <div className="relative flex min-h-52 flex-col justify-end"><p className="eyebrow !text-remate-greenLight">Odobren padel klub</p><div className="mt-4 flex flex-col justify-between gap-5 sm:flex-row sm:items-end"><div><h1 className="text-4xl font-black sm:text-5xl">{club.name}</h1><p className="mt-3 inline-flex items-center gap-2 text-white/70"><MapPin size={18} />{club.address ? `${club.address}, ${club.city}` : club.city}</p></div><span className="inline-flex items-center gap-2 rounded-full bg-remate-green px-4 py-2 text-sm font-black text-remate-navy"><CheckCircle2 size={17} /> Proveren klub</span></div></div>
      </div>

      {imagesQuery.data && imagesQuery.data.length > 1 && <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-4">{imagesQuery.data.slice(0, 8).map((image) => <figure key={image.id} className="aspect-[4/3] overflow-hidden rounded-2xl bg-slate-200"><img src={resolveApiAssetUrl(image.url)} alt={image.altText} loading="lazy" className="h-full w-full object-cover transition hover:scale-105" /></figure>)}</div>}

      {rescheduleBookingId && <div className="mt-6 rounded-remate border border-amber-200 bg-amber-50 p-4 text-sm font-bold text-amber-900">Izaberi novi termin. Stara rezervacija ostaje aktivna sve dok ne potvrdiš promenu.</div>}

      <div className="mt-8 grid gap-6 lg:grid-cols-[300px_1fr]">
        <aside className="space-y-4 lg:sticky lg:top-24 lg:h-fit">
          <div className="surface-card"><p className="eyebrow">Korak 1</p><h2 className="mt-2 text-2xl font-black">Izaberi teren</h2></div>
          {courtsQuery.isLoading && <LoadingState label="Učitavamo terene…" />}
          {courtsQuery.isError && <ErrorState message="Tereni trenutno nisu dostupni." onRetry={() => void courtsQuery.refetch()} />}
          {courtsQuery.data?.map((court) => (
            <button key={court.id} onClick={() => setSelectedCourtId(court.id)} className={`w-full rounded-remate border p-4 text-left transition ${selectedCourtId === court.id ? 'border-remate-green bg-remate-green/10 ring-2 ring-remate-green/20' : 'border-white bg-white hover:border-remate-green/50'}`}>
              <div className="flex items-start justify-between gap-2"><strong>{court.name}</strong><Sparkles size={17} className="text-emerald-600" /></div>
              <p className="mt-1 text-sm text-remate-muted">{courtTypeLabels[court.type]} · {(court.environment ?? 'INDOOR') === 'INDOOR' ? 'Indoor' : 'Outdoor'} · {formatMoney(court.hourlyPrice)}/h</p>
            </button>
          ))}
        </aside>

        <div className="surface-card">
          <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end"><div><p className="eyebrow">Koraci 2 i 3</p><h2 className="mt-2 text-2xl font-black">Datum i termin</h2></div><label className="text-sm font-black">Datum<input type="date" min={localIsoDate()} value={date} onChange={(event) => setDate(event.target.value)} className="ml-3 rounded-xl border border-slate-300 bg-white px-3 py-2 outline-none focus:border-remate-green" /></label></div>
          {message && <p className="auth-error mt-5">{message}</p>}
          <div className="mt-6">
            {!selectedCourtId && <EmptyState title="Nema aktivnih terena" message="Klub još nema teren dostupan za online rezervaciju." />}
            {availabilityQuery.isLoading && <LoadingState label="Proveravamo dostupnost…" />}
            {availabilityQuery.isError && <ErrorState message={getApiErrorMessage(availabilityQuery.error, 'Dostupnost trenutno nije moguće proveriti.')} onRetry={() => void availabilityQuery.refetch()} />}
            {availabilityQuery.data && <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">{availabilityQuery.data.slots.map((slot) => <button key={slot.startAt} disabled={!slot.available || bookingMutation.isPending} onClick={() => setSelectedSlot(slot)} className={`slot-button ${slot.available ? 'slot-available' : 'slot-unavailable'} ${selectedSlot?.startAt === slot.startAt ? '!border-remate-navy ring-2 ring-remate-green' : ''}`}><span className="inline-flex items-center gap-2 font-black"><Clock3 size={17} />{formatTime(slot.startAt)}–{formatTime(slot.endAt)}</span><small>{slot.available ? `${formatMoney(slot.price)} · Izaberi` : 'Nije dostupno'}</small></button>)}</div>}
          </div>
          <p className="mt-5 flex items-center gap-2 text-xs text-remate-muted"><CalendarDays size={15} /> Termini se prikazuju u zoni Europe/Belgrade i proveravaju ponovo pri potvrdi.</p>

          <div className="sticky bottom-3 z-20 mt-6 rounded-2xl border border-remate-green/40 bg-remate-navy p-4 text-white shadow-soft sm:p-5">
            <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center"><div><p className="eyebrow !text-remate-greenLight">Koraci 4 i 5 · Pregled i potvrda</p>{selectedSlot ? <p className="mt-2 font-black">{selectedCourt?.name} · {formatDateTime(selectedSlot.startAt)} · {formatMoney(selectedSlot.price)}</p> : <p className="mt-2 text-sm text-white/70">Izaberi jedan slobodan termin da nastaviš.</p>}</div><Button disabled={!selectedSlot} icon={<ShieldCheck size={18} />} onClick={prepareConfirmation}>{rescheduleBookingId ? 'Potvrdi promenu' : 'Potvrdi rezervaciju'}</Button></div>
          </div>
        </div>
      </div>

      {confirmationOpen && selectedSlot && (
        <div className="fixed inset-0 z-50 grid place-items-center bg-remate-navy/70 p-4" role="dialog" aria-modal="true" aria-labelledby="booking-confirmation-title" onMouseDown={(event) => { if (event.target === event.currentTarget) setConfirmationOpen(false); }}>
          <div className="w-full max-w-lg rounded-[28px] bg-white p-6 shadow-soft sm:p-8">
            <div className="flex items-start justify-between gap-4"><div><p className="eyebrow">Poslednja provera</p><h2 id="booking-confirmation-title" className="mt-2 text-3xl font-black">{rescheduleBookingId ? 'Promeni termin?' : 'Potvrdi rezervaciju'}</h2></div><button type="button" onClick={() => setConfirmationOpen(false)} aria-label="Zatvori potvrdu" className="rounded-full bg-remate-bg p-2"><X /></button></div>
            <div className="mt-6 grid gap-3 sm:grid-cols-2"><ModalInfo icon={<MapPin />} label="Klub i teren" value={`${club.name} · ${selectedCourt?.name ?? ''}`} /><ModalInfo icon={<CalendarDays />} label="Datum i vreme" value={`${formatDateTime(selectedSlot.startAt)}–${formatTime(selectedSlot.endAt)}`} /><ModalInfo icon={<Clock3 />} label="Trajanje" value={`${Math.round((new Date(selectedSlot.endAt).getTime() - new Date(selectedSlot.startAt).getTime()) / 60_000)} minuta`} /><ModalInfo icon={<CreditCard />} label="Ukupna cena" value={formatMoney(selectedSlot.price, selectedSlot.currency)} /></div>
            <div className="mt-5 rounded-xl bg-amber-50 p-4 text-sm text-amber-900"><strong>Pravila otkazivanja:</strong> potvrđena rezervacija može da se otkaže pre početka termina. Otkazani termin ponovo postaje slobodan.</div>
            <div className="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end"><Button variant="tertiary" onClick={() => setConfirmationOpen(false)}>Nazad</Button><Button isLoading={bookingMutation.isPending} onClick={() => bookingMutation.mutate(selectedSlot)}>{rescheduleBookingId ? 'Sačuvaj novi termin' : 'Rezerviši i potvrdi'}</Button></div>
          </div>
        </div>
      )}
    </section>
  );
}

function ModalInfo({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return <div className="rounded-xl bg-remate-bg p-4"><span className="text-emerald-700">{icon}</span><small className="mt-3 block font-bold text-remate-muted">{label}</small><strong className="mt-1 block">{value}</strong></div>;
}

function SuccessInfo({ label, value }: { label: string; value: string }) {
  return <div className="rounded-xl bg-remate-bg p-4"><small className="font-bold text-remate-muted">{label}</small><strong className="mt-1 block">{value}</strong></div>;
}
