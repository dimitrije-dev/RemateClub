import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, CircleDollarSign, Pencil, Plus, RectangleHorizontal, Save } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { getApiErrorMessage } from '../services/api';
import { remateApi, type Court, type CourtPayload, type CourtType } from '../services/remateApi';
import { formatMoney } from '../utils/format';

const typeLabels: Record<CourtType, string> = { STANDARD: 'Standard', PANORAMIC: 'Panoramski', SINGLE: 'Single' };

export function CourtsManagementPage() {
  const { clubId = '' } = useParams();
  const [editing, setEditing] = useState<Court | null>(null);
  const [name, setName] = useState('');
  const [type, setType] = useState<CourtType>('STANDARD');
  const [active, setActive] = useState(true);
  const [hourlyPrice, setHourlyPrice] = useState('3000');
  const [error, setError] = useState('');
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['owner-courts', clubId], queryFn: () => remateApi.getOwnerCourts(clubId), enabled: Boolean(clubId) });
  const clubsQuery = useQuery({ queryKey: ['owner-clubs'], queryFn: remateApi.getOwnerClubs });
  const club = clubsQuery.data?.find((item) => item.id === clubId);

  useEffect(() => {
    if (editing) { setName(editing.name); setType(editing.type); setActive(editing.active); setHourlyPrice(String(editing.hourlyPrice)); }
  }, [editing]);

  const mutation = useMutation({
    mutationFn: (payload: CourtPayload) => editing
      ? remateApi.updateCourt(clubId, editing.id, payload)
      : remateApi.createCourt(clubId, payload),
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: ['owner-courts', clubId] }); resetForm(); },
    onError: (mutationError) => setError(getApiErrorMessage(mutationError, 'Teren nije sačuvan.')),
  });

  function resetForm() { setEditing(null); setName(''); setType('STANDARD'); setActive(true); setHourlyPrice('3000'); setError(''); }
  function submit(event: FormEvent) {
    event.preventDefault(); setError('');
    const price = Number(hourlyPrice);
    if (!name.trim() || !Number.isFinite(price) || price <= 0) { setError('Unesi naziv i validnu cenu terena.'); return; }
    mutation.mutate({ name, type, active, hourlyPrice: price });
  }

  return (
    <section className="page-shell">
      <Link to="/owner/clubs" className="back-link"><ArrowLeft size={17} /> Moji klubovi</Link>
      <div className="page-heading mt-5"><div><p className="eyebrow">Upravljanje terenima</p><h1>{club?.name ?? 'Tereni kluba'}</h1><p>Aktivni tereni se odmah prikazuju na javnoj stranici odobrenog kluba.</p></div></div>
      <div className="mt-8 grid gap-6 lg:grid-cols-[1fr_380px]">
        <div className="space-y-4">
          {query.isLoading && <LoadingState label="Učitavamo terene…" />}
          {query.isError && <ErrorState message="Tereni trenutno nisu dostupni." onRetry={() => void query.refetch()} />}
          {query.data?.length === 0 && <EmptyState title="Nema terena" message="Dodaj prvi teren kroz formu pored liste." />}
          {query.data?.map((court) => (
            <article key={court.id} className="surface-card flex items-center justify-between gap-4">
              <div className="flex items-center gap-4"><span className={`grid h-12 w-12 place-items-center rounded-xl ${court.active ? 'bg-remate-greenLight' : 'bg-slate-200 text-slate-500'}`}><RectangleHorizontal /></span><div><h2 className="font-black">{court.name}</h2><p className="mt-1 text-sm text-remate-muted">{typeLabels[court.type]} · {formatMoney(court.hourlyPrice)}/h · {court.active ? 'Aktivan' : 'Neaktivan'}</p></div></div>
              <Button variant="tertiary" icon={<Pencil size={16} />} onClick={() => setEditing(court)}>Uredi</Button>
            </article>
          ))}
        </div>
        <aside className="surface-card h-fit lg:sticky lg:top-24">
          <div className="flex items-center gap-3"><span className="auth-icon !mb-0">{editing ? <Pencil /> : <Plus />}</span><div><p className="eyebrow">{editing ? 'Izmena' : 'Novi teren'}</p><h2 className="text-xl font-black">{editing?.name ?? 'Dodaj teren'}</h2></div></div>
          <form onSubmit={submit} className="mt-6 space-y-4">
            {error && <p className="auth-error">{error}</p>}
            <Input label="Naziv terena" value={name} onChange={(event) => setName(event.target.value)} leadingIcon={<RectangleHorizontal size={18} />} />
            <label className="block text-sm font-extrabold">Tip<select value={type} onChange={(event) => setType(event.target.value as CourtType)} className="form-select"><option value="STANDARD">Standard</option><option value="PANORAMIC">Panoramski</option><option value="SINGLE">Single</option></select></label>
            <Input label="Cena po satu (RSD)" type="number" min="1" step="0.01" value={hourlyPrice} onChange={(event) => setHourlyPrice(event.target.value)} leadingIcon={<CircleDollarSign size={18} />} />
            <label className="flex items-center gap-3 rounded-xl bg-remate-bg p-3 text-sm font-bold"><input type="checkbox" checked={active} onChange={(event) => setActive(event.target.checked)} className="h-5 w-5 accent-emerald-600" /> Teren je aktivan za rezervacije</label>
            <div className="flex gap-2"><Button type="submit" isLoading={mutation.isPending} icon={<Save size={17} />} className="flex-1">Sačuvaj</Button>{editing && <Button variant="tertiary" onClick={resetForm}>Otkaži</Button>}</div>
          </form>
        </aside>
      </div>
    </section>
  );
}
