import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <section className="mx-auto grid min-h-[65vh] max-w-2xl place-items-center px-5 py-14 text-center">
      <div>
        <p className="text-sm font-black uppercase text-remate-muted">404</p>
        <h1 className="mt-2 text-4xl font-black">Stranica nije pronađena</h1>
        <p className="mt-3 text-remate-muted">Proveri adresu ili se vrati na početnu stranicu.</p>
        <Link to="/" className="mt-8 inline-flex rounded-xl bg-remate-green px-5 py-3 font-black text-remate-navy">
          Nazad na početnu
        </Link>
      </div>
    </section>
  );
}
