export function formatMoney(value: number, currency = 'RSD') {
  return new Intl.NumberFormat('sr-RS', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(value);
}

export function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('sr-RS', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'Europe/Belgrade',
  }).format(new Date(value));
}

export function formatTime(value: string) {
  return new Intl.DateTimeFormat('sr-RS', {
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'Europe/Belgrade',
  }).format(new Date(value));
}

export function localIsoDate(offsetDays = 0) {
  const date = new Date();
  date.setDate(date.getDate() + offsetDays);
  return new Intl.DateTimeFormat('en-CA', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    timeZone: 'Europe/Belgrade',
  }).format(date);
}

export function downloadBookingCalendar(booking: Booking) {
  const lines = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//Remate Club//Booking//SR',
    'BEGIN:VEVENT',
    `UID:${booking.id}@remate.club`,
    `DTSTAMP:${icsDate(new Date())}`,
    `DTSTART:${icsDate(new Date(booking.startAt))}`,
    `DTEND:${icsDate(new Date(booking.endAt))}`,
    `SUMMARY:${icsEscape(`Padel - ${booking.clubName}`)}`,
    `LOCATION:${icsEscape([booking.clubAddress, booking.clubCity].filter(Boolean).join(', '))}`,
    `DESCRIPTION:${icsEscape(`Teren: ${booking.courtName}. Rezervacija: ${booking.id}`)}`,
    'END:VEVENT',
    'END:VCALENDAR',
  ];
  const blob = new Blob([lines.join('\r\n')], { type: 'text/calendar;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `remate-${booking.startAt.slice(0, 10)}.ics`;
  link.click();
  URL.revokeObjectURL(url);
}

export function bookingMapUrl(booking: Booking) {
  const query = booking.clubLatitude != null && booking.clubLongitude != null
    ? `${booking.clubLatitude},${booking.clubLongitude}`
    : [booking.clubAddress, booking.clubCity].filter(Boolean).join(', ');
  return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(query)}`;
}

function icsDate(date: Date) {
  return date.toISOString().replace(/[-:]/g, '').replace(/\.\d{3}Z$/, 'Z');
}

function icsEscape(value: string) {
  return value.replace(/\\/g, '\\\\').replace(/,/g, '\\,').replace(/;/g, '\\;').replace(/\n/g, '\\n');
}
import type { Booking } from '../services/remateApi';
