import { Link } from 'react-router-dom';
import { LogIn } from 'lucide-react';

export function LoginPage() {
  return (
    <section className="mx-auto grid min-h-[70vh] max-w-md place-items-center px-5 py-14">
      <div className="w-full rounded-remate bg-white p-8 shadow-soft">
        <div className="mb-6 flex h-12 w-12 items-center justify-center rounded-2xl bg-remate-greenLight text-remate-navy">
          <LogIn size={24} />
        </div>
        <h1 className="text-3xl font-black">Prijava</h1>
        <p className="mt-2 text-remate-muted">Auth forma se implementira u prvom razvojnom ciklusu.</p>
        <div className="mt-7 rounded-xl border border-dashed border-remate-muted/30 bg-remate-bg/70 p-4 text-sm text-remate-muted">
          Sledeći korak: React Hook Form + Zod + povezivanje sa <code>/api/auth/login</code>.
        </div>
        <Link to="/register" className="mt-6 inline-flex font-bold text-remate-navy">
          Nemaš nalog? Registruj se
        </Link>
      </div>
    </section>
  );
}

