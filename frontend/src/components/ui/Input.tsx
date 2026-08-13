import { forwardRef, useId, type InputHTMLAttributes, type ReactNode } from 'react';

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
  hint?: string;
  leadingIcon?: ReactNode;
};

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { label, error, hint, leadingIcon, className = '', id, ...props },
  ref,
) {
  const generatedId = useId();
  const inputId = id ?? generatedId;
  const descriptionId = error || hint ? `${inputId}-description` : undefined;

  return (
    <div className="space-y-2">
      <label htmlFor={inputId} className="block text-sm font-extrabold text-remate-navy">
        {label}
      </label>
      <div className="relative">
        {leadingIcon && (
          <span className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-remate-muted" aria-hidden>
            {leadingIcon}
          </span>
        )}
        <input
          ref={ref}
          id={inputId}
          aria-invalid={Boolean(error)}
          aria-describedby={descriptionId}
          className={`min-h-12 w-full rounded-xl border bg-white px-3.5 py-2.5 text-remate-navy outline-none transition placeholder:text-slate-400 focus:ring-2 ${leadingIcon ? 'pl-11' : ''} ${error ? 'border-red-500 focus:border-red-500 focus:ring-red-100' : 'border-slate-300 focus:border-remate-green focus:ring-remate-green/20'} ${className}`}
          {...props}
        />
      </div>
      {(error || hint) && (
        <p id={descriptionId} className={`text-sm ${error ? 'font-semibold text-red-600' : 'text-remate-muted'}`}>
          {error ?? hint}
        </p>
      )}
    </div>
  );
});
