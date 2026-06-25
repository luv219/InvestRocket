# Architecture

## Final System Overview

```text
React + TypeScript + Tailwind + Recharts
  ├── Axios REST/JWT
  └── STOMP/WebSocket demo prices
             ↓
Spring Boot 3 / Java 21
  ├── Security, validation, domain services, and admin monitoring
  ├── MarketDataProvider abstraction
  ├── Scheduled order, alert, snapshot, and price processors
  └── Flyway + Spring Data JPA
             ↓
Neon PostgreSQL over TLS
```

## System Context

```text
Browser
  │
  ▼
React + TypeScript frontend
  │ REST/JSON
  ▼
Spring Boot backend
  ├── Neon PostgreSQL
  ├── Mock or Finnhub market-data provider
  └── Redis cache (later)
```

Invest Rocket is a simulator. No component will connect to a brokerage or execute real-money transactions.

## Monorepo Boundaries

- `frontend/`: browser UI, routing, feature views, and future API client
- `backend/`: REST API, security, application services, and persistence
- `docs/`: product and technical decisions
- `docker/`: future container and local orchestration files

## Backend Modules

- `auth`, `user`, `wallet`: identity and virtual funding in Phase 1
- `marketdata`: provider abstraction, search, and quote retrieval in Phase 2
- `watchlist`: authenticated per-user symbol tracking
- `websocket`: live demo price generation and STOMP broadcasting
- `portfolio`, `order`, `trade`: simulated execution and positions in Phase 3
- `analytics`: portfolio calculations, snapshots, and trading statistics
- `config`, `common`, `exception`: cross-cutting infrastructure

## Market Data Flow

```text
React market pages
  ↓ authenticated REST
MarketDataController
  ↓
MarketDataService
  ↓
MarketDataProvider
  ├── MockMarketDataProvider
  └── FinnhubMarketDataProvider
```

`MarketDataProvider` keeps controllers and services independent from a financial vendor. The mock provider is the default development path and needs no API key. Setting `FINANCIAL_API_PROVIDER=finnhub` selects the Finnhub implementation.

Financial API keys are backend environment variables only. The frontend always calls Invest Rocket’s authenticated API and never contacts Finnhub directly.

## Trading Engine Flow

```text
Frontend market order form
  ↓ authenticated request without trusted price
OrderController
  ↓
OrderService
  ↓ fetch latest quote
MarketDataService
  ↓ transactional update
Wallet + Holding + Order + Trade
  ↓ read model
PortfolioService
```

The backend always selects the execution price from `MarketDataService`. It never accepts a client-supplied execution price. Wallet and matching holding rows are locked during execution, and the wallet, holding, order, and trade changes commit or roll back together.

All executions are virtual simulations. Phase 3 supports only immediately executed whole-share market orders.

## Advanced Order Flow

```text
Frontend advanced order form
  ↓
OrderController
  ↓
OrderService
  ↓ latest backend quote
MarketDataService
  ↓
Immediate execution OR pending order with reservation
  ↓ every configured interval
PendingOrderProcessor
  ↓ trigger matched
Transactional Wallet + Holding + Order + Trade update
```

The processor re-locks and re-checks each order’s status before execution, preventing duplicate trades when runs overlap. Market and trigger prices are evaluated on the backend. Pending limit buys reserve virtual cash; pending limit and stop-loss sells lock shares. Cancellation reverses the reservation.

This remains simulated execution, not brokerage routing or real-money trading.

## Live Price Flow

```text
MockLivePriceGenerator
  ↓
LivePriceService (in-memory latest-price map)
  ↓
PriceBroadcastService
  ↓
Spring WebSocket/STOMP simple broker
  ↓
Frontend livePriceClient and useLivePrices
  ↓
Watchlist and Stock Detail UI
```

The mock stream is explicitly for development and demonstrations. Financial provider credentials remain backend-only, and browsers subscribe to Invest Rocket rather than directly to a provider. `MarketDataService` overlays the latest generated price, so pending-order checks and REST quotes use the same in-memory demo value.

The simple broker supports the current single-instance architecture. Redis pub/sub may be added later for horizontally scaled deployments.

## Analytics Flow

```text
Trades + Holdings + Wallet + MarketData
  ↓
PortfolioService
  ↓
AnalyticsService
  ├── current allocation and P/L
  ├── order and trade statistics
  └── PortfolioSnapshot persistence
  ↓
PortfolioSnapshotScheduler + AnalyticsController
  ↓
Frontend Recharts Dashboard
```

Analytics are calculated on the backend with fixed-precision decimal arithmetic. The frontend only visualizes trusted response values. Snapshots preserve portfolio value history, while current holding values use the configured market-data provider. With the default provider, those prices remain simulated mock data.

