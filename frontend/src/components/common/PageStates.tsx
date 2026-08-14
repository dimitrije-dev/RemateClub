import { AlertTriangle, Inbox, LoaderCircle, RefreshCw } from 'lucide-react';
import type { ReactNode } from 'react';
import { Button } from '../ui/Button';

export function LoadingState({ label = 'Učitavamo podatke…' }: { label?: string }) {
  return (
    <div className="state-card" role="status">
      <LoaderCircle className="animate-spin text-remate-green" size={28} />
      <p>{label}</p>
    </div>
  );
}

export function EmptyState({ title, message, action }: { title: string; message: string; action?: ReactNode }) {
  return (
    <div className="state-card">
      <Inbox className="text-remate-muted" size={32} />
      <div><h2 className="font-black">{title}</h2><p className="mt-1 text-sm text-remate-muted">{message}</p></div>
      {action}
    </div>
  );
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="state-card border-red-200 bg-red-50 text-red-800" role="alert">
      <AlertTriangle size={30} />
      <div><h2 className="font-black">Nešto nije u redu</h2><p className="mt-1 text-sm">{message}</p></div>
      {onRetry && <Button variant="danger" icon={<RefreshCw size={16} />} onClick={onRetry}>Pokušaj ponovo</Button>}
    </div>
  );
}
