import { forwardRef, type ButtonHTMLAttributes, type ReactNode } from 'react';
import { Link, type LinkProps } from 'react-router-dom';

export type ButtonVariant = 'primary' | 'secondary' | 'tertiary' | 'danger';

const variants: Record<ButtonVariant, string> = {
  primary: 'bg-remate-green text-remate-navy hover:bg-remate-greenLight focus-visible:ring-remate-green',
  secondary: 'bg-remate-navy text-white hover:bg-remate-navy2 focus-visible:ring-remate-navy',
  tertiary: 'bg-transparent text-remate-navy hover:bg-remate-bg focus-visible:ring-remate-navy',
  danger: 'bg-red-600 text-white hover:bg-red-700 focus-visible:ring-red-600',
};

const baseClassName = 'inline-flex min-h-11 items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-extrabold transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-55';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
  icon?: ReactNode;
  isLoading?: boolean;
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  { variant = 'primary', icon, isLoading = false, className = '', children, disabled, type = 'button', ...props },
  ref,
) {
  return (
    <button
      ref={ref}
      type={type}
      disabled={disabled || isLoading}
      className={`${baseClassName} ${variants[variant]} ${className}`}
      {...props}
    >
      {isLoading ? <span aria-hidden className="h-4 w-4 animate-spin rounded-full border-2 border-current border-r-transparent" /> : icon}
      <span>{isLoading ? 'Sačekaj…' : children}</span>
    </button>
  );
});

type ButtonLinkProps = LinkProps & {
  variant?: ButtonVariant;
  icon?: ReactNode;
};

export function ButtonLink({ variant = 'primary', icon, className = '', children, ...props }: ButtonLinkProps) {
  return (
    <Link className={`${baseClassName} ${variants[variant]} ${className}`} {...props}>
      {icon}
      <span>{children}</span>
    </Link>
  );
}
