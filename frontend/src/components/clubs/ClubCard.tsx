import { ArrowUpRight, Clock3, MapPin, Star } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { Club } from '../../services/remateApi';
import { resolveApiAssetUrl } from '../../services/api';
import { formatMoney, formatTime } from '../../utils/format';

export function ClubCard({
  club,
  selectedDate,
  distanceKm,
}: {
  club: Club;
  selectedDate?: string;
  distanceKm?: number;
}) {
  const detailsUrl = selectedDate ? `/clubs/${club.id}?date=${selectedDate}` : `/clubs/${club.id}`;
  const distanceAvailable = distanceKm != null && Number.isFinite(distanceKm);
  return (
    <article className="group overflow-hidden rounded-remate border border-white bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-soft">
      <div className="relative h-44 bg-remate-navy p-5 text-white">
        {club.coverImageUrl && <img src={resolveApiAssetUrl(club.coverImageUrl)} alt={`Glavna fotografija kluba ${club.name}`} className="absolute inset-0 h-full w-full object-cover" />}
        {club.coverImageUrl && <div className="absolute inset-0 bg-gradient-to-t from-remate-navy via-remate-navy/45 to-transparent" />}
        <div className="absolute -right-8 -top-12 h-32 w-32 rounded-full border-[22px] border-remate-green/20" />
        <div className="absolute inset-x-5 bottom-5"><div className="flex items-center justify-between gap-2"><p className="eyebrow !text-remate-greenLight">Padel klub</p>{(club.averageRating ?? 0) > 0 && <span className="inline-flex items-center gap-1 rounded-full bg-white/15 px-2 py-1 text-xs font-black"><Star size={13} fill="currentColor" />{club.averageRating?.toFixed(1)}</span>}</div><h2 className="mt-2 max-w-[90%] text-2xl font-black leading-tight">{club.name}</h2></div>
      </div>
      <div className="space-y-4 p-5">
        <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm font-bold text-remate-muted">
          <span className="inline-flex items-center gap-2"><MapPin size={17} />{club.city}</span>
          {distanceAvailable && <span>{distanceKm! < 1 ? `${Math.round(distanceKm! * 1000)} m` : `${distanceKm!.toFixed(1)} km`}</span>}
        </div>
        <div className="flex flex-wrap gap-2">
          {club.environments?.map((item) => <span key={item} className="rounded-full bg-remate-bg px-2.5 py-1 text-xs font-black text-remate-muted">{item === 'INDOOR' ? 'Indoor' : 'Outdoor'}</span>)}
          {club.minimumHourlyPrice != null && <span className="rounded-full bg-remate-greenLight px-2.5 py-1 text-xs font-black">od {formatMoney(club.minimumHourlyPrice)}/h</span>}
        </div>
        <div className={`rounded-xl px-3 py-2.5 text-sm ${club.earliestAvailableAt ? 'bg-emerald-50 text-emerald-800' : 'bg-slate-100 text-slate-500'}`}>
          <span className="inline-flex items-center gap-2 font-black"><Clock3 size={16} />{club.earliestAvailableAt ? `Prvi termin u ${formatTime(club.earliestAvailableAt)}` : 'Nema slobodnih termina tog dana'}</span>
        </div>
        <Link to={detailsUrl} className="inline-flex w-full items-center justify-center gap-1 rounded-xl bg-remate-navy px-4 py-3 text-sm font-black text-white transition hover:bg-emerald-700">
          Izaberi termin <ArrowUpRight size={17} />
        </Link>
      </div>
    </article>
  );
}
