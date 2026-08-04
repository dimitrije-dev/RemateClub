# Remate Club - Project Plan

## Project Goal

Remate Club is a full-stack web application for discovering padel clubs, viewing available courts and time slots, and creating court reservations.

The backend is the primary focus of the project. The architecture should be clean, modular, testable, and suitable for a university full-stack project.

---

## Project Identity

**Product name:** Remate Club  
**Domain:** Padel club discovery and court reservations  
**Primary users:** Players, club owners, administrators  
**UI language:** Serbian  
**Design style:** Modern, clean, premium, sports-oriented  

### Colors

| Token | Value | Usage |
|---|---|---|
| Primary dark navy | `#0E1A2B` | Navigation, footer, important sections |
| Secondary navy | `#1F2A3D` | Panels and secondary dark surfaces |
| Light background | `#E7EBEF` | Page background |
| Primary green | `#4FD09C` | Primary actions and availability |
| Light green | `#A7F3D0` | Positive badges and subtle accents |
| White | `#FFFFFF` | Cards and surfaces |
| Main text | `#0E1A2B` | Primary text |
| Muted text | `#64748B` | Secondary text |

---

## Notion Status Flow

- Backlog
- Ready
- In Progress
- Review
- Done
- Blocked

## Priorities

- P0 Must
- P1 Should
- P2 Later

---

# Roadmap

| Phase | Name | Goal | Status |
|---|---|---|---|
| Phase 0 | Planning | Scope, prerequisites, project boundaries | Ready |
| Phase 1 | Foundation MVP | Monorepo, Docker, backend/frontend init, auth | Ready |
| Phase 2 | Core Domain | Clubs, courts, ownership rules | Backlog |
| Phase 3 | Booking Engine | Availability, conflict prevention, transactions | Backlog |
| Phase 4 | Owner/Admin Flows | Club approval, court management, blocks | Backlog |
| Phase 5 | Frontend Product Pages | Public, player, owner, admin screens | Backlog |
| Phase 6 | Testing & Documentation | Tests, builds, README, architecture docs | Backlog |

---

# Phase 0 - Planning

## Goal

Define the exact scope of the first implementation cycle and prevent the project from growing in too many directions at once.

## Tasks

| Task | Output | Priority | Status |
|---|---|---|---|
| Confirm MVP scope | Defined first implementation boundary | P0 | Ready |
| Confirm local prerequisites | Java 21, Node, Docker, Maven | P0 | Ready |
| Define first milestone | Auth + project foundation | P0 | Ready |
| Decide refresh token strategy | DB-backed or revocation-ready model | P0 | Ready |
| Prepare implementation plan document | `docs/implementation-plan.md` | P0 | Ready |

---

# Phase 1 - Foundation MVP

## Goal

Create the stable base of the project: monorepo, Docker, backend, frontend, and authentication foundation.

## Tasks

| Task | Output | Depends On | Priority | Status |
|---|---|---|---|---|
| Inspect repository | Understand current repo state | None | P0 | Ready |
| Create monorepo structure | `backend/`, `frontend/`, `docs/` | Repo inspection | P0 | Ready |
| Add root files | `.gitignore`, `README.md`, `docker-compose.yml` | Structure | P0 | Ready |
| Add PostgreSQL Docker service | Local PostgreSQL container | Docker config | P0 | Ready |
| Initialize Spring Boot backend | Java 21, Maven, Spring Boot | Structure | P0 | Ready |
| Initialize React frontend | Vite, React, TypeScript | Structure | P0 | Ready |
| Add environment examples | Backend/frontend `.env.example` | Apps initialized | P0 | Ready |
| Add initial README setup | Local setup instructions | Root config | P0 | Ready |

## Acceptance Criteria

- [ ] Backend starts locally.
- [ ] Frontend starts locally.
- [ ] PostgreSQL starts through Docker Compose.
- [ ] README explains basic setup.
- [ ] Project structure matches planned monorepo layout.

---

# Phase 2 - Backend Auth Foundation

## Goal

Implement users, authentication, JWT security, and basic security infrastructure.

## Tasks

