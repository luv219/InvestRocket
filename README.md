# Invest Rocket

Invest Rocket is a full-stack virtual stock trading simulator for learning, experimentation, and portfolio demonstration. Users will eventually be able to register, receive virtual funds, explore market data, place simulated orders, and monitor portfolio performance.

> **Disclaimer:** Invest Rocket supports simulated trading only. It does not execute real-money trades, provide financial advice, or recommend investments.

## Phase 9 Status

Phase 9 adds authenticated in-app notifications, scheduled price alerts, and a private trading journal linked optionally to owned orders and trades.

The admin bootstrap is disabled by default and never contains a real credential in source control.

## Tech Stack

- Backend: Java 21, Spring Boot, Spring Web, Spring Security, Spring Data JPA, Validation, PostgreSQL, Actuator
- Frontend: React, TypeScript, Vite, Tailwind CSS, React Router
- Database: Neon PostgreSQL over SSL; no local PostgreSQL installation required
- Auth: Stateless JWT security with BCrypt password hashing
- Market data: Provider abstraction with mock and Finnhub implementations
- Trading: Transactional simulated market orders, holdings, orders, and trades
- Advanced orders: Pending lifecycle, reservation, cancellation, and scheduled triggers
- Live updates: Spring WebSocket/STOMP, in-memory demo prices, and React STOMP client
- Analytics: Portfolio snapshots, P/L metrics, allocation, trading statistics, and Recharts
- Account safety: Profile settings, password change, risk controls, simulator reset, and audit logs
- Administration: User management, platform monitoring, admin audit visibility, and system health
- Planned: TanStack Query, Redis pub/sub, and Docker

## Planned Features

- User registration and JWT authentication — complete
- Virtual wallet creation with starting funds — complete
- Stock search and on-demand market quotes — complete
- Simulated market buy and sell orders — complete
- Limit buy and sell orders — complete
- Stop-loss sell orders — complete
- Pending order cancellation and processing — complete
- Portfolio holdings and valuation — complete
- Order and trade history — complete
- Personal watchlists with live demo prices — complete
- Portfolio analytics and performance dashboard — complete
- User profile, risk settings, simulator reset, and activity logs — complete
- Admin dashboard and platform monitoring — complete

## Project Structure

```text
InvestRocket/
├── backend/       Spring Boot API
├── frontend/      React application
├── docs/          Product and architecture documentation
├── docker/        Placeholder for future container support
├── .env.example   Environment variable template
└── README.md
```

## Prerequisites

- Java 21
- Maven Daemon (`mvnd`)
- Node.js 20 or newer
- npm
- A Neon PostgreSQL project

## Environment Setup

Copy `.env.example` to a local `.env` file and replace every placeholder. Never commit `.env` or credentials.

Required backend variables:

| Variable | Purpose |
| --- | --- |
| `DATABASE_URL` | Neon JDBC URL including `sslmode=require` |
| `DATABASE_USERNAME` | Neon database role |
| `DATABASE_PASSWORD` | Neon database password |
| `FRONTEND_URL` | Allowed browser origin |
| `JWT_SECRET` | JWT signing secret with at least 32 characters |
| `JWT_EXPIRATION_MS` | Access token lifetime in milliseconds |
| `FINANCIAL_API_PROVIDER` | `mock` by default, or `finnhub` |
| `FINANCIAL_API_KEY` | Required only for the Finnhub provider |
| `PENDING_ORDER_PROCESSOR_ENABLED` | Enables scheduled pending-order checks |
| `PENDING_ORDER_PROCESSOR_INTERVAL_MS` | Delay between checks; defaults to `30000` |
| `LIVE_PRICE_STREAM_ENABLED` | Enables the mock WebSocket price generator |
| `LIVE_PRICE_STREAM_INTERVAL_MS` | Delay between mock broadcasts; defaults to `5000` |
| `PORTFOLIO_SNAPSHOT_ENABLED` | Enables scheduled portfolio snapshots |
| `PORTFOLIO_SNAPSHOT_INTERVAL_MS` | Snapshot interval; defaults to `300000` |
| `ADMIN_BOOTSTRAP_ENABLED` | Enables one-time initial admin creation; defaults to `false` |
| `PRICE_ALERT_PROCESSOR_ENABLED` | Enables scheduled active-alert checks; defaults to `true` |
| `PRICE_ALERT_PROCESSOR_INTERVAL_MS` | Alert polling interval; defaults to `15000` |
| `ADMIN_EMAIL` | Initial admin email when bootstrap is enabled |
| `ADMIN_PASSWORD` | Initial admin password; must be at least 8 characters |
| `ADMIN_FULL_NAME` | Initial admin display name |

PowerShell example for the current terminal:

