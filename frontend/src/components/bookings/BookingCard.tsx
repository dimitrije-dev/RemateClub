import { CalendarDays, Clock3, MapPin } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { Booking } from '../../services/remateApi';
import { formatDateTime, formatMoney } from '../../utils/format';
import { StatusBadge } from '../common/StatusBadge';

export function BookingCard({ booking }: { booking: Booking }) {
  return (
    <article className="surface-card flex flex-col justify-between gap-5 sm:flex-row sm:items-center">
      <div>
        <div className="flex flex-wrap items-center gap-3"><StatusBadge status={booking.status} /><span className="text-xs font-bold text-remate-muted">#{booking.id.slice(0, 8)}</span></div>
        <h2 className="mt-3 text-xl font-black">Rezervacija terena</h2>
        <div className="mt-2 flex flex-wrap gap-x-5 gap-y-2 text-sm text-remate-muted">
          <span className="inline-flex items-center gap-2"><CalendarDays size={16} />{formatDateTime(booking.startAt)}</span>
          <span className="inline-flex items-center gap-2"><Clock3 size={16} />{formatMoney(booking.totalPrice, booking.currency)}</span>
          <span className="inline-flex items-center gap-2"><MapPin size={16} />Teren {booking.courtId.slice(0, 8)}</span>
        </div>
      </div>
      <Link to={`/bookings/${booking.id}`} className="text-sm font-black text-emerald-700 hover:text-remate-navy">Pogledaj detalje →</Link>
    </article>
  );
}