| Task | Output | Priority | Status |
|---|---|---|---|
| Create backend package structure | `auth`, `user`, `security`, `common` | P0 | Backlog |
| Add User entity | UUID, roles, status, timestamps | P0 | Backlog |
| Add UserRole enum | PLAYER, OWNER, ADMIN | P0 | Backlog |
| Add UserStatus enum | ACTIVE, SUSPENDED, DISABLED | P0 | Backlog |
| Add Flyway users migration | Production database schema | P0 | Backlog |
| Configure JPA validation | `ddl-auto=validate` | P0 | Backlog |
| Add register endpoint | `POST /api/auth/register` | P0 | Backlog |
| Add login endpoint | `POST /api/auth/login` | P0 | Backlog |
| Add refresh endpoint | `POST /api/auth/refresh` | P0 | Backlog |
| Add current user endpoint | `GET /api/auth/me` | P0 | Backlog |
| Add BCrypt password hashing | Secure password storage | P0 | Backlog |
| Add JWT access tokens | Stateless authentication | P0 | Backlog |
| Add refresh token model | Secure token renewal | P0 | Backlog |
| Add global exception handler | Consistent JSON errors | P0 | Backlog |
| Add OpenAPI/Swagger | API documentation | P1 | Backlog |

## Acceptance Criteria

- [ ] Passwords are hashed with BCrypt.
- [ ] Admin accounts cannot be created through public registration.
- [ ] Controllers do not return JPA entities.
- [ ] Validation errors use consistent JSON format.
- [ ] Unauthenticated requests return 401.
- [ ] Forbidden requests return 403.

---

# Phase 3 - Frontend Auth Foundation

## Goal

Create the frontend foundation, authentication state, routes, and first auth pages.

## Tasks

| Task | Output | Priority | Status |
|---|---|---|---|
| Add Tailwind CSS | Styling foundation | P0 | Backlog |
| Add design tokens | Remate Club colors and typography | P0 | Backlog |
| Create reusable Button component | Primary, secondary, tertiary, danger | P0 | Backlog |
| Create reusable Input component | Form input base | P0 | Backlog |
| Create Axios API client | Base URL, auth headers, error mapping | P0 | Backlog |
| Create AuthContext | Minimal auth state | P0 | Backlog |
| Add React Router setup | Public/protected route structure | P0 | Backlog |
| Build Login page | Serbian UI + validation | P0 | Backlog |
| Build Registration page | Serbian UI + validation | P0 | Backlog |
| Add ProtectedRoute | Auth-only access | P0 | Backlog |
| Add RoleProtectedRoute | Role-based access | P0 | Backlog |
| Add Unauthorized page | 403 frontend route | P1 | Backlog |
| Add Not Found page | 404 frontend route | P1 | Backlog |

## Acceptance Criteria

- [ ] Login and registration forms use React Hook Form + Zod.
- [ ] Protected pages redirect unauthenticated users.
- [ ] UI uses Remate Club design tokens.
- [ ] Initial interface text is Serbian.
- [ ] Sensitive long-lived tokens are not stored in localStorage.

---

# Phase 4 - Core Club And Court Domain

## Goal

Implement clubs, courts, and ownership rules.

## Tasks

| Task | Output | Priority | Status |
|---|---|---|---|
| Add Club entity | Club model with owner and status | P0 | Backlog |
| Add ClubStatus enum | PENDING_APPROVAL, APPROVED, REJECTED, SUSPENDED | P0 | Backlog |
| Add Club migration | Club table, indexes, foreign keys | P0 | Backlog |
| Add Court entity | Court model with type and active flag | P0 | Backlog |
| Add CourtType enum | STANDARD, PANORAMIC, SINGLE | P0 | Backlog |
| Add Court migration | Court table, indexes, foreign keys | P0 | Backlog |
| Add public clubs endpoint | `GET /api/clubs` | P0 | Backlog |
| Add club details endpoint | `GET /api/clubs/{clubId}` | P0 | Backlog |
| Add public courts endpoint | `GET /api/clubs/{clubId}/courts` | P0 | Backlog |
| Add owner club creation | `POST /api/owner/clubs` | P0 | Backlog |
| Add owner club update | `PUT /api/owner/clubs/{clubId}` | P0 | Backlog |
| Add owner clubs endpoint | `GET /api/owner/clubs` | P0 | Backlog |
| Add admin approve endpoint | `PATCH /api/admin/clubs/{clubId}/approve` | P0 | Backlog |
| Add admin reject endpoint | `PATCH /api/admin/clubs/{clubId}/reject` | P0 | Backlog |
| Add ownership authorization tests | Owner cannot manage other owners' clubs | P0 | Backlog |

---

# Phase 5 - Booking Engine

## Goal

Implement the most important part of the system: availability, reservations, and conflict prevention.

## Booking Conflict Rule

A new booking overlaps an existing booking when:

```text
existing.startAt < requested.endAt
AND
existing.endAt > requested.startAt
```

## Tasks

