import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Building2, MapPin, Save } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ErrorState, LoadingState } from '../components/common/PageStates';
import { ClubImageManager } from '../components/clubs/ClubImageManager';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { getApiErrorMessage } from '../services/api';
import { remateApi } from '../services/remateApi';

export function ClubEditorPage() {
  const { clubId } = useParams();
  const isEditing = Boolean(clubId);
  const [name, setName] = useState('');
  const [city, setCity] = useState('');
  const [address, setAddress] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const clubsQuery = useQuery({ queryKey: ['owner-clubs'], queryFn: remateApi.getOwnerClubs, enabled: isEditing });
  const club = clubsQuery.data?.find((item) => item.id === clubId);

  useEffect(() => {
    if (club) {
      setName(club.name);
      setCity(club.city);
      setAddress(club.address ?? '');
      setLatitude(club.latitude == null ? '' : String(club.latitude));
      setLongitude(club.longitude == null ? '' : String(club.longitude));
    }
  }, [club]);
  const mutation = useMutation({
    mutationFn: () => {
      const payload = {
        name,
        city,
        address: address.trim() || undefined,
        latitude: latitude ? Number(latitude) : undefined,
        longitude: longitude ? Number(longitude) : undefined,
      };
      return isEditing && clubId ? remateApi.updateClub(clubId, payload) : remateApi.createClub(payload);
    },
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: ['owner-clubs'] }); navigate('/owner/clubs'); },
    onError: (mutationError) => setError(getApiErrorMessage(mutationError, 'Klub nije sačuvan.')),
  });

  function submit(event: FormEvent) {
    event.preventDefault(); setError('');
    if (!name.trim() || !city.trim()) { setError('Naziv i grad su obavezni.'); return; }
    if ((latitude && !Number.isFinite(Number(latitude))) || (longitude && !Number.isFinite(Number(longitude)))) {
      setError('Koordinate moraju biti ispravni brojevi.'); return;
    }
    if ((latitude && (Number(latitude) < -90 || Number(latitude) > 90)) || (longitude && (Number(longitude) < -180 || Number(longitude) > 180))) {
      setError('Geografska širina mora biti između -90 i 90, a dužina između -180 i 180.'); return;
    }
    if (Boolean(latitude) !== Boolean(longitude)) {
      setError('Unesi obe koordinate ili ostavi oba polja prazna.'); return;
    }
    mutation.mutate();
  }

  if (isEditing && clubsQuery.isLoading) return <section className="page-shell"><LoadingState /></section>;
  if (isEditing && clubsQuery.isError) return <section className="page-shell"><ErrorState message="Klub nije moguće učitati." /></section>;
  if (isEditing && clubsQuery.data && !club) return <section className="page-shell"><ErrorState message="Klub nije pronađen među tvojim klubovima." /></section>;

  return (
    <section className="page-shell max-w-3xl">
      <Link to="/owner/clubs" className="back-link"><ArrowLeft size={17} /> Moji klubovi</Link>
      <div className="mt-5 surface-card sm:p-9"><div className="auth-icon"><Building2 /></div><p className="eyebrow">Owner workspace</p><h1 className="auth-title">{isEditing ? 'Uredi klub' : 'Dodaj novi klub'}</h1><p className="auth-subtitle">Promene osnovnih podataka ostaju vezane samo za tvoj nalog.</p>
        <form onSubmit={submit} className="mt-8 space-y-5">
          {error && <p className="auth-error">{error}</p>}
          <Input label="Naziv kluba" value={name} onChange={(event) => setName(event.target.value)} leadingIcon={<Building2 size={18} />} maxLength={160} />
          <Input label="Grad" value={city} onChange={(event) => setCity(event.target.value)} leadingIcon={<MapPin size={18} />} maxLength={120} />
          <Input label="Adresa" value={address} onChange={(event) => setAddress(event.target.value)} leadingIcon={<MapPin size={18} />} maxLength={255} placeholder="Ulica i broj" />
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Geografska širina" type="number" min="-90" max="90" step="0.000001" value={latitude} onChange={(event) => setLatitude(event.target.value)} placeholder="44.817600" />
            <Input label="Geografska dužina" type="number" min="-180" max="180" step="0.000001" value={longitude} onChange={(event) => setLongitude(event.target.value)} placeholder="20.463300" />
          </div>
          <p className="text-xs text-remate-muted">Koordinate omogućavaju sortiranje klubova po udaljenosti i otvaranje lokacije na mapi.</p>
          <Button type="submit" isLoading={mutation.isPending} icon={<Save size={18} />} className="w-full">{isEditing ? 'Sačuvaj izmene' : 'Kreiraj klub'}</Button>
        </form>
      </div>
      {isEditing && clubId && <ClubImageManager clubId={clubId} />}
    </section>
  );
}
