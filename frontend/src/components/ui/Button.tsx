import type { ReactNode } from 'react';
import { Link, type LinkProps } from 'react-router-dom';

type ButtonVariant = 'primary' | 'secondary' | 'tertiary' | 'danger';

const variants: Record<ButtonVariant, string> = {
  primary: 'bg-remate-green text-remate-navy hover:bg-remate-greenLight',
  secondary: 'bg-remate-navy text-white hover:bg-remate-navy2',
  tertiary: 'bg-transparent text-remate-navy hover:bg-remate-bg',
  danger: 'bg-red-600 text-white hover:bg-red-700',
};

type ButtonLinkProps = LinkProps & {
  variant?: ButtonVariant;
  icon?: ReactNode;
};

export function ButtonLink({ variant = 'primary', icon, className = '', children, ...props }: ButtonLinkProps) {
  return (
    <Link
      className={`inline-flex min-h-10 items-center justify-center gap-2 rounded-xl px-4 py-2 text-sm font-bold transition ${variants[variant]} ${className}`}
      {...props}
    >
      {icon}
      <span>{children}</span>
    </Link>
  );
}
