# Remate Club API endpoints

Base URL za lokalni razvoj je `http://localhost:8080/api`. Za zaštićene rute koristi se `Authorization: Bearer <accessToken>`.

## Health i autentikacija

| Method | Ruta | Pristup | Namena |
| --- | --- | --- | --- |
| GET | `/health` | Public | Status backend servisa |
| POST | `/auth/register` | Public | Registracija PLAYER ili OWNER naloga |
| POST | `/auth/login` | Public | Access/refresh token i user DTO |
| POST | `/auth/refresh` | Public | Rotacija refresh tokena i novi token par |
| GET | `/auth/me` | Authenticated | Trenutni korisnik iz JWT subject-a |

## Javni klubovi, tereni i dostupnost

| Method | Ruta | Pristup | Namena |
| --- | --- | --- | --- |
| GET | `/clubs` | Public | Samo odobreni klubovi |
| GET | `/clubs/{clubId}` | Public | Detalji odobrenog kluba |
| GET | `/clubs/{clubId}/courts` | Public | Aktivni tereni odobrenog kluba |
| GET | `/clubs/{clubId}/images` | Public | Cover i galerija odobrenog kluba |
| GET | `/club-images/{imageId}/content` | Public | Sadržaj slike po nepredvidivom UUID identifikatoru |
| GET | `/courts/{courtId}/availability?date=YYYY-MM-DD` | Public | Satni slotovi, dostupnost i backend cena |

## Rezervacije

| Method | Ruta | Pristup | Namena |
| --- | --- | --- | --- |
| POST | `/bookings` | PLAYER | Kreiranje rezervacije uz lock, overlap proveru i obračun cene |
| GET | `/bookings/me` | PLAYER | Rezervacije prijavljenog igrača |
| GET | `/bookings/{bookingId}` | Player, owning OWNER ili ADMIN | Detalji rezervacije |
| PATCH | `/bookings/{bookingId}/cancel` | Player, owning OWNER ili ADMIN | Otkazivanje potvrđene rezervacije |
| GET | `/owner/bookings` | OWNER | Rezervacije kroz sve klubove prijavljenog vlasnika |

## Owner upravljanje

| Method | Ruta | Pristup | Namena |
| --- | --- | --- | --- |
| GET | `/owner/clubs` | OWNER | Klubovi prijavljenog vlasnika |
| POST | `/owner/clubs` | OWNER | Kreiranje kluba u `PENDING_APPROVAL` statusu |
| PUT | `/owner/clubs/{clubId}` | Owning OWNER | Izmena osnovnih podataka kluba |
| GET | `/owner/clubs/{clubId}/courts` | Owning OWNER | Svi tereni owned kluba |
| POST | `/owner/clubs/{clubId}/courts` | Owning OWNER | Kreiranje terena |
| PUT | `/owner/clubs/{clubId}/courts/{courtId}` | Owning OWNER | Izmena terena, tipa, cene i aktivnosti |
| GET | `/owner/clubs/{clubId}/images` | Owning OWNER | Sve slike kluba, uključujući klub na čekanju |
| POST | `/owner/clubs/{clubId}/images` | Owning OWNER | Multipart upload JPEG/PNG/WebP slike, alt teksta i cover zastavice |
| PATCH | `/owner/clubs/{clubId}/images/{imageId}/cover` | Owning OWNER | Postavljanje glavne slike |
| DELETE | `/owner/clubs/{clubId}/images/{imageId}` | Owning OWNER | Brisanje slike i izbor sledećeg covera |

## Admin moderacija

| Method | Ruta | Pristup | Namena |
| --- | --- | --- | --- |
| GET | `/admin/clubs/pending` | ADMIN | Klubovi koji čekaju odluku |
| PATCH | `/admin/clubs/{clubId}/approve` | ADMIN | Odobravanje i javna vidljivost kluba |
| PATCH | `/admin/clubs/{clubId}/reject` | ADMIN | Odbijanje kluba |

## Odgovori i greške

- `400 BAD_REQUEST` — nevalidan payload ili period rezervacije.
- `401 UNAUTHORIZED` — nedostaje ili nije validan JWT.
- `403 FORBIDDEN` — uloga ili ownership ne dozvoljava akciju.
- `404 RESOURCE_NOT_FOUND` — resurs nije pronađen ili nije javno vidljiv.
- `409 CONFLICT` — konflikt domena.
- `409 BOOKING_TIME_UNAVAILABLE` — booking ili court block preklapa traženi period.

Swagger UI: <http://localhost:8080/swagger-ui.html>

OpenAPI JSON: <http://localhost:8080/v3/api-docs>
