# Remate Club - Run Commands

## 1. Start PostgreSQL

Use this if port `5432` is free:

```bash
cd /Users/dimi/Dev/RemateClub
docker compose up postgres
```

If port `5432` is already allocated, use port `5433`:

```bash
cd /Users/dimi/Dev/RemateClub
docker compose down
POSTGRES_PORT=5433 docker compose up postgres
```

## 2. Start Backend

If PostgreSQL is running on default port `5432`:

```bash
cd /Users/dimi/Dev/RemateClub/backend
JAVA_HOME=/Users/dimi/Library/Java/JavaVirtualMachines/jbr-21.0.9/Contents/Home mvn spring-boot:run
```

If PostgreSQL is running on port `5433`:

```bash
cd /Users/dimi/Dev/RemateClub/backend
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/remate_club JAVA_HOME=/Users/dimi/Library/Java/JavaVirtualMachines/jbr-21.0.9/Contents/Home mvn spring-boot:run
```

## 3. Check Backend Health

```bash
curl http://localhost:8080/api/health
```

Expected result:

```json
{
  "status": "UP",
  "service": "remate-club-backend",
  "timestamp": "..."
}
```

## 4. Start Frontend

```bash
cd /Users/dimi/Dev/RemateClub/frontend
npm run dev
```

Open:

```text
http://localhost:5173
```

## 5. Run Checks

Backend compile:

```bash
cd /Users/dimi/Dev/RemateClub/backend
JAVA_HOME=/Users/dimi/Library/Java/JavaVirtualMachines/jbr-21.0.9/Contents/Home mvn -q -DskipTests compile
```

Frontend typecheck:

```bash
cd /Users/dimi/Dev/RemateClub/frontend
npm run typecheck
```

Frontend production build:

```bash
cd /Users/dimi/Dev/RemateClub/frontend
npm run build
```

## 6. Docker Full Stack

Use this when all ports are free:

```bash
cd /Users/dimi/Dev/RemateClub
docker compose up --build
```

If `5432` is taken:

```bash
cd /Users/dimi/Dev/RemateClub
POSTGRES_PORT=5433 docker compose up --build
```

## 7. Useful Docker Commands

Stop project containers:

```bash
cd /Users/dimi/Dev/RemateClub
docker compose down
```

Show running containers:

```bash
docker ps
```

Show logs:

```bash
cd /Users/dimi/Dev/RemateClub
docker compose logs -f
```

## Notes

- The project backend targets Java 21.
- The local Java 21 path is:

```text
/Users/dimi/Library/Java/JavaVirtualMachines/jbr-21.0.9/Contents/Home
```

- If port `5432` is already allocated, use `POSTGRES_PORT=5433`.
- The current app is still a foundation skeleton, so the main backend check is `/api/health`.
