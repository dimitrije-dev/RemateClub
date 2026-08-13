import { ShieldX } from 'lucide-react';
import { ButtonLink } from '../components/ui/Button';

export function UnauthorizedPage() {
  return (
    <section className="mx-auto grid min-h-[65vh] max-w-2xl place-items-center px-5 py-14 text-center">
      <div>
        <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-red-100 text-red-700"><ShieldX size={31} /></div>
        <p className="eyebrow mt-6">Greška 403</p>
        <h1 className="mt-2 text-4xl font-black">Nemaš pristup ovoj stranici</h1>
        <p className="mx-auto mt-3 max-w-lg text-remate-muted">Tvoj nalog nema potrebnu ulogu. Vrati se na svoj profil ili početnu stranicu.</p>
        <div className="mt-8 flex justify-center gap-3">
          <ButtonLink to="/profile">Moj profil</ButtonLink>
          <ButtonLink to="/" variant="tertiary">Početna</ButtonLink>
        </div>
      </div>
    </section>
  );
}
