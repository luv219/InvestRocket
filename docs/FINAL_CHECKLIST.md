# Final Launch Checklist

## Code and CI

- [ ] Backend CI passes
- [ ] Frontend CI passes
- [ ] Backend tests pass with `mvnd test`
- [ ] Backend builds with `mvnd clean package`
- [ ] Frontend tests pass with `npm test`
- [ ] Frontend builds with `npm run build`
- [ ] No broken imports, TypeScript errors, or accidental debug logging

## Environment and Database

- [ ] Production environment variables are configured
- [ ] Neon production database and role are created
- [ ] JDBC URL contains `sslmode=require`
- [ ] Flyway migrations are current
- [ ] No `.env` files or real secrets are committed

## Hosting

- [ ] Backend is deployed and `/api/health` is healthy
- [ ] Frontend is deployed and direct SPA routes load
- [ ] `FRONTEND_URL` matches the deployed frontend origin
- [ ] `VITE_API_BASE_URL` targets the deployed API
- [ ] `VITE_WS_BASE_URL` uses `wss://`
- [ ] CORS and WebSocket connections work
- [ ] Admin bootstrap is disabled after administrator creation

## Portfolio Presentation

- [ ] Live demo links are added to README
- [ ] Screenshots are added under `docs/screenshots/`
- [ ] Demo walkthrough has been rehearsed
- [ ] README feature, architecture, testing, deployment, and security sections are current
- [ ] Repository description and topics are configured on GitHub
- [ ] License decision is recorded

## Product Verification

- [ ] Login and registration work in production
- [ ] Market, orders, portfolio, analytics, watchlist, alerts, notifications, journal, and settings work
- [ ] Admin pages work for ADMIN and reject USER
- [ ] Simulator disclaimer is visible
