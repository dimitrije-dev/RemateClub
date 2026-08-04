# Remate Club Implementation Plan

## Current Scope

This repository is initialized for the first development cycle. The current goal is to create a push-ready monorepo foundation, not to implement the full business domain.

## First Implementation Cycle

Focus only on:

1. Monorepo structure.
2. Docker Compose and PostgreSQL.
3. Spring Boot backend initialization.
4. React frontend initialization.
5. Shared frontend design tokens.
6. User entity and roles.
7. Authentication endpoints.
8. JWT security configuration.
9. Login and registration pages.
10. Protected routes.
11. Initial automated tests.
12. README setup instructions.

## Do Not Implement Yet

- Full booking engine.
- All frontend pages.
- Payments.
- Email verification flow.
- Public admin registration.
- Hibernate production table generation.
- Returning JPA entities from controllers.

## Next Technical Steps

1. Install or configure local Maven, or use Docker for backend builds.
2. Confirm local Java 21 availability for backend development.
3. Run `docker compose up --build` once dependencies can be downloaded.
4. Implement the `User` entity and Flyway migration.
5. Implement register/login/refresh/me endpoints.
6. Add backend tests around registration and login.
7. Replace frontend auth placeholders with real React Hook Form + Zod forms.

## Notes

- The backend targets Java 21 even if a newer JDK is installed locally.
- Hibernate is configured with `ddl-auto=validate`.
- Flyway owns production schema changes.
- The frontend uses Serbian user-facing copy.

