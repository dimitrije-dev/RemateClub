import { Link, Outlet } from 'react-router-dom';
import { CalendarDays, LogIn, UserPlus } from 'lucide-react';
import { ButtonLink } from '../components/ui/Button';

export function AppLayout() {
  return (
    <div className="min-h-screen bg-remate-bg text-remate-navy">
      <header className="border-b border-white/60 bg-white/80 backdrop-blur">
        <nav className="mx-auto flex max-w-6xl items-center justify-between px-5 py-4">
          <Link to="/" className="flex items-center gap-3 font-black">
            <img src="/remate-logo.png" alt="Remate Club" className="h-10 w-10 rounded-xl object-contain" />
            <span>Remate Club</span>
          </Link>
          <div className="flex items-center gap-2">
            <ButtonLink to="/login" variant="tertiary" icon={<LogIn size={18} />}>
              Prijava
            </ButtonLink>
            <ButtonLink to="/register" variant="primary" icon={<UserPlus size={18} />}>
              Registracija
            </ButtonLink>
          </div>
        </nav>
      </header>

      <main>
        <Outlet />
      </main>

      <footer className="mt-16 bg-remate-navy px-5 py-10 text-white">
        <div className="mx-auto flex max-w-6xl flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="font-black">Remate Club</p>
            <p className="mt-1 text-sm text-white/65">Igraj više. Rezerviši pametnije.</p>
          </div>
          <div className="flex items-center gap-2 text-sm text-white/65">
            <CalendarDays size={17} />
            <span>Foundation MVP</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

