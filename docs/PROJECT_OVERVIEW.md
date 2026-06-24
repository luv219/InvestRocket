# Project Overview

## Purpose

Invest Rocket is an educational virtual stock trading platform. It will let users practice portfolio management with simulated funds and market data without executing real-money transactions.

Invest Rocket does not provide financial advice, investment recommendations, brokerage services, or access to real-money trading.

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

## Intended Users

- Students learning full-stack engineering and financial concepts
- Developers demonstrating a portfolio-grade application
- Users practicing basic investment workflows without financial risk

## Future Capabilities

Authenticated users can search stocks, view quotes, and place virtual whole-share market buy and sell orders. Successful orders update virtual cash and holdings and create order and trade history records.

Users can place market, limit, and stop-loss sell orders. Pending orders are evaluated by a scheduled processor and can be cancelled before execution. Every operation remains virtual and simulated.

Watchlist prices now update through a backend-generated demo stream. These values are simulated, are not persisted, and are not licensed exchange data.

Users can now review total return, realized and unrealized profit or loss, best and worst holdings, allocation, trading statistics, and historical portfolio snapshots.

## Constraints

- All balances, orders, trades, and returns are simulated.
- Neon PostgreSQL is the primary database; local PostgreSQL is not required.
- Backend development commands use Maven Daemon (`mvnd`) only.
- External financial data requires a provider key supplied through environment variables.
- Secrets and credentials must never be committed.
