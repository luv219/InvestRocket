# Database Design

## Platform

Neon PostgreSQL is the primary development and deployment database. A local PostgreSQL installation is not required.

The backend connects using:

- `DATABASE_URL`: JDBC PostgreSQL URL containing `sslmode=require`
- `DATABASE_USERNAME`: Neon role
- `DATABASE_PASSWORD`: Neon password

Flyway owns schema changes. `V1__create_users_and_wallets.sql` creates identity and wallet tables, and `V2__create_trading_tables.sql` creates the Phase 3 trading model. Hibernate validates the mapped schema without generating it.

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

## Holdings

One row represents one symbol held by one user.

| Column | Type | Rules |
| --- | --- | --- |
| `id` | `UUID` | Primary key |
| `user_id` | `UUID` | Foreign key to `users` |
| `symbol` | `VARCHAR(15)` | Uppercase symbol |
| `company_name` | `VARCHAR(255)` | Quote-time company name |
| `quantity` | `INTEGER` | Positive whole-share quantity |
| `average_buy_price` | `NUMERIC(19,4)` | Weighted average cost |
| `total_invested` | `NUMERIC(19,2)` | Remaining cost basis |
| `created_at`, `updated_at` | `TIMESTAMPTZ` | UTC timestamps |

`(user_id, symbol)` is unique. A holding is deleted when all shares are sold.

## Orders

| Column | Type | Rules |
| --- | --- | --- |
| `id` | `UUID` | Primary key |
| `user_id` | `UUID` | Foreign key to `users` |
| `symbol`, `side`, `order_type` | Text | Market-order details |
| `quantity` | `INTEGER` | Positive whole-share quantity |
| `requested_price`, `executed_price` | `NUMERIC(19,4)` | Backend quote price |
| `status` | `VARCHAR(20)` | Executed for successful Phase 3 orders |
| `total_amount` | `NUMERIC(19,2)` | Price multiplied by quantity |
| `created_at`, `executed_at` | `TIMESTAMPTZ` | UTC timestamps |

## Trades

Each successful order creates one immutable trade.

| Column | Type | Rules |
| --- | --- | --- |
| `id` | `UUID` | Primary key |
| `order_id` | `UUID` | Unique foreign key to `orders` |
| `user_id` | `UUID` | Foreign key to `users` |
| `symbol`, `side`, `quantity` | Mixed | Execution details |
| `price`, `trade_value` | Numeric | Execution price and total |
| `realized_profit_loss` | `NUMERIC(19,2)` | Zero for buys; average-cost result for sells |
| `executed_at` | `TIMESTAMPTZ` | UTC execution time |

## Future Tables

Later phases may add watchlists and portfolio snapshots.

## Data Integrity Principles

- Use database transactions for wallet, holding, order, and trade updates.
- Lock the user wallet and matching holding during order execution.
- Store money with fixed-precision decimal types.
- Normalize emails before lookup and persistence.
- Preserve UTC timestamps.
- Keep Redis, when introduced, as a cache rather than a source of record.
