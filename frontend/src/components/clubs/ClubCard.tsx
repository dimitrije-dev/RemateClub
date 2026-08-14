import { ArrowUpRight, MapPin } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { Club } from '../../services/remateApi';
import { resolveApiAssetUrl } from '../../services/api';

export function ClubCard({ club }: { club: Club }) {
  return (
    <article className="group overflow-hidden rounded-remate border border-white bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-soft">
      <div className="relative h-44 bg-remate-navy p-5 text-white">
        {club.coverImageUrl && <img src={resolveApiAssetUrl(club.coverImageUrl)} alt={`Glavna fotografija kluba ${club.name}`} className="absolute inset-0 h-full w-full object-cover" />}
        {club.coverImageUrl && <div className="absolute inset-0 bg-gradient-to-t from-remate-navy via-remate-navy/45 to-transparent" />}
        <div className="absolute -right-8 -top-12 h-32 w-32 rounded-full border-[22px] border-remate-green/20" />
        <div className="absolute inset-x-5 bottom-5"><p className="eyebrow !text-remate-greenLight">Padel klub</p><h2 className="mt-2 max-w-[85%] text-2xl font-black leading-tight">{club.name}</h2></div>
      </div>
      <div className="flex items-center justify-between gap-4 p-5">
        <span className="inline-flex items-center gap-2 text-sm font-bold text-remate-muted"><MapPin size={17} />{club.city}</span>
        <Link to={`/clubs/${club.id}`} className="inline-flex items-center gap-1 text-sm font-black text-remate-navy hover:text-emerald-700">
          Detalji <ArrowUpRight size={17} />
        </Link>
      </div>
    </article>
  );
}
