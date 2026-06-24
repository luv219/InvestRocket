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
- `watchlist`: reserved for a later phase
- `portfolio`, `order`, `trade`: simulated execution and positions in Phase 3
- `analytics`: performance calculations in Phase 4
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

## Data and Infrastructure

Neon PostgreSQL is the development and deployment database. The backend receives a JDBC URL containing `sslmode=require`, username, and password through environment variables. A local PostgreSQL installation is neither required nor assumed.

Redis is deferred. A later phase may use it for short-lived quote caching, provider rate-limit protection, and selected read models. It will not be a source of record.

Docker is also deferred. The placeholder directory reserves a location for backend and frontend images plus deployment-oriented orchestration.

## Security

Spring Security uses stateless JWT authentication. Registration and login are public, `/api/auth/me` is protected, and future API routes require authentication by default. Passwords are hashed with BCrypt, and JWT signing configuration comes from environment variables.

The Phase 1 frontend stores the access token in `localStorage` for development simplicity. A production-grade deployment should evaluate secure `httpOnly` cookies and a CSRF strategy.

## Development

Backend commands use Maven Daemon (`mvnd`) only. Frontend commands use npm.
