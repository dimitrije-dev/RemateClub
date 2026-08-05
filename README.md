# Remate Club

Remate Club is a full-stack web application for discovering padel clubs, viewing available courts and time slots, and creating court reservations.

This repository is initialized as a monorepo:

```text
remate-club/
  backend/
  frontend/
  assets/
  docker-compose.yml
  README.md
```

## Current Status

The project foundation is initialized so it can be committed and pushed to Git. The first real implementation cycle should focus on authentication and the user model before moving into clubs, courts, and bookings.

## Tech Stack

Backend:

- Java 21 target
- Spring Boot 3.5.x
- Maven
- PostgreSQL
- Flyway
- Spring Security

Frontend:

- React 19
- Vite
- TypeScript
- React Router
- TanStack Query
- Axios
- Tailwind CSS

Infrastructure:

- Docker Compose
- PostgreSQL container
- Backend container
- Frontend container

## Local Prerequisites

- Docker Desktop
- Node.js 20+ or 22+
- Java 21 for local backend development
- Maven for local backend commands

Note: this machine currently has Java 26 and Node 24 available, but Maven is not installed. Docker can still build the backend using the Maven image.

## Environment

Copy the example environment file when you start local development:

```bash
cp .env.example .env
```

Do not commit real secrets.

## Run With Docker

```bash
docker compose up --build
```

Services:

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432

## Backend

```bash
cd backend
mvn spring-boot:run
```

Health endpoint:

```bash
curl http://localhost:8080/api/health
```

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend dev server:

```text
http://localhost:5173
```

## Next Implementation Cycle

The next implementation cycle focuses on authentication and the user model before moving into clubs, courts, and bookings.
