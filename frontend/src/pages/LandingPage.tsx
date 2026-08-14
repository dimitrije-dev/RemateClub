import { ArrowRight, CalendarCheck2, MapPin, Search, ShieldCheck, Sparkles, Trophy } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
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
  const [city, setCity] = useState('');
  const navigate = useNavigate();

  function handleSearch(event: FormEvent) {
    event.preventDefault();
    const query = city.trim() ? `?city=${encodeURIComponent(city.trim())}` : '';
    navigate(`/clubs${query}`);
  }

  return (
    <>
      <section className="hero-grid relative overflow-hidden bg-remate-navy px-5 py-20 text-white sm:py-24">
        <div className="mx-auto grid max-w-6xl gap-12 lg:grid-cols-[1.15fr_0.85fr] lg:items-center">
          <div>
            <p className="mb-5 inline-flex items-center gap-2 rounded-full border border-remate-green/25 bg-remate-green/10 px-4 py-2 text-sm font-bold text-remate-greenLight">
              <Sparkles size={16} /> Padel rezervacije, konačno jednostavne
            </p>
            <h1 className="max-w-3xl text-5xl font-black leading-[0.98] tracking-tight sm:text-7xl">
              Tvoj sledeći meč počinje <span className="text-remate-green">ovde.</span>
            </h1>
            <p className="mt-6 max-w-2xl text-lg text-white/72">
              Pronađi odobren klub, proveri stvarnu dostupnost i rezerviši teren bez poziva i čekanja.
            </p>
            <form onSubmit={handleSearch} className="mt-9 flex max-w-xl flex-col gap-2 rounded-2xl bg-white p-2 shadow-soft sm:flex-row">
              <label className="flex min-h-12 flex-1 items-center gap-3 px-3 text-remate-navy">
                <MapPin className="text-remate-muted" size={20} />
                <span className="sr-only">Grad</span>
                <input value={city} onChange={(event) => setCity(event.target.value)} placeholder="Grad, npr. Beograd" className="w-full bg-transparent font-semibold outline-none" />
              </label>
              <button className="inline-flex min-h-12 items-center justify-center gap-2 rounded-xl bg-remate-green px-5 font-black text-remate-navy hover:bg-remate-greenLight">
                <Search size={18} /> Pronađi teren
              </button>
            </form>
          </div>
          <div className="relative rounded-[32px] border border-white/15 bg-white/10 p-6 shadow-soft backdrop-blur">
            <div className="rounded-[24px] bg-white p-6 text-remate-navy">
              <div className="flex items-center justify-between"><p className="font-black">Brza rezervacija</p><CalendarCheck2 className="text-emerald-600" /></div>
              <div className="mt-6 space-y-3">
                {['Izaberi klub i teren', 'Proveri slobodne slotove', 'Potvrdi rezervaciju'].map((step, index) => (
                  <div key={step} className="flex items-center gap-3 rounded-xl bg-remate-bg p-3">
                    <span className="grid h-8 w-8 place-items-center rounded-lg bg-remate-navy text-sm font-black text-white">{index + 1}</span>
                    <span className="font-bold">{step}</span>
                  </div>
                ))}
              </div>
              <ButtonLink to="/clubs" className="mt-5 w-full" icon={<ArrowRight size={18} />}>Pregledaj klubove</ButtonLink>
            </div>
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

      <section className="mx-auto max-w-6xl px-5 pb-8">
        <div className="flex flex-col items-start justify-between gap-5 rounded-[28px] bg-remate-green p-7 sm:flex-row sm:items-center sm:p-10">
          <div><p className="eyebrow">Za vlasnike klubova</p><h2 className="mt-2 text-3xl font-black">Pretvori slobodne termine u pune terene.</h2></div>
          <ButtonLink to="/register" variant="secondary">Registruj klub</ButtonLink>
        </div>
      </section>
    </>
  );
}
