# Project Overview

## Purpose

Invest Rocket is a portfolio-grade educational stock trading simulator. Users practice portfolio management with simulated funds and market data without executing real-money transactions.

Invest Rocket does not provide financial advice, investment recommendations, brokerage services, or access to real-money trading.

The completed system demonstrates secure authentication, transactional virtual trading, scheduled processing, WebSocket updates, analytics, administration, automated testing, and containerized deployment preparation.

## Completed Foundation

- Spring Boot backend foundation with a public health endpoint
- React, TypeScript, Vite, and Tailwind CSS interface foundation
- Neon PostgreSQL environment-based configuration
- Package boundaries for future product modules
- Architecture, API, database, and phased-delivery documentation
- Registration and login with BCrypt and JWT security
- Automatic virtual wallet creation
- Protected dashboard routes and authenticated navigation
- Authenticated stock search and quote pages
- Mock market-data provider and backend-only Finnhub integration
- Virtual market buy and sell execution
- Portfolio holdings, wallet valuation, order history, and trade history
- Limit orders, stop-loss sells, pending orders, and cancellation
- Reserved virtual cash and locked holding quantities
- Personal watchlists owned by the authenticated user
- WebSocket/STOMP demo price updates on watchlist and stock-detail pages
- Portfolio snapshots, performance history, allocation, and trading statistics
- Protected Recharts analytics dashboard
- User-managed profile and password settings
- Backend-enforced trading risk controls
- Simulator reset and per-user activity history
- Admin user management, platform statistics, and service health monitoring
- In-app notifications for trading and simulator lifecycle events
- Scheduled price alerts using configured market data
- Private trading journal with optional order and trade links
- Automated JUnit/MockMvc and Vitest/Testing Library suites
- Production profile, Actuator health, Docker images, and deployment runbooks

## Intended Users

- Students learning full-stack engineering and financial concepts
- Developers demonstrating a portfolio-grade application
- Users practicing basic investment workflows without financial risk

## Future Capabilities

Authenticated users can search stocks, view quotes, and place virtual whole-share market buy and sell orders. Successful orders update virtual cash and holdings and create order and trade history records.

Users can place market, limit, and stop-loss sell orders. Pending orders are evaluated by a scheduled processor and can be cancelled before execution. Every operation remains virtual and simulated.

Watchlist prices now update through a backend-generated demo stream. These values are simulated, are not persisted, and are not licensed exchange data.

Users can now review total return, realized and unrealized profit or loss, best and worst holdings, allocation, trading statistics, and historical portfolio snapshots.

Users can also update profile settings, change their password, configure order safety limits, inspect account activity, and reset virtual trading state without deleting their account.

Administrators can manage simulator users, inspect platform-wide trading metrics, review recent audit events, and monitor backend, database, scheduler, and market-provider status.

Users can now review in-app notifications, create ABOVE or BELOW price alerts, and maintain a private journal for simulated trading decisions. Email, SMS, push delivery, and AI recommendations remain out of scope.

## Constraints

- All balances, orders, trades, and returns are simulated.
- Neon PostgreSQL is the primary database; local PostgreSQL is not required.
- Backend development commands use Maven Daemon (`mvnd`) only.
- External financial data requires a provider key supplied through environment variables.
- Secrets and credentials must never be committed.
- Production uses environment-driven configuration and Flyway schema validation.

## Portfolio Highlights

- Modular Spring Boot services with thin controllers and ownership checks
- JWT, BCrypt, backend-enforced role authorization, and fixed-precision money handling
- Mock/Finnhub provider abstraction, scheduled processors, STOMP updates, and Recharts analytics
- Flyway-managed Neon PostgreSQL, automated regression tests, Docker, and production documentation
