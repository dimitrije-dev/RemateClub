import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ImagePlus, Star, Trash2, Upload } from 'lucide-react';
import { useRef, useState, type FormEvent } from 'react';
import { getApiErrorMessage, resolveApiAssetUrl } from '../../services/api';
import { remateApi } from '../../services/remateApi';
import { ErrorState, LoadingState } from '../common/PageStates';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';

const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
const maxFileSize = 5 * 1024 * 1024;

export function ClubImageManager({ clubId }: { clubId: string }) {
  const [file, setFile] = useState<File | null>(null);
  const [altText, setAltText] = useState('');
  const [cover, setCover] = useState(false);
  const [error, setError] = useState('');
  const fileInput = useRef<HTMLInputElement>(null);
  const queryClient = useQueryClient();
  const queryKey = ['owner-club-images', clubId];
  const imagesQuery = useQuery({ queryKey, queryFn: () => remateApi.getOwnerClubImages(clubId) });

  const refresh = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey }),
      queryClient.invalidateQueries({ queryKey: ['owner-clubs'] }),
      queryClient.invalidateQueries({ queryKey: ['clubs'] }),
      queryClient.invalidateQueries({ queryKey: ['club', clubId] }),
      queryClient.invalidateQueries({ queryKey: ['club-images', clubId] }),
    ]);
  };

  const uploadMutation = useMutation({
    mutationFn: async () => {
      if (!file) throw new Error('Izaberi sliku.');
      await validateClientImage(file);
      return remateApi.uploadClubImage(clubId, file, altText, cover);
    },
    onSuccess: async () => {
      setFile(null); setAltText(''); setCover(false); setError('');
      if (fileInput.current) fileInput.current.value = '';
      await refresh();
    },
    onError: (uploadError) => setError(getApiErrorMessage(uploadError, 'Slika nije dodata.')),
  });
  const coverMutation = useMutation({
    mutationFn: (imageId: string) => remateApi.makeClubImageCover(clubId, imageId),
    onSuccess: refresh,
    onError: (coverError) => setError(getApiErrorMessage(coverError, 'Cover slika nije promenjena.')),
  });
  const deleteMutation = useMutation({
    mutationFn: (imageId: string) => remateApi.deleteClubImage(clubId, imageId),
    onSuccess: refresh,
    onError: (deleteError) => setError(getApiErrorMessage(deleteError, 'Slika nije obrisana.')),
  });

  function submit(event: FormEvent) {
    event.preventDefault(); setError('');
    if (!file) { setError('Izaberi sliku.'); return; }
    if (!altText.trim()) { setError('Kratak opis slike je obavezan zbog pristupačnosti.'); return; }
    uploadMutation.mutate();
  }

  return (
    <section className="mt-6 surface-card sm:p-9">
      <div className="flex items-start gap-4">
        <div className="auth-icon !mb-0"><ImagePlus /></div>
        <div><p className="eyebrow">Galerija kluba</p><h2 className="mt-1 text-2xl font-black">Fotografije</h2><p className="mt-2 text-sm text-remate-muted">Do 8 slika. WebP je najbolji izbor; JPEG i PNG su takođe podržani.</p></div>
      </div>

      <div className="mt-6 rounded-2xl border border-emerald-100 bg-emerald-50 p-4 text-sm text-emerald-950">
        <strong>Preporuka:</strong> cover 1600 × 900 px (16:9), galerija 1600 × 1200 px (4:3), najmanje 600 × 400 px, najviše 5 MB po slici.
      </div>

      <form onSubmit={submit} className="mt-6 grid gap-4">
        {error && <p className="auth-error">{error}</p>}
        <label className="text-sm font-extrabold text-remate-navy">Slika
          <input ref={fileInput} type="file" accept="image/jpeg,image/png,image/webp" onChange={(event) => setFile(event.target.files?.[0] ?? null)} className="form-select file:mr-4 file:rounded-lg file:border-0 file:bg-remate-greenLight file:px-3 file:py-2 file:font-bold" />
        </label>
        <Input label="Opis slike" value={altText} onChange={(event) => setAltText(event.target.value)} maxLength={180} placeholder="Npr. Panoramski teren kluba u večernjem terminu" />
        <label className="flex items-center gap-3 text-sm font-bold"><input type="checkbox" checked={cover} onChange={(event) => setCover(event.target.checked)} className="h-5 w-5 accent-emerald-500" /> Postavi kao glavnu cover sliku</label>
        <Button type="submit" isLoading={uploadMutation.isPending} icon={<Upload size={18} />} className="sm:w-fit">Dodaj sliku</Button>
      </form>

      <div className="mt-8">
        {imagesQuery.isLoading && <LoadingState label="Učitavamo galeriju…" />}
        {imagesQuery.isError && <ErrorState message="Galerija trenutno nije dostupna." onRetry={() => void imagesQuery.refetch()} />}
        {imagesQuery.data?.length === 0 && <p className="rounded-2xl bg-remate-bg p-5 text-sm text-remate-muted">Još nema dodatih slika.</p>}
        <div className="grid gap-4 sm:grid-cols-2">
          {imagesQuery.data?.map((image) => (
            <article key={image.id} className="overflow-hidden rounded-2xl border border-slate-200 bg-white">
              <div className="relative aspect-[4/3] bg-remate-navy">
                <img src={resolveApiAssetUrl(image.url)} alt={image.altText} className="h-full w-full object-cover" />
                {image.cover && <span className="absolute left-3 top-3 rounded-full bg-remate-green px-3 py-1 text-xs font-black text-remate-navy">Cover</span>}
              </div>
              <div className="p-4"><p className="text-sm font-bold">{image.altText}</p><p className="mt-1 text-xs text-remate-muted">{image.width} × {image.height} px · {(image.fileSize / 1024 / 1024).toFixed(2)} MB</p>
                <div className="mt-4 flex flex-wrap gap-2">
                  {!image.cover && <Button variant="tertiary" icon={<Star size={16} />} disabled={coverMutation.isPending} onClick={() => coverMutation.mutate(image.id)}>Postavi cover</Button>}
                  <Button variant="danger" icon={<Trash2 size={16} />} disabled={deleteMutation.isPending} onClick={() => { if (window.confirm('Obrisati ovu sliku?')) deleteMutation.mutate(image.id); }}>Obriši</Button>
                </div>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}

async function validateClientImage(file: File) {
  if (!allowedTypes.includes(file.type)) throw new Error('Dozvoljeni formati su JPEG, PNG i WebP.');
  if (file.size > maxFileSize) throw new Error('Slika može imati najviše 5 MB.');
  const bitmap = await createImageBitmap(file);
  const { width, height } = bitmap;
  bitmap.close();
  if (width < 600 || height < 400) throw new Error('Slika mora biti najmanje 600 × 400 px.');
  if (width > 6000 || height > 6000) throw new Error('Slika može biti najviše 6000 × 6000 px.');
}
