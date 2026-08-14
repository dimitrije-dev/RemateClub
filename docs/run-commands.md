# Remate Club run commands

## Full stack

```bash
cp .env.example .env
docker compose up -d --build
```

Ako su podrazumevani portovi zauzeti:

```bash
FRONTEND_PORT=5174 POSTGRES_PORT=5433 docker compose up -d --build
```

Provera:

```bash
curl http://localhost:8080/api/health
docker compose ps
docker compose logs -f
```

Gašenje:

```bash
docker compose down
```

## Backend bez backend kontejnera

```bash
docker compose up -d postgres
cd backend
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

Za PostgreSQL na host portu `5433` dodati:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/remate_club
```

Testovi:

```bash
cd backend
mvn test
```

## Frontend bez frontend kontejnera

```bash
cd frontend
npm ci
npm run dev
```

Provere:

```bash
npm test -- --run
npm run typecheck
npm run build
```

Razvojni nalozi i detaljan walkthrough su u root [README-u](../README.md).