## Account Safety Flow

```text
Frontend Settings Page
  ↓
UserProfileController / RiskSettingsController
  ↓
UserProfileService / RiskSettingsService
  ↓
OrderService backend risk validation
  ↓
AuditLogService
  ↓
ActivityController and Activity Page
```

Risk controls are enforced before an order reserves virtual cash or shares. The frontend displays settings but is not trusted for enforcement. Passwords, JWTs, and secrets are excluded from audit metadata. Audit persistence uses an isolated best-effort transaction so activity-table failures do not roll back critical account or trading operations.

Simulator reset is transactional and affects only virtual trading state: pending orders are cancelled, current holdings are cleared, wallet balances return to their initial amount, historical orders/trades remain, and an analytics snapshot is created. IP address and User-Agent fields remain nullable placeholders in Phase 7.

## Admin Architecture

```text
Admin Frontend Routes
  ↓
AdminRoute role check (UX only)
  ↓
@PreAuthorize ADMIN Controllers
  ↓
AdminUserService + AdminMonitoringService
  ↓
Repositories + PortfolioService + Audit Logs + MarketDataService
  ↓
Admin Dashboard UI
```

The backend enforces the ADMIN role for every admin endpoint. The frontend check only hides navigation and redirects unauthorized users. Admin responses never expose password hashes, JWTs, or secrets. Monitoring provides simulator-platform visibility and intentionally avoids repeated paid-provider calls; the provider status endpoint performs one safe quote check per request.

## Data and Infrastructure

Neon PostgreSQL is the development and deployment database. The backend receives a JDBC URL containing `sslmode=require`, username, and password through environment variables. A local PostgreSQL installation is neither required nor assumed.

Redis is deferred. A later phase may use it for short-lived quote caching, provider rate-limit protection, and selected read models. It will not be a source of record.

Docker is also deferred. The placeholder directory reserves a location for backend and frontend images plus deployment-oriented orchestration.

## Notification and Alert Architecture

```text
OrderService / Simulator Reset / PriceAlertService
  ↓
NotificationService (isolated best-effort transaction)
  ↓
NotificationController
  ↓
Frontend Notification Center

PriceAlertProcessor
  ↓
MarketDataService
  ↓
PriceAlertService
  ↓
NotificationService
```

Notifications are in-app only. Price alerts use mock or configured backend market data, isolate failures per symbol, and never expose provider keys to the browser.

## Trading Journal Architecture

```text
Frontend Journal Page
  ↓
TradingJournalController
  ↓
TradingJournalService
  ↓
TradingJournalRepository
```

The backend resolves ownership from JWT identity. Optional order and trade links are accepted only when those records belong to the same user.

## Security

Spring Security uses stateless JWT authentication. Registration and login are public, `/api/auth/me` is protected, and future API routes require authentication by default. Passwords are hashed with BCrypt, and JWT signing configuration comes from environment variables.

The Phase 1 frontend stores the access token in `localStorage` for development simplicity. A production-grade deployment should evaluate secure `httpOnly` cookies and a CSRF strategy.

## Development

Backend commands use Maven Daemon (`mvnd`) only. Frontend commands use npm.

## Testing Strategy

- JUnit 5 and Mockito cover business rules, ownership checks, calculations, and scheduler behavior.
- MockMvc covers authentication, validation, response secrecy, and USER/ADMIN boundaries.
- Vitest, Testing Library, and jsdom cover pages, forms, route guards, navigation roles, Axios authorization, and service contracts.
- Test and production-build commands run independently to catch behavioral and packaging failures.

## Deployment Architecture

```text
Browser
  ↓ HTTPS / WSS
Static frontend hosting or nginx container
  ↓ HTTPS / WSS
Spring Boot service
  ↓ JDBC TLS (sslmode=require)
Neon PostgreSQL
```

`VITE_API_BASE_URL` and `VITE_WS_BASE_URL` are frontend build-time configuration. Backend CORS and WebSocket origins use the exact `FRONTEND_URL`. The production profile validates migrations, suppresses error details, limits Actuator exposure to health, and keeps admin bootstrap disabled by default.

## CI/CD

GitHub Actions separates backend and frontend checks using path filters:

- Backend CI configures Java 21, runs tests, builds the JAR, and uploads the artifact.
- Frontend CI configures Node.js 22, runs tests and lint, builds Vite assets, and uploads `dist`.

No Neon or financial-provider secrets are required in CI. Backend CI uses the mock provider, disables scheduled jobs, and supplies non-production placeholder configuration.
