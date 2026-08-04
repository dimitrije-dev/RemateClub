import { Link } from 'react-router-dom';
import { UserPlus } from 'lucide-react';

export function RegisterPage() {
  return (
    <section className="mx-auto grid min-h-[70vh] max-w-md place-items-center px-5 py-14">
      <div className="w-full rounded-remate bg-white p-8 shadow-soft">
        <div className="mb-6 flex h-12 w-12 items-center justify-center rounded-2xl bg-remate-greenLight text-remate-navy">
          <UserPlus size={24} />
        </div>
        <h1 className="text-3xl font-black">Registracija</h1>
        <p className="mt-2 text-remate-muted">Ovde ide registracija za igrače i vlasnike klubova.</p>
        <div className="mt-7 rounded-xl border border-dashed border-remate-muted/30 bg-remate-bg/70 p-4 text-sm text-remate-muted">
          ADMIN nalog se nikada ne kreira kroz javnu registraciju.
        </div>
        <Link to="/login" className="mt-6 inline-flex font-bold text-remate-navy">
          Već imaš nalog? Prijavi se
        </Link>
      </div>
    </section>
  );
}

