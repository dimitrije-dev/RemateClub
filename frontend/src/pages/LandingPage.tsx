import { ArrowRight, MapPin, ShieldCheck, Trophy } from 'lucide-react';
import { ButtonLink } from '../components/ui/Button';

const benefits = [
  {
    icon: <MapPin size={24} />,
    title: 'Pronađi klubove',
    text: 'Pretraži padel klubove po gradu, terenu i dostupnim terminima.',
  },
  {
    icon: <Trophy size={24} />,
    title: 'Rezerviši brzo',
    text: 'Izaberi teren, termin i potvrdi rezervaciju kroz nekoliko koraka.',
  },
  {
    icon: <ShieldCheck size={24} />,
    title: 'Siguran backend',
    text: 'Rezervacije se proveravaju server-side da ne dođe do duplog bukiranja.',
  },
];

export function LandingPage() {
  return (
    <>
      <section className="bg-remate-navy px-5 py-20 text-white">
        <div className="mx-auto grid max-w-6xl gap-10 lg:grid-cols-[1fr_380px] lg:items-center">
          <div>
            <p className="mb-5 inline-flex rounded-full bg-remate-green/15 px-4 py-2 text-sm font-bold text-remate-greenLight">
              Padel rezervacije, pametnije organizovane
            </p>
            <h1 className="max-w-3xl text-5xl font-black leading-none sm:text-6xl">
              Igraj više. Rezerviši pametnije.
            </h1>
            <p className="mt-6 max-w-2xl text-lg text-white/72">
              Pronađi padel teren, izaberi slobodan termin i rezerviši za nekoliko trenutaka.
            </p>
            <div className="mt-9 flex flex-wrap gap-3">
              <ButtonLink to="/clubs" icon={<ArrowRight size={18} />}>
                Izaberi teren
              </ButtonLink>
              <ButtonLink to="/register" variant="secondary">
                Registruj klub
              </ButtonLink>
            </div>
          </div>
          <div className="rounded-[28px] border border-white/15 bg-white/10 p-8 shadow-soft">
            <img src="/remate-logo.png" alt="Remate Club" className="mx-auto h-48 w-48 rounded-3xl object-contain" />
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-5 py-14">
        <div className="grid gap-4 md:grid-cols-3">
          {benefits.map((benefit) => (
            <article key={benefit.title} className="rounded-remate border border-white bg-white p-6 shadow-sm">
              <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-2xl bg-remate-greenLight text-remate-navy">
                {benefit.icon}
              </div>
              <h2 className="text-xl font-black">{benefit.title}</h2>
              <p className="mt-2 text-remate-muted">{benefit.text}</p>
            </article>
          ))}
        </div>
      </section>
    </>
  );
}