| Task | Output | Priority | Status |
|---|---|---|---|
| Add Booking entity | Booking model with status and price | P0 | Backlog |
| Add BookingStatus enum | CONFIRMED, COMPLETED, CANCELLED states | P0 | Backlog |
| Add CourtBlock entity | Blocked court periods | P0 | Backlog |
| Add booking migration | Booking table, indexes, foreign keys | P0 | Backlog |
| Add court block migration | Court block table, indexes, foreign keys | P0 | Backlog |
| Implement overlap query | Active booking conflict detection | P0 | Backlog |
| Implement booking transaction | Safe booking creation | P0 | Backlog |
| Add pessimistic locking | Prevent race conditions | P0 | Backlog |
| Check overlapping court blocks | Court unavailable during blocks | P0 | Backlog |
| Calculate price on backend | Do not trust frontend price | P0 | Backlog |
| Add booking create endpoint | `POST /api/bookings` | P0 | Backlog |
| Add my bookings endpoint | `GET /api/bookings/me` | P0 | Backlog |
| Add booking details endpoint | `GET /api/bookings/{bookingId}` | P0 | Backlog |
| Add cancel booking endpoint | `PATCH /api/bookings/{bookingId}/cancel` | P0 | Backlog |
| Add availability endpoint | `GET /api/courts/{courtId}/availability` | P0 | Backlog |
| Add conflict response | `409 BOOKING_TIME_UNAVAILABLE` | P0 | Backlog |
| Add concurrency integration test | Two competing bookings, only one succeeds | P0 | Backlog |

## Acceptance Criteria

- [ ] Booking is created inside a database transaction.
- [ ] Court must be active.
- [ ] Club must be approved.
- [ ] Requested time must be in the future.
- [ ] Overlapping bookings are rejected.
- [ ] Overlapping court blocks are rejected.
- [ ] Frontend availability is never trusted as final truth.
- [ ] Conflict returns HTTP 409.

---

# Phase 6 - Frontend Product Pages

## Goal

Build user-facing pages for public, player, owner, and admin flows.

## Public Pages

| Task | Output | Priority | Status |
|---|---|---|---|
| Build landing page | Hero, search, benefits, CTA, footer | P1 | Backlog |
| Build clubs listing page | Filters, club cards, pagination | P1 | Backlog |
| Build club details page | Club info, courts, slots, booking CTA | P1 | Backlog |
| Add loading states | Skeletons/spinners | P1 | Backlog |
| Add empty states | No results UX | P1 | Backlog |
| Add error states | API failure UX | P1 | Backlog |

## Player Pages

| Task | Output | Priority | Status |
|---|---|---|---|
| Build My Bookings page | Player reservations | P1 | Backlog |
| Build Booking Details page | Reservation details | P1 | Backlog |
| Build Profile page | Current user info | P1 | Backlog |

## Owner Pages

| Task | Output | Priority | Status |
|---|---|---|---|
| Build Owner Dashboard | Owner overview | P1 | Backlog |
| Build My Clubs page | Owner club list | P1 | Backlog |
| Build Club Editor page | Create/edit club form | P1 | Backlog |
| Build Courts Management page | Manage courts | P1 | Backlog |
| Build Reservations Calendar placeholder | Owner calendar base | P1 | Backlog |

## Admin Pages

| Task | Output | Priority | Status |
|---|---|---|---|
| Build Pending Clubs page | Approve/reject clubs | P1 | Backlog |

---

# Phase 7 - Testing And Documentation

## Backend Tests

| Task | Output | Priority | Status |
|---|---|---|---|
| Add authentication service tests | Register/login logic tested | P0 | Backlog |
| Add booking overlap unit tests | Conflict logic tested | P0 | Backlog |
| Add club ownership tests | Owner authorization tested | P0 | Backlog |
| Add controller integration tests | Main API endpoints tested | P1 | Backlog |
| Add Testcontainers PostgreSQL config | Real DB integration tests | P0 | Backlog |
| Add booking concurrency test | Race condition protection tested | P0 | Backlog |

## Frontend Tests

| Task | Output | Priority | Status |
|---|---|---|---|
| Add login form validation test | Login validation tested | P1 | Backlog |
| Add protected route test | Auth routing tested | P1 | Backlog |
| Add club card rendering test | Club UI component tested | P1 | Backlog |
| Add booking form validation test | Booking validation tested | P1 | Backlog |

## Documentation

