import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Building2, Check, MapPin, ShieldCheck, X } from 'lucide-react';
import { EmptyState, ErrorState, LoadingState } from '../components/common/PageStates';
import { Button } from '../components/ui/Button';
import { getApiErrorMessage } from '../services/api';
import { remateApi } from '../services/remateApi';

export function PendingClubsPage() {
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['pending-clubs'], queryFn: remateApi.getPendingClubs });
  const decision = useMutation({
    mutationFn: ({ clubId, action }: { clubId: string; action: 'approve' | 'reject' }) => action === 'approve' ? remateApi.approveClub(clubId) : remateApi.rejectClub(clubId),
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['pending-clubs'] }),
  });
  return (
    <section className="page-shell">
      <div className="page-heading"><div><p className="eyebrow">Admin workspace</p><h1>Klubovi na čekanju</h1><p>Odobri samo klubove čiji su naziv i lokacija spremni za javni katalog.</p></div><span className="result-pill"><ShieldCheck size={16} /> {query.data?.length ?? 0} za pregled</span></div>
      {decision.isError && <p className="auth-error mt-6">{getApiErrorMessage(decision.error, 'Odluka nije sačuvana.')}</p>}
      <div className="mt-8 grid gap-5 lg:grid-cols-2">
        {query.isLoading && <div className="lg:col-span-2"><LoadingState label="Učitavamo prijavljene klubove…" /></div>}
        {query.isError && <div className="lg:col-span-2"><ErrorState message="Klubovi na čekanju nisu dostupni." onRetry={() => void query.refetch()} /></div>}
        {query.data?.length === 0 && <div className="lg:col-span-2"><EmptyState title="Sve je pregledano" message="Trenutno nema klubova koji čekaju odluku administratora." /></div>}
        {query.data?.map((club) => (
          <article key={club.id} className="surface-card">
            <div className="flex items-start justify-between"><span className="auth-icon !mb-0"><Building2 /></span><span className="text-xs font-black uppercase tracking-wider text-amber-700">Čeka odluku</span></div>
            <h2 className="mt-5 text-2xl font-black">{club.name}</h2><p className="mt-2 inline-flex items-center gap-2 text-remate-muted"><MapPin size={17} />{club.city}</p>
            <div className="mt-6 flex gap-3"><Button icon={<Check size={17} />} isLoading={decision.isPending} onClick={() => decision.mutate({ clubId: club.id, action: 'approve' })}>Odobri</Button><Button variant="danger" icon={<X size={17} />} disabled={decision.isPending} onClick={() => decision.mutate({ clubId: club.id, action: 'reject' })}>Odbij</Button></div>
          </article>
        ))}
      </div>
    </section>
  );
}
