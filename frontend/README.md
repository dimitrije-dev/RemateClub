# Remate Club frontend

React 19 + TypeScript klijent za javni katalog, booking tok i role-aware PLAYER, OWNER i ADMIN ekrane.

## Pokretanje

```bash
npm ci
cp .env.example .env
npm run dev
```

Podrazumevana adresa je <http://localhost:5173>. Backend treba da bude dostupan na vrednosti `VITE_API_BASE_URL`, podrazumevano `http://localhost:8080/api`.

## Komande

```bash
npm run dev          # Vite development server
npm test -- --run    # svi Vitest testovi jednom
npm run typecheck    # TypeScript bez emitovanja fajlova
npm run build        # production bundle u dist/
npm run preview      # lokalni pregled production builda
```

## Organizacija

```text
src/auth/             AuthContext i obnova sesije
src/components/       zajednički UI, kartice i page states
src/layout/           glavni header/footer layout
src/pages/            javni, auth, player, owner i admin ekrani
src/routes/           router i role zaštita
src/services/         Axios klijent i API funkcije/tipovi
src/styles/           Tailwind slojevi i design tokens
src/test/             test setup i auth helperi
```

Access token se drži u memoriji, a opaque refresh token u local storage-u. Axios interceptor radi jednu kontrolisanu obnovu sesije posle `401`; neuspešna obnova čisti sesiju.

## Glavne rute

- Javno: `/`, `/clubs`, `/clubs/:clubId`, `/login`, `/register`
- Zaštićeno: `/profile`
- PLAYER: `/bookings`, `/bookings/:bookingId`
- OWNER: `/owner`, `/owner/clubs`, club editor, courts i `/owner/reservations`
- ADMIN: `/admin/clubs/pending`

## Fotografije klubova

Upload je dostupan OWNER korisniku na `/owner/clubs/{clubId}/edit`. Podržani su JPEG, PNG i WebP fajlovi do 5 MB, u rasponu od 600 × 400 do 6000 × 6000 px. Preporuka je WebP `1600 × 900` za cover i `1600 × 1200` za galeriju. UI radi preliminarnu proveru, dok backend ponavlja proveru stvarnog sadržaja, veličine i dimenzija.

Detaljan prolaz kroz ekrane i razvojni nalozi nalaze se u root [README-u](../README.md).