```powershell
$env:DATABASE_URL="jdbc:postgresql://your-neon-hostname.neon.tech/your-db-name?sslmode=require"
$env:DATABASE_USERNAME="your_neon_username"
$env:DATABASE_PASSWORD="your_neon_password"
$env:JWT_SECRET="replace_with_long_secure_secret_at_least_32_characters"
$env:JWT_EXPIRATION_MS="86400000"
$env:FRONTEND_URL="http://localhost:5173"
$env:FINANCIAL_API_PROVIDER="mock"
$env:PENDING_ORDER_PROCESSOR_ENABLED="true"
$env:PENDING_ORDER_PROCESSOR_INTERVAL_MS="30000"
$env:LIVE_PRICE_STREAM_ENABLED="true"
$env:LIVE_PRICE_STREAM_INTERVAL_MS="5000"
$env:PORTFOLIO_SNAPSHOT_ENABLED="true"
$env:PORTFOLIO_SNAPSHOT_INTERVAL_MS="300000"
$env:ADMIN_BOOTSTRAP_ENABLED="false"
```

Create `frontend/.env` from `frontend/.env.example` to configure the browser API URL:

```text
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_BASE_URL=ws://localhost:8080/ws
```

## Neon PostgreSQL

1. Create a project and database in Neon.
2. Copy the host, database, role, and password from the Neon connection details.
3. Use the JDBC format shown in `.env.example`.
4. Keep `sslmode=require` in the URL.
5. Export the variables before starting the backend.

Local PostgreSQL is not required or assumed.

## Run the Backend

```bash
cd backend
mvnd spring-boot:run
```

The API starts at `http://localhost:8080`. Verify:

```text
GET http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "UP",
  "service": "Invest Rocket Backend"
}
```

Flyway automatically applies all migrations through `V7__admin_monitoring_indexes.sql` to the configured Neon database at startup.

## Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite application starts at `http://localhost:5173`.

Phase 1 stores the JWT in browser `localStorage` under `invest_rocket_token`. This is acceptable for the current development phase. A production-grade deployment should evaluate secure, `httpOnly`, `sameSite` cookies to reduce token exposure to browser scripts.

## Authentication API

| Method | Endpoint | Access |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Public |
| `POST` | `/api/auth/login` | Public |
| `GET` | `/api/auth/me` | Bearer token required |

Registration creates both the user and their virtual wallet in one database transaction.

## Market Data API

Both market endpoints require `Authorization: Bearer <accessToken>`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/market/search?query=AAPL` | Search symbols and companies |
| `GET` | `/api/market/quote/{symbol}` | Fetch the latest available quote |

The frontend never calls a financial data provider directly, and provider API keys remain on the backend.

### Providers

- `mock`: default development provider with deterministic sample symbols and no API key
- `finnhub`: uses Finnhub search, quote, and company profile endpoints; requires `FINANCIAL_API_KEY`

Finnhub quote responses do not include trade volume in the basic quote payload, so `volume` can be unavailable when that provider is selected.

## Trading API

All trading endpoints require `Authorization: Bearer <accessToken>`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/orders` | Submit a market, limit, or stop-loss sell order |
| `GET` | `/api/orders` | Current user’s order history |
| `GET` | `/api/orders/pending` | Current user’s pending orders |
| `DELETE` | `/api/orders/{orderId}/cancel` | Cancel one owned pending order |
| `GET` | `/api/trades` | Current user’s trade history |
| `GET` | `/api/portfolio/holdings` | Current holdings with market valuation |
| `GET` | `/api/portfolio/summary` | Wallet and portfolio summary |

The frontend never sends an execution price. The backend fetches the active provider quote and either executes immediately or creates a pending order.

- Pending limit buys move `limitPrice × quantity` from available cash to reserved cash.
- Pending limit and stop-loss sells move shares from available quantity to locked quantity.
- Execution settles the reservation transactionally and creates one trade.
- Cancellation returns reserved cash or unlocks shares.
- The scheduled processor checks pending triggers every 30 seconds by default.

Example request:

```json
{
  "symbol": "AAPL",
  "side": "BUY",
  "orderType": "MARKET",
  "quantity": 1
}
```

Limit buy example:

```json
{
  "symbol": "AAPL",
  "side": "BUY",
  "orderType": "LIMIT",
  "quantity": 1,
  "limitPrice": 180.00
}
```

## Watchlist and Live Prices

All watchlist endpoints require `Authorization: Bearer <accessToken>`. User identity is resolved from the JWT; the frontend never submits a user ID.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/watchlist` | List the current user’s enriched watchlist |
| `POST` | `/api/watchlist` | Add one symbol |
| `DELETE` | `/api/watchlist/{symbol}` | Remove one owned symbol |

WebSocket/STOMP configuration:

- Endpoint: `ws://localhost:8080/ws`
- General topic: `/topic/prices`
- Symbol topic: `/topic/prices/{symbol}`
- Allowed browser origin: `FRONTEND_URL`

The mock generator broadcasts AAPL, MSFT, TSLA, AMZN, GOOGL, NVDA, and META. Provider API keys remain backend-only. Redis pub/sub is deferred.

