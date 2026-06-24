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
- Rate-limit handling and resilient API errors
- Protected market and stock-detail pages
- Disabled buy and sell placeholders

Status: complete. The mock provider is the default; Finnhub can be selected with backend environment variables.

Watchlists remain deferred.

## Phase 3 — Simulated Trading

- Portfolios and positions
- Buy and sell order validation
- Transaction-safe simulated execution
- Trade history and virtual cash settlement

Status: complete. Phase 3 supports whole-share `MARKET` orders only, weighted-average holdings, realized sell profit or loss, portfolio valuation, and order/trade history.

## Phase 4 — Advanced Order Types

- Limit buy and sell orders
- Stop-loss sell orders
- Pending lifecycle and cancellation
- Reserved cash and locked share quantities
- Scheduled trigger processing

Status: complete.

## Phase 5 — Analytics

- Portfolio valuation history and returns
- Performance history
- Recharts visualizations
- Dashboard analytics

## Phase 6 — Hardening and Delivery

- Redis quote caching
- Docker support
- Broader automated testing
- Observability, security review, CI/CD, and deployment

Backend workflows use `mvnd`; a local PostgreSQL server is never required.
