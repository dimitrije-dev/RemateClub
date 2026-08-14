import type { BookingStatus, ClubStatus } from '../../services/remateApi';

const labels: Record<BookingStatus | ClubStatus, string> = {
  CONFIRMED: 'Potvrđena',
  COMPLETED: 'Završena',
  CANCELLED: 'Otkazana',
  PENDING_APPROVAL: 'Čeka odobrenje',
  APPROVED: 'Odobren',
  REJECTED: 'Odbijen',
  SUSPENDED: 'Suspendovan',
};

export function StatusBadge({ status }: { status: BookingStatus | ClubStatus }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}>{labels[status]}</span>;
}
