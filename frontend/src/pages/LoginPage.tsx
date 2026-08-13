import { zodResolver } from '@hookform/resolvers/zod';
import { LockKeyhole, LogIn, Mail } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { useAuth } from '../auth/AuthContext';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { getApiErrorMessage } from '../services/api';

const loginSchema = z.object({
  email: z.string().trim().min(1, 'Unesi email adresu.').email('Unesi ispravnu email adresu.'),
  password: z.string().min(1, 'Unesi lozinku.'),
});

type LoginForm = z.infer<typeof loginSchema>;

export function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [serverError, setServerError] = useState<string | null>(null);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  });

  if (isAuthenticated) return <Navigate to="/profile" replace />;

  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/profile';

  const onSubmit = handleSubmit(async (values) => {
    setServerError(null);
    try {
      await login(values);
      navigate(from, { replace: true });
    } catch (error) {
      setServerError(getApiErrorMessage(error, 'Prijava nije uspela. Proveri podatke i pokušaj ponovo.'));
    }
  });

  return (
    <section className="auth-page-shell">
      <div className="auth-card">
        <div className="auth-icon"><LogIn size={25} /></div>
        <p className="eyebrow">Dobro došao nazad</p>
        <h1 className="auth-title">Prijavi se</h1>
        <p className="auth-subtitle">Nastavi do svojih rezervacija, klubova i termina.</p>

        <form className="mt-8 space-y-5" onSubmit={onSubmit} noValidate>
          <Input
            label="Email adresa"
            type="email"
            autoComplete="email"
            placeholder="ime@primer.rs"
            leadingIcon={<Mail size={18} />}
            error={errors.email?.message}
            {...register('email')}
          />
          <Input
            label="Lozinka"
            type="password"
            autoComplete="current-password"
            placeholder="Unesi lozinku"
            leadingIcon={<LockKeyhole size={18} />}
            error={errors.password?.message}
            {...register('password')}
          />

          {serverError && <p role="alert" className="auth-error">{serverError}</p>}

          <Button type="submit" isLoading={isSubmitting} className="w-full">
            Prijavi se
          </Button>
        </form>

        <p className="mt-7 text-center text-sm text-remate-muted">
          Nemaš nalog?{' '}
          <Link to="/register" className="font-extrabold text-remate-navy underline decoration-remate-green decoration-2 underline-offset-4">
            Registruj se
          </Link>
        </p>
      </div>
    </section>
  );
}
