import { Ban, CalendarClock, CalendarDays, CalendarPlus, Clock3, ExternalLink, MapPin } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { Booking } from '../../services/remateApi';
import { bookingMapUrl, downloadBookingCalendar, formatDateTime, formatMoney } from '../../utils/format';
import { StatusBadge } from '../common/StatusBadge';

export function BookingCard({ booking, onCancel, cancelling = false }: { booking: Booking; onCancel?: (booking: Booking) => void; cancelling?: boolean }) {
  const upcoming = booking.status === 'CONFIRMED' && new Date(booking.endAt).getTime() >= Date.now();
  const canChange = booking.status === 'CONFIRMED' && new Date(booking.startAt).getTime() > Date.now();
  const rescheduleDate = new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Belgrade' }).format(new Date(booking.startAt));
  return (
    <article className="surface-card">
      <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-start">
        <div>
          <div className="flex flex-wrap items-center gap-3"><StatusBadge status={upcoming ? booking.status : booking.status === 'CONFIRMED' ? 'COMPLETED' : booking.status} /><span className="text-xs font-bold text-remate-muted">#{booking.id.slice(0, 8)}</span></div>
          <h2 className="mt-3 text-xl font-black">{booking.clubName || 'Padel rezervacija'}</h2>
          <p className="mt-1 text-sm font-bold text-remate-muted">{booking.courtName}</p>
          <div className="mt-3 flex flex-wrap gap-x-5 gap-y-2 text-sm text-remate-muted">
            <span className="inline-flex items-center gap-2"><CalendarDays size={16} />{formatDateTime(booking.startAt)}</span>
            <span className="inline-flex items-center gap-2"><Clock3 size={16} />{formatMoney(booking.totalPrice, booking.currency)}</span>
            <span className="inline-flex items-center gap-2"><MapPin size={16} />{booking.clubAddress ?? booking.clubCity}</span>
          </div>
        </div>
        <Link to={`/bookings/${booking.id}`} className="inline-flex items-center gap-1 text-sm font-black text-emerald-700 hover:text-remate-navy">Detalji <ExternalLink size={15} /></Link>
      </div>
      <div className="mt-5 flex flex-wrap gap-2 border-t border-slate-100 pt-4">
        {canChange && <Link to={`/clubs/${booking.clubId}?date=${rescheduleDate}&reschedule=${booking.id}`} className="inline-flex items-center gap-2 rounded-xl bg-remate-greenLight px-3 py-2 text-sm font-black text-remate-navy hover:bg-remate-green"><CalendarClock size={16} />Promeni termin</Link>}
        <button type="button" onClick={() => downloadBookingCalendar(booking)} className="inline-flex items-center gap-2 rounded-xl bg-remate-bg px-3 py-2 text-sm font-black hover:bg-slate-200"><CalendarPlus size={16} />Dodaj u kalendar</button>
        <a href={bookingMapUrl(booking)} target="_blank" rel="noreferrer" className="inline-flex items-center gap-2 rounded-xl bg-remate-bg px-3 py-2 text-sm font-black hover:bg-slate-200"><MapPin size={16} />Otvori lokaciju</a>
        {canChange && onCancel && <button type="button" disabled={cancelling} onClick={() => onCancel(booking)} className="inline-flex items-center gap-2 rounded-xl bg-red-50 px-3 py-2 text-sm font-black text-red-700 hover:bg-red-100 disabled:opacity-60"><Ban size={16} />{cancelling ? 'Otkazujemo…' : 'Otkaži'}</button>}
      </div>
    </article>
  );
}
