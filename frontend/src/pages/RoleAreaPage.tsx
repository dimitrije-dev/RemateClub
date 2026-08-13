import { ShieldCheck } from 'lucide-react';
import { useAuth } from '../auth/AuthContext';

export function RoleAreaPage({ title }: { title: string }) {
  const { user } = useAuth();
  return (
    <section className="mx-auto grid min-h-[60vh] max-w-3xl place-items-center px-5 py-14 text-center">
      <div><ShieldCheck className="mx-auto text-remate-green" size={48} /><h1 className="mt-5 text-4xl font-black">{title}</h1><p className="mt-3 text-remate-muted">Pristup potvrđen za ulogu {user?.role}.</p></div>
    </section>
  );
}
