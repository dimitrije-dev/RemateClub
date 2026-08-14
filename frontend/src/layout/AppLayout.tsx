import { Building2, CalendarDays, LayoutDashboard, LogIn, LogOut, ShieldCheck, UserPlus, UserRound } from 'lucide-react';
import type { ReactNode } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { Button, ButtonLink } from '../components/ui/Button';

export function AppLayout() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/');
  }

  return (
    <div className="min-h-screen bg-remate-bg text-remate-navy">
      <header className="sticky top-0 z-20 border-b border-white/60 bg-white/90 backdrop-blur">
        <nav className="mx-auto flex max-w-6xl items-center justify-between px-5 py-3.5" aria-label="Glavna navigacija">
          <Link to="/" className="flex items-center gap-3 font-black">
            <img src="/remate-logo.png" alt="Remate Club" className="h-10 w-10 rounded-xl object-contain" />
            <span className="hidden sm:inline">Remate Club</span>
          </Link>
          <div className="hidden items-center gap-1 lg:flex">
            <NavItem to="/clubs" label="Klubovi" icon={<Building2 size={16} />} />
            {user?.role === 'PLAYER' && <NavItem to="/bookings" label="Rezervacije" icon={<CalendarDays size={16} />} />}
            {user?.role === 'OWNER' && <NavItem to="/owner" label="Panel" icon={<LayoutDashboard size={16} />} />}
            {user?.role === 'OWNER' && <NavItem to="/owner/clubs" label="Moji klubovi" icon={<Building2 size={16} />} />}
            {user?.role === 'ADMIN' && <NavItem to="/admin/clubs/pending" label="Administracija" icon={<ShieldCheck size={16} />} />}
          </div>
          <div className="flex items-center gap-1.5 sm:gap-2">
            {isAuthenticated && user ? (
              <>
                <ButtonLink to="/profile" variant="tertiary" icon={<UserRound size={18} />}>
                  <span className="hidden sm:inline">{user.firstName}</span><span className="sm:hidden">Profil</span>
                </ButtonLink>
                <Button variant="tertiary" icon={<LogOut size={18} />} onClick={handleLogout} aria-label="Odjavi se">
                  <span className="hidden sm:inline">Odjava</span>
                </Button>
              </>
            ) : (
              <>
                <ButtonLink to="/login" variant="tertiary" icon={<LogIn size={18} />}>Prijava</ButtonLink>
                <ButtonLink to="/register" variant="primary" icon={<UserPlus size={18} />}>
                  <span className="hidden sm:inline">Registracija</span><span className="sm:hidden">Nalog</span>
                </ButtonLink>
              </>
            )}
          </div>
        </nav>
        <nav className="mx-auto flex max-w-6xl gap-1 overflow-x-auto px-5 pb-3 lg:hidden" aria-label="Brza navigacija">
          <NavItem to="/clubs" label="Klubovi" icon={<Building2 size={16} />} />
          {user?.role === 'PLAYER' && <NavItem to="/bookings" label="Rezervacije" icon={<CalendarDays size={16} />} />}
          {user?.role === 'OWNER' && <NavItem to="/owner" label="Panel" icon={<LayoutDashboard size={16} />} />}
          {user?.role === 'OWNER' && <NavItem to="/owner/clubs" label="Moji klubovi" icon={<Building2 size={16} />} />}
          {user?.role === 'ADMIN' && <NavItem to="/admin/clubs/pending" label="Administracija" icon={<ShieldCheck size={16} />} />}
        </nav>
      </header>

      <main><Outlet /></main>

      <footer className="mt-16 bg-remate-navy px-5 py-10 text-white">
        <div className="mx-auto flex max-w-6xl flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div><p className="font-black">Remate Club</p><p className="mt-1 text-sm text-white/65">Igraj više. Rezerviši pametnije.</p></div>
          <div className="flex items-center gap-2 text-sm text-white/65"><CalendarDays size={17} /><span>Foundation MVP</span></div>
        </div>
      </footer>
    </div>
  );
}

function NavItem({ to, label, icon }: { to: string; label: string; icon: ReactNode }) {
  return <NavLink to={to} className={({ isActive }) => `inline-flex items-center gap-2 rounded-xl px-3 py-2 text-sm font-extrabold transition ${isActive ? 'bg-remate-greenLight text-remate-navy' : 'text-remate-muted hover:bg-remate-bg hover:text-remate-navy'}`}>{icon}{label}</NavLink>;
}
