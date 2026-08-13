import { zodResolver } from '@hookform/resolvers/zod';
import { Building2, LockKeyhole, Mail, UserRound, UserPlus } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { useAuth } from '../auth/AuthContext';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { getApiErrorMessage } from '../services/api';

const registerSchema = z.object({
  firstName: z.string().trim().min(2, 'Ime mora imati najmanje 2 karaktera.').max(80),
  lastName: z.string().trim().min(2, 'Prezime mora imati najmanje 2 karaktera.').max(80),
  email: z.string().trim().min(1, 'Unesi email adresu.').email('Unesi ispravnu email adresu.'),
  password: z.string().min(8, 'Lozinka mora imati najmanje 8 karaktera.').max(72),
  confirmPassword: z.string().min(1, 'Ponovi lozinku.'),
  role: z.enum(['PLAYER', 'OWNER']),
}).refine((values) => values.password === values.confirmPassword, {
  path: ['confirmPassword'],
  message: 'Lozinke se ne podudaraju.',
});

type RegisterForm = z.infer<typeof registerSchema>;

export function RegisterPage() {
  const { register: createAccount, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);
  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      firstName: '', lastName: '', email: '', password: '', confirmPassword: '', role: 'PLAYER',
    },
  });

  if (isAuthenticated) return <Navigate to="/profile" replace />;

  const selectedRole = watch('role');
  const onSubmit = handleSubmit(async ({ confirmPassword: _confirmPassword, ...values }) => {
    setServerError(null);
    try {
      await createAccount(values);
      navigate('/profile', { replace: true });
    } catch (error) {
      setServerError(getApiErrorMessage(error, 'Registracija nije uspela. Pokušaj ponovo.'));
    }
  });

  return (
    <section className="auth-page-shell py-10 sm:py-14">
      <div className="auth-card max-w-2xl">
        <div className="auth-icon"><UserPlus size={25} /></div>
        <p className="eyebrow">Novi Remate Club nalog</p>
        <h1 className="auth-title">Kreiraj nalog</h1>
        <p className="auth-subtitle">Izaberi tip naloga i pripremi se za sledeći meč.</p>

        <form className="mt-8 space-y-5" onSubmit={onSubmit} noValidate>
          <fieldset>
            <legend className="mb-2 block text-sm font-extrabold text-remate-navy">Tip naloga</legend>
            <div className="grid gap-3 sm:grid-cols-2">
              <label className={`role-option ${selectedRole === 'PLAYER' ? 'role-option-active' : ''}`}>
                <input type="radio" value="PLAYER" className="sr-only" {...register('role')} />
                <UserRound size={21} />
                <span><strong>Igrač</strong><small>Pronađi i rezerviši teren</small></span>
              </label>
              <label className={`role-option ${selectedRole === 'OWNER' ? 'role-option-active' : ''}`}>
                <input type="radio" value="OWNER" className="sr-only" {...register('role')} />
                <Building2 size={21} />
                <span><strong>Vlasnik kluba</strong><small>Upravljaj klubom i terenima</small></span>
              </label>
            </div>
          </fieldset>

          <div className="grid gap-5 sm:grid-cols-2">
            <Input label="Ime" autoComplete="given-name" placeholder="Nikola" leadingIcon={<UserRound size={18} />} error={errors.firstName?.message} {...register('firstName')} />
            <Input label="Prezime" autoComplete="family-name" placeholder="Jokić" leadingIcon={<UserRound size={18} />} error={errors.lastName?.message} {...register('lastName')} />
          </div>
          <Input label="Email adresa" type="email" autoComplete="email" placeholder="ime@primer.rs" leadingIcon={<Mail size={18} />} error={errors.email?.message} {...register('email')} />
          <div className="grid gap-5 sm:grid-cols-2">
            <Input label="Lozinka" type="password" autoComplete="new-password" placeholder="Najmanje 8 karaktera" leadingIcon={<LockKeyhole size={18} />} error={errors.password?.message} {...register('password')} />
            <Input label="Ponovi lozinku" type="password" autoComplete="new-password" placeholder="Ponovi lozinku" leadingIcon={<LockKeyhole size={18} />} error={errors.confirmPassword?.message} {...register('confirmPassword')} />
          </div>

          {serverError && <p role="alert" className="auth-error">{serverError}</p>}

          <Button type="submit" isLoading={isSubmitting} className="w-full">Kreiraj nalog</Button>
        </form>

        <p className="mt-7 text-center text-sm text-remate-muted">
          Već imaš nalog?{' '}
          <Link to="/login" className="font-extrabold text-remate-navy underline decoration-remate-green decoration-2 underline-offset-4">
            Prijavi se
          </Link>
        </p>
      </div>
    </section>
  );
}
