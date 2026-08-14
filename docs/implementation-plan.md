# Remate Club implementation status

## Completed MVP foundation

1. Docker Compose monorepo runtime.
2. PostgreSQL schema managed by Flyway.
3. User authentication with BCrypt, JWT and rotating refresh tokens.
4. Public club and court discovery.
5. Owner club/court management with ownership authorization.
6. Admin approve/reject flow.
7. Transactional booking engine with pessimistic court locking.
8. Availability calculation, blocks, backend pricing and cancellation.
9. Responsive React UI for public, PLAYER, OWNER and ADMIN roles.
10. Club image upload, validation, cover selection and persistent local storage.
11. Backend unit/integration/concurrency tests and frontend component/routing tests.
12. Dev-only accounts and representative seed data.
13. Setup, API, architecture and screen-flow documentation.

## Logical next product iterations

1. Production object storage, responsive image variants and CDN delivery.
2. Opening hours and configurable slot duration per court.
3. Owner-created court blocks UI.
4. Email verification and password reset.
5. Payment/deposit flow and refund rules.
6. Notifications and calendar export.
7. Search filters for date, price, court type and distance.
8. Audit log and richer admin moderation.
9. Pagination/filtering on the backend for larger datasets.
10. Observability, rate limiting, backups and production deployment.

Business rules remain backend-controlled; Flyway remains the only schema-change mechanism and controllers return DTOs rather than JPA entities.
