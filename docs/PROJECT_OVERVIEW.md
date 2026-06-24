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

## Intended Users

- Students learning full-stack engineering and financial concepts
- Developers demonstrating a portfolio-grade application
- Users practicing basic investment workflows without financial risk

## Future Capabilities

Authenticated users can search stocks, view quotes, and place virtual whole-share market buy and sell orders. Successful orders update virtual cash and holdings and create order and trade history records.

Only market orders are supported at this stage. Later phases may add watchlists, advanced order types, historical snapshots, and analytics.

## Constraints

- All balances, orders, trades, and returns are simulated.
- Neon PostgreSQL is the primary database; local PostgreSQL is not required.
- Backend development commands use Maven Daemon (`mvnd`) only.
- External financial data requires a provider key supplied through environment variables.
- Secrets and credentials must never be committed.
