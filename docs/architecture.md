# Remate Club Architecture

## Overview

Remate Club is a full-stack application for padel club discovery and court reservations. The backend is the primary system of record and must enforce all security and booking rules.

## Monorepo Layout

```text
backend/   Spring Boot API
frontend/  React Vite client
docs/      Architecture and implementation notes
assets/    Planning and Notion import artifacts
```

## Backend Direction

Base package:

```text
com.remateclub
```

Feature-oriented modules:

```text
auth
user
club
court
booking
availability
security
common
```

Controllers should stay thin. Business rules belong in services. REST controllers must use DTOs and must not expose JPA entities directly.

## Authentication Flow

Planned flow:

1. User registers as `PLAYER` or `OWNER`.
2. Backend hashes password with BCrypt.
3. User logs in with email and password.
4. Backend returns a short-lived access token and a refresh token.
5. Frontend keeps access token in memory where practical.
6. Refresh tokens use a secure revocation-ready strategy.
7. `/api/auth/me` returns the authenticated user DTO.

## Booking Creation Flow

Planned booking flow:

1. Validate request.
2. Derive authenticated user from Spring Security.
3. Validate court is active.
4. Validate club is approved.
5. Validate requested time is in the future.
6. Lock the court or reservation scope.
7. Check overlapping active bookings.
8. Check overlapping court blocks.
9. Calculate price on the backend.
10. Create booking only if the slot is still available.

## Booking Conflict Rule

```text
existing.startAt < requested.endAt
AND
existing.endAt > requested.startAt
```

## Role Permissions

| Role | Permissions |
|---|---|
| Public | View approved clubs and active courts |
| PLAYER | Manage only own reservations |
| OWNER | Manage only owned clubs and courts |
| ADMIN | Manage all resources and approve/suspend clubs |

