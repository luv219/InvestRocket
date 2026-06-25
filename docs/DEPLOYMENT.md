# Deployment

## Recommended Topology

- Frontend: Vercel, Netlify, or the included nginx image
- Backend: Render, Railway, or a Docker-based VPS
- Database: Neon PostgreSQL

## Backend Environment

Required:

- `DATABASE_URL` using JDBC and `sslmode=require`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `FRONTEND_URL`

Optional/configurable values include JWT expiration, financial provider settings, scheduler enable/interval values, and initial administrator values from `.env.example`.

Run with the `prod` Spring profile. It validates the Flyway schema, suppresses detailed errors, uses graceful shutdown, and exposes only Actuator health.

## Frontend Environment

- `VITE_API_BASE_URL=https://api.example.com/api`
- `VITE_WS_BASE_URL=wss://api.example.com/ws`

Vite values are compiled into the static frontend and must never contain secrets.

## Neon Setup

1. Create a Neon project and deployment branch.
2. Build `DATABASE_URL` as `jdbc:postgresql://...?...sslmode=require`.
3. Deploy the backend so Flyway applies pending migrations.
4. Verify `/actuator/health` before routing frontend traffic.

No local PostgreSQL service is included in `docker-compose.yml`.

## CORS and WebSockets

Set `FRONTEND_URL` to one exact origin, such as `https://invest-rocket.example`. The same origin controls REST CORS and the STOMP handshake. Production proxies must support WebSocket upgrades and route `/ws` to the backend.

## Admin Bootstrap

1. Temporarily set `ADMIN_BOOTSTRAP_ENABLED=true`.
2. Provide a unique email and strong password.
3. Start the backend once and verify the administrator.
4. Set `ADMIN_BOOTSTRAP_ENABLED=false` and redeploy.

An existing email is never automatically promoted.

## Verification

```bash
cd backend
mvnd test
mvnd clean package
```

```bash
cd frontend
npm install
npm test
npm run build
```

## Docker

From the repository root, supply the root environment variables and run:

```bash
docker compose up --build
```

The frontend is exposed on port `80`, the backend on port `8080`, and Neon remains external.

## Release Checklist

- Automated tests and production builds pass
- Neon migration is verified on staging
- HTTPS/WSS and exact CORS origin are configured
- Secrets are outside Git and container images
- Admin bootstrap is disabled
- Market provider and scheduler intervals match quotas/capacity
- Health checks and logs are monitored
