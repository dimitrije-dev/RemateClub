import { Building2, CalendarDays, ShieldCheck, UserRound } from 'lucide-react';
import { useAuth } from '../auth/AuthContext';

const roleLabels = { PLAYER: 'Igrač', OWNER: 'Vlasnik kluba', ADMIN: 'Administrator' } as const;

export function ProfilePage() {
  const { user } = useAuth();
  if (!user) return null;

  return (
    <section className="mx-auto max-w-5xl px-5 py-14">
      <p className="eyebrow">Moj nalog</p>
      <h1 className="mt-2 text-4xl font-black">Zdravo, {user.firstName}</h1>
      <p className="mt-3 text-remate-muted">Tvoja Remate Club početna tačka.</p>
      <div className="mt-8 grid gap-5 md:grid-cols-[1.2fr_1fr]">
        <article className="rounded-remate bg-white p-7 shadow-soft">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-remate-greenLight"><UserRound /></div>
            <div><h2 className="text-xl font-black">{user.firstName} {user.lastName}</h2><p className="text-remate-muted">{user.email}</p></div>
          </div>
          <div className="mt-6 flex flex-wrap gap-3 text-sm font-bold">
            <span className="profile-badge"><ShieldCheck size={16} />{roleLabels[user.role]}</span>
            <span className="profile-badge"><CalendarDays size={16} />Status: {user.status}</span>
          </div>
        </article>
        <article className="rounded-remate bg-remate-navy p-7 text-white shadow-soft">
          <Building2 className="text-remate-green" />
          <h2 className="mt-5 text-xl font-black">Sledeći korak</h2>
          <p className="mt-2 text-white/70">Klubovi, tereni i rezervacije dolaze u narednoj razvojnoj fazi.</p>
        </article>
      </div>
    </section>
  );
}
