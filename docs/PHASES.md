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

## Phase 5 — Watchlist and Live Price Updates

- Per-user watchlist management
- JWT-protected add, list, and remove endpoints
- Spring WebSocket/STOMP broker
- In-memory mock live-price generation
- Live watchlist and stock-detail updates

Status: complete. The stream is a development/demo simulation and is not exchange-grade real-time data. Redis pub/sub remains deferred.

## Phase 6 — Analytics

- Portfolio valuation history and returns
- Performance history
- Recharts visualizations
- Dashboard analytics

Status: complete. Analytics are calculated by the backend from virtual wallet, holdings, orders, trades, snapshots, and configured market data.

## Phase 7 — Profile, Risk Controls, and Audit Logs

- User profile and password management
- Simulator reset with exact confirmation
- Per-user order value and daily limits
- Limit and stop-loss permissions
- Account activity and audit history

Status: complete. Risk controls are enforced by the backend, and simulator reset affects virtual trading state without deleting the user or historical executions.

## Phase 8 — Admin Dashboard and Platform Monitoring

- ADMIN role and method-level authorization
- Disabled-by-default initial admin bootstrap
- User management and self-lockout prevention
- Platform trading and portfolio statistics
- System, scheduler, database, and provider monitoring
- Admin audit visibility

Status: complete. Backend authorization is authoritative; frontend role checks provide navigation and UX only.

## Phase 9 — Notifications, Price Alerts, and Trading Journal

- In-app notification center and unread tracking
- Order, reset, and triggered-alert notifications
- Scheduled ABOVE/BELOW price alerts
- Private journal entries with optional order/trade links
- Protected frontend pages and navigation

Status: complete. Notifications are in-app only, alerts use configured simulator market data, and all records are scoped to the authenticated user.

## Phase 10 — Testing, Hardening, and Deployment Preparation

- Expanded backend regression and ownership tests
- Vitest and React Testing Library frontend suite
- Production profile and sanitized error handling
- Backend/frontend Docker images and Neon-based compose
- Deployment, testing, and security runbooks
- Portfolio-ready final documentation

Status: complete. Backend workflows use `mvnd`; a local PostgreSQL server is never required.