## Portfolio Analytics API

All analytics endpoints require `Authorization: Bearer <accessToken>`. Calculations are performed by the backend from virtual wallet, holdings, orders, trades, snapshots, and current provider quotes.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/analytics/overview` | Complete analytics overview |
| `GET` | `/api/analytics/performance` | Portfolio snapshot history |
| `GET` | `/api/analytics/allocation` | Allocation by holding |
| `GET` | `/api/analytics/holdings` | Holding-level performance |
| `GET` | `/api/analytics/trading-stats` | Order and trade statistics |
| `POST` | `/api/analytics/snapshot` | Create a current-user snapshot |

The scheduler creates snapshots every five minutes by default. Daily P/L uses the latest prior-day snapshot as its baseline. Recharts is used only to visualize backend response values.

## Account Settings API

All endpoints require `Authorization: Bearer <accessToken>` and resolve the current user from the JWT.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/profile` | Fetch profile settings |
| `PUT` | `/api/profile` | Update profile information |
| `PUT` | `/api/profile/password` | Change password after current-password verification |
| `POST` | `/api/profile/reset-simulator` | Reset virtual balance and holdings |
| `GET` | `/api/profile/risk-settings` | Fetch trading limits |
| `PUT` | `/api/profile/risk-settings` | Update trading limits |
| `GET` | `/api/activity` | Fetch account activity |
| `GET` | `/api/activity?category=ORDER` | Filter activity by category |

Default risk controls limit orders to `$25,000` and 50 orders per UTC day. Limits and order-type permissions are enforced by the backend before funds or shares are reserved.

Simulator reset requires the exact confirmation text `RESET MY SIMULATOR`. It restores the initial virtual balance, clears reserved cash and current holdings, cancels pending orders, preserves historical orders/trades, and creates an analytics snapshot.

## Admin Platform API

Every admin endpoint requires a valid JWT with the `ADMIN` role.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/admin/dashboard` | Platform-wide summary |
| `GET` | `/api/admin/trading-stats` | Trading composition and rankings |
| `GET` | `/api/admin/system-health` | Backend, database, and scheduler health |
| `GET` | `/api/admin/audit-logs` | Recent platform activity |
| `GET` | `/api/admin/market-data-status` | Safe provider quote check |
| `GET` | `/api/admin/users` | User list |
| `GET` | `/api/admin/users/{userId}` | User detail |
| `PUT` | `/api/admin/users/{userId}` | Update role, name, or status |
| `POST` | `/api/admin/users/{userId}/disable` | Disable a user |
| `POST` | `/api/admin/users/{userId}/enable` | Enable a user |

To create the first admin, set `ADMIN_BOOTSTRAP_ENABLED=true` with a secure email/password for one startup. If the email already exists, no account is recreated. Disable bootstrap afterward. Administrators cannot disable themselves or remove their own `ADMIN` role.

## Notifications, Alerts, and Journal API

All endpoints require `Authorization: Bearer <accessToken>` and operate only on the current user.

| Module | Endpoints |
| --- | --- |
| Notifications | `GET /api/notifications`, `/unread`, `/summary`; `PUT /{id}/read`, `/read-all`; `DELETE /{id}` |
| Price alerts | `POST /api/alerts`; `GET /api/alerts`, `/active`; `DELETE /api/alerts/{id}/cancel` |
| Trading journal | `POST/GET /api/journal`; `GET /api/journal?symbol=AAPL`; `PUT/DELETE /api/journal/{id}` |

Order execution, pending-order creation/cancellation, simulator reset, and triggered alerts create best-effort in-app notifications. Alerts poll configured market data every 15 seconds by default. No email, SMS, or push delivery is included.

## Development Commit

```bash
git add .
git commit -m "feat: implement phase 9 notifications alerts and trading journal"
```

## Roadmap

- Phase 0: Foundation, documentation, health endpoint, and UI shell — complete
- Phase 1: Users, JWT authentication, and virtual wallets — complete
- Phase 2: Market-data provider foundation, stock search, and quotes — complete
- Phase 3: Transactional market orders, holdings, portfolio, orders, and trades — complete
- Phase 4: Limit orders, stop-loss sells, pending processing, and cancellation — complete
- Phase 5: Watchlists and live demo price updates — complete
- Phase 6: Portfolio performance analytics and charts — complete
- Phase 7: Profile settings, risk controls, reset, and audit logs — complete
- Phase 8: Admin dashboard and platform monitoring — complete
- Phase 9: Notifications, price alerts, and trading journal — complete
- Phase 10: Redis caching, Docker support, testing, and deployment hardening

See [docs/PHASES.md](docs/PHASES.md) for scope boundaries.

## API Documentation

The API conventions and authentication, market-data, and advanced-order contracts are described in [docs/API_DESIGN.md](docs/API_DESIGN.md).

## License

No license has been selected yet.
