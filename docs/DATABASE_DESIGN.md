# Database Design

## Platform

Neon PostgreSQL is the primary development and deployment database. Developers do not need a local PostgreSQL installation.

The backend connects with:

- `DATABASE_URL`: JDBC PostgreSQL URL containing `sslmode=require`
- `DATABASE_USERNAME`: Neon role
- `DATABASE_PASSWORD`: Neon password

Credentials are environment variables and must never be stored in source control.

## Phase 0

Phase 0 deliberately defines no JPA entities, migrations, or domain tables. Hibernate schema generation is disabled.

## Planned Domain Model

Later phases are expected to introduce:

- `users`: identity and account state
- `wallets`: virtual cash balances
- `watchlists` and `watchlist_items`: tracked symbols
- `portfolios` and `positions`: simulated holdings
- `orders`: simulated buy and sell requests
- `trades`: completed simulated executions
- `portfolio_snapshots`: historical valuation for analytics

Exact columns, constraints, indexes, money precision, and migration scripts will be designed with each owning phase. Market quotes are external data and should not be treated as durable transaction truth without an explicit snapshot strategy.

## Data Integrity Principles

- Use database transactions for wallet, order, trade, and position updates.
- Store monetary values with fixed precision rather than floating-point types.
- Preserve immutable trade history.
- Use UTC timestamps.
- Keep Redis, when introduced, as a cache rather than a source of record.

All financial records represent virtual simulation activity only.
