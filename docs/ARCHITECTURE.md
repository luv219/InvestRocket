# Architecture

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
- `analytics`: reserved for performance calculations in Phase 6
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

## Data and Infrastructure

Neon PostgreSQL is the development and deployment database. The backend receives a JDBC URL containing `sslmode=require`, username, and password through environment variables. A local PostgreSQL installation is neither required nor assumed.

Redis is deferred. A later phase may use it for short-lived quote caching, provider rate-limit protection, and selected read models. It will not be a source of record.

Docker is also deferred. The placeholder directory reserves a location for backend and frontend images plus deployment-oriented orchestration.

## Security

Spring Security uses stateless JWT authentication. Registration and login are public, `/api/auth/me` is protected, and future API routes require authentication by default. Passwords are hashed with BCrypt, and JWT signing configuration comes from environment variables.

The Phase 1 frontend stores the access token in `localStorage` for development simplicity. A production-grade deployment should evaluate secure `httpOnly` cookies and a CSRF strategy.

## Development

Backend commands use Maven Daemon (`mvnd`) only. Frontend commands use npm.
