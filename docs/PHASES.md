# Delivery Phases

Each phase must preserve the core constraint: Invest Rocket is a virtual simulator and does not provide financial advice or real-money trading.

## Phase 0 — Foundation

- Monorepo structure and documentation
- Spring Boot and React foundations
- Neon PostgreSQL environment configuration
- Health endpoint and responsive UI shell
- No entities, authentication, or trading logic

Status: complete.

## Phase 1 — Identity and Wallet

- User registration and login
- Secure password hashing and JWT authentication
- Virtual wallet and configurable starting balance
- User and wallet persistence
- Protected frontend dashboard and session restoration

Status: complete. JWTs are stored in `localStorage` for development; production hardening should evaluate `httpOnly` cookies.

## Phase 2 — Market Discovery

- Financial API provider abstraction
- Stock symbol search and quote retrieval
- Watchlists
- Rate-limit handling and resilient API errors

## Phase 3 — Simulated Trading

- Portfolios and positions
- Buy and sell order validation
- Transaction-safe simulated execution
- Trade history and virtual cash settlement

## Phase 4 — Analytics

- Portfolio valuation and returns
- Performance history
- Recharts visualizations
- Dashboard summaries

## Phase 5 — Hardening and Delivery

- Redis quote caching
- Docker support
- Broader automated testing
- Observability, security review, CI/CD, and deployment

Backend workflows use `mvnd`; a local PostgreSQL server is never required.
