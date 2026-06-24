# Database Design

## Platform

Neon PostgreSQL is the primary development and deployment database. A local PostgreSQL installation is not required.

The backend connects using:

- `DATABASE_URL`: JDBC PostgreSQL URL containing `sslmode=require`
- `DATABASE_USERNAME`: Neon role
- `DATABASE_PASSWORD`: Neon password

Flyway owns schema changes. Phase 1 introduces `V1__create_users_and_wallets.sql`, while Hibernate validates the mapped schema without generating it.

## Users

| Column | Type | Rules |
| --- | --- | --- |
| `id` | `UUID` | Primary key |
| `full_name` | `VARCHAR(120)` | Required |
| `email` | `VARCHAR(320)` | Required and unique |
| `password_hash` | `VARCHAR(255)` | Required BCrypt hash |
| `role` | `VARCHAR(20)` | `USER` or `ADMIN`; defaults to `USER` |
| `is_enabled` | `BOOLEAN` | Defaults to `TRUE` |
| `created_at` | `TIMESTAMPTZ` | Required UTC timestamp |
| `updated_at` | `TIMESTAMPTZ` | Required UTC timestamp |

Plain-text passwords are never persisted or returned by the API.

## Wallets

| Column | Type | Rules |
| --- | --- | --- |
| `id` | `UUID` | Primary key |
| `user_id` | `UUID` | Unique foreign key to `users` |
| `cash_balance` | `NUMERIC(19,2)` | Defaults to `100000.00` |
| `initial_balance` | `NUMERIC(19,2)` | Defaults to `100000.00` |
| `currency` | `VARCHAR(3)` | Defaults to `USD` |
| `created_at` | `TIMESTAMPTZ` | Required UTC timestamp |
| `updated_at` | `TIMESTAMPTZ` | Required UTC timestamp |

Every successful registration creates exactly one wallet in the same transaction. Wallet balances are virtual and cannot be deposited, withdrawn, or traded for real money.

## Future Tables

Later phases may add watchlists, portfolios, positions, orders, trades, and portfolio snapshots. They are deliberately absent from Phase 1.

## Data Integrity Principles

- Use database transactions for user and wallet creation.
- Store money with fixed-precision decimal types.
- Normalize emails before lookup and persistence.
- Preserve UTC timestamps.
- Keep Redis, when introduced, as a cache rather than a source of record.
