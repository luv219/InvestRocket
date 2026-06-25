# Testing

## Backend

The backend uses JUnit 5, Mockito, Spring Boot Test, Spring Security Test, and MockMvc.

Coverage includes authentication, response secrecy, market data, trading calculations, advanced orders, ownership checks, portfolio analytics, watchlists, profile and risk controls, notifications, alerts, journal entries, and administrator authorization.

```bash
cd backend
mvnd test
```

```bash
cd backend
mvnd clean package
```

## Frontend

The frontend uses Vitest, React Testing Library, jest-dom, user-event, and jsdom.

Coverage includes landing/auth pages, protected and administrator redirects, role-aware navigation, bearer-token attachment, and authentication/market service contracts.

```bash
cd frontend
npm test
```

```bash
cd frontend
npm run build
```

## Manual Release Checklist

1. Register and log in as a USER.
2. Verify market search, virtual orders, portfolio, analytics, watchlist, alerts, notifications, journal, activity, and settings.
3. Log out and confirm protected routes redirect to login.
4. Log in as an ADMIN and verify administration pages.
5. Confirm USER requests to `/api/admin/**` receive `403`.
6. Confirm unauthenticated protected API requests receive `401`.
7. Confirm `/api/health` and `/actuator/health`.
8. Apply Flyway migrations to a staging Neon branch before production.

## Known Limitations

- Automated tests use mocks and MVC slices rather than a live Neon test database.
- WebSocket behavior is service-tested; browser end-to-end tests are not included.
- External provider quotas and Docker execution require environment-specific verification.
- `npm audit --omit=dev` reports zero production vulnerabilities. The full audit currently reports one low-severity Windows development-server advisory in Vite's `esbuild` dependency.
