# Deployment

## Recommended Production Topology

```text
Vercel / Netlify / nginx
  ↓ HTTPS and WSS
Render / Railway / Docker VPS
  ↓ JDBC TLS
Neon PostgreSQL
```

- Frontend: Vercel, Netlify, or the included nginx image
- Backend: Render, Railway, or a Docker-based VPS
- Database: Neon PostgreSQL

## Release Verification

Local backend commands always use Maven Daemon:

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

GitHub Actions uses the standard Maven executable provided by the hosted runner. This CI-only choice does not change the local `mvnd` requirement.

## Backend Environment

Required production values:

- `SPRING_PROFILES_ACTIVE=prod`
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `FRONTEND_URL`

The hosting platform normally supplies `PORT`; Invest Rocket binds to it automatically and falls back to `SERVER_PORT` or `8080`.

Optional values include JWT expiration, financial-provider configuration, scheduler controls, and initial administrator settings documented in `ENVIRONMENT_VARIABLES.md`.

## Render Backend

Use the Docker deployment path so the checked-in backend image defines the Java runtime and startup command.

1. Create a Render Web Service from the GitHub repository.
2. Select Docker as the runtime.
3. Set the root directory to `backend`.
4. Use `backend/Dockerfile`.
5. Add all required backend environment variables.
6. Set the health check path to `/api/health`.
7. Deploy and confirm both `/api/health` and `/actuator/health`.

Render supplies `PORT`; no hardcoded production port is required.

## Railway Backend

1. Create a Railway project and connect the GitHub repository.
2. Add a service using `backend/Dockerfile`.
3. Set the service root directory to `backend` if Railway requests one.
4. Add required backend environment variables.
5. Configure `/api/health` as the health check.
6. Generate a public HTTPS domain.
7. Verify WebSocket upgrade support on `/ws`.

Railway also supplies `PORT`, which the backend reads automatically.

## Docker VPS Backend

Build from the repository root:

```bash
docker build -t invest-rocket-backend ./backend
```

Run with an environment file stored outside the repository:

```bash
docker run --env-file /secure/path/invest-rocket.env -p 8080:8080 invest-rocket-backend
```

Place Caddy, nginx, Traefik, or another reverse proxy in front of the container for HTTPS, WSS, and domain routing. Never bake credentials into the image.

## Vercel Frontend

1. Import the GitHub repository.
2. Set the root directory to `frontend`.
3. Set the framework preset to Vite.
4. Use build command `npm run build`.
5. Use output directory `dist`.
6. Add `VITE_API_BASE_URL=https://your-backend-domain.com/api`.
7. Add `VITE_WS_BASE_URL=wss://your-backend-domain.com/ws`.
8. Redeploy whenever either public URL changes.

Configure an SPA rewrite to `index.html` if the platform does not detect React Router automatically.

## Netlify Frontend

1. Import the GitHub repository.
2. Set base directory to `frontend`.
3. Use build command `npm run build`.
4. Use publish directory `dist`.
5. Add the two production Vite environment variables.
6. Configure the SPA fallback:

```text
/* /index.html 200
```

The fallback ensures direct navigation to routes such as `/dashboard` or `/market/AAPL` loads the React application.

## Frontend Docker or Static Hosting

```bash
docker build \
  --build-arg VITE_API_BASE_URL=https://your-backend-domain.com/api \
  --build-arg VITE_WS_BASE_URL=wss://your-backend-domain.com/ws \
  -t invest-rocket-frontend ./frontend
```

```bash
docker run -p 80:80 invest-rocket-frontend
```

Vite variables are public build-time configuration. They must contain URLs only, never secrets.

## Neon Production Database

1. Create a Neon project and production database.
2. Create or select the application role.
3. Copy the host, database name, username, and password.
4. Set `DATABASE_URL` in JDBC form:

```text
jdbc:postgresql://your-neon-hostname.neon.tech/your-db-name?sslmode=require
```

5. Set `DATABASE_USERNAME` and `DATABASE_PASSWORD` separately.
6. Never commit Neon credentials or embed the password in checked-in URLs.

Flyway runs automatically during backend startup. Review backend startup logs for migration success and inspect Neon's `flyway_schema_history` table to confirm applied versions. Test migrations against a Neon branch before production. Connection pooling can be added later if traffic requires it.

## CORS and WebSockets

Backend production:

```text
FRONTEND_URL=https://your-frontend-domain.com
```

Frontend production:

```text
VITE_API_BASE_URL=https://your-backend-domain.com/api
VITE_WS_BASE_URL=wss://your-backend-domain.com/ws
```

`FRONTEND_URL` must be one exact origin without a path or wildcard. The same value controls REST CORS and the `/ws` STOMP handshake. HTTPS frontends must use `wss://`, not `ws://`. Reverse proxies must preserve WebSocket upgrade headers.

## Admin Bootstrap

1. Temporarily set `ADMIN_BOOTSTRAP_ENABLED=true`.
2. Set a unique `ADMIN_EMAIL`, strong `ADMIN_PASSWORD`, and display name.
3. Deploy once and verify administrator access.
4. Set `ADMIN_BOOTSTRAP_ENABLED=false`.
5. Redeploy and confirm bootstrap remains disabled.

An existing account is never silently promoted.

## Post-Deployment Verification

1. Confirm `/api/health` and `/actuator/health`.
2. Register and log in from the deployed frontend.
3. Confirm CORS permits only the configured frontend.
4. Confirm the `/ws` connection upgrades over WSS.
5. Test market search, quote loading, orders, portfolio, analytics, watchlist, alerts, notifications, and journal.
6. Test administrator pages with an ADMIN account.
7. Confirm a USER receives `403` from `/api/admin/**`.
8. Confirm Flyway migrations are current.
9. Confirm logs contain no secrets or stack traces returned to clients.
10. Confirm admin bootstrap is disabled.