| Task | Output | Priority | Status |
|---|---|---|---|
| Complete root README | Setup, Docker, env, accounts | P0 | Backlog |
| Add backend setup docs | Backend run/test instructions | P0 | Backlog |
| Add frontend setup docs | Frontend run/test instructions | P0 | Backlog |
| Add Docker instructions | Compose usage | P0 | Backlog |
| Add environment docs | Required env variables | P0 | Backlog |
| Add endpoint table | Implemented APIs | P0 | Backlog |
| Add architecture document | `docs/architecture.md` | P0 | Backlog |
| Add default dev accounts | Admin, owner, player | P1 | Backlog |

---

# First Implementation Cycle

## Scope

The first cycle should be limited to foundation and authentication. Do not implement clubs, courts, booking engine, or admin flow yet.

## Ordered Checklist

- [ ] Inspect complete repository
- [ ] Create or confirm monorepo structure
- [ ] Add root `.gitignore`
- [ ] Add root `README.md`
- [ ] Add root `docker-compose.yml`
- [ ] Add PostgreSQL Docker service
- [ ] Initialize Spring Boot backend
- [ ] Initialize React frontend
- [ ] Add backend `.env.example`
- [ ] Add frontend `.env.example`
- [ ] Add frontend design tokens
- [ ] Add User entity
- [ ] Add UserRole enum
- [ ] Add UserStatus enum
- [ ] Add Flyway users migration
- [ ] Configure Spring Security
- [ ] Add JWT access token generation
- [ ] Add refresh token strategy
- [ ] Add register endpoint
- [ ] Add login endpoint
- [ ] Add refresh endpoint
- [ ] Add `/api/auth/me`
- [ ] Add global exception handler
- [ ] Add login page
- [ ] Add registration page
- [ ] Add protected routes
- [ ] Add initial backend tests
- [ ] Add initial frontend tests
- [ ] Run backend tests
- [ ] Run frontend tests
- [ ] Run frontend type checking
- [ ] Run production builds
- [ ] Report executed commands and failures

---

# Do Not Do In First Cycle

- [ ] Do not implement the full booking engine.
- [ ] Do not implement all frontend pages.
- [ ] Do not add payments.
- [ ] Do not add email verification flow.
- [ ] Do not create admin accounts through public registration.
- [ ] Do not rely on Hibernate to create production tables.
- [ ] Do not return JPA entities from controllers.

---

# Potential Blockers

| Blocker | Why It Matters | Status |
|---|---|---|
| Exact Spring Boot 3.5.x version | Needed for project initialization | Open |
| React 19 package compatibility | Some libraries may warn about peer deps | Open |
| Refresh token strategy | Affects DB and auth flow | Open |
| Price calculation model missing | Needed before real bookings | Open |
| Club images not specified | Use placeholders initially | Open |
| Email verification not scoped | Field exists, flow can wait | Open |
| Payments not included | Booking is reservation-only for MVP | Open |

---

# Technical Decisions

| Decision | Choice | Reason |
|---|---|---|
| Architecture | Feature-based packages | Cleaner modular backend |
| Backend schema | Flyway migrations | Production-style DB management |
| Hibernate DDL | `validate` | Prevent accidental schema creation |
| Auth | Stateless JWT | Suitable for API/frontend split |
| Password hashing | BCrypt | Standard secure hashing |
| Frontend state | TanStack Query + AuthContext | Avoid unnecessary Redux |
| DTOs | Required at API boundaries | Do not expose JPA entities |
| Booking safety | Transaction + locking + overlap checks | Prevent double booking |

---

# Definition Of Done

A task is done only when:

- [ ] Code compiles.
- [ ] Relevant tests pass.
- [ ] API does not expose JPA entities.
- [ ] Security rules are enforced.
- [ ] Errors return consistent JSON responses.
- [ ] README/docs are updated if setup or API changed.
- [ ] No placeholder business logic exists in critical backend paths.
- [ ] Commands and failures are reported clearly.

---

# Important Backend Rules

- Keep controllers thin.
- Put business rules in services.
- Use transactions for state-changing operations.
- Use DTOs for request and response objects.
- Never return password hashes.
- Never log passwords or tokens.
- Never trust user-provided ownership identifiers.
- Derive the authenticated user from Spring Security.
- Backend validation is the source of truth.

---

# Important Frontend Rules

- Use Serbian text in the initial UI.
- Use Remate Club colors and Inter typography.
- Use TanStack Query for server state.
- Use React Hook Form and Zod for forms.
- Use Axios instance for API calls.
- Use loading, empty, and error states.
- Retrieve fresh availability before submitting a booking.
- Do not use Redux unless truly needed later.
