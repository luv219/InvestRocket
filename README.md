# Invest Rocket

Invest Rocket is a full-stack virtual stock trading simulator for learning, experimentation, and portfolio demonstration. Users will eventually be able to register, receive virtual funds, explore market data, place simulated orders, and monitor portfolio performance.

> **Disclaimer:** Invest Rocket supports simulated trading only. It does not execute real-money trades, provide financial advice, or recommend investments.

## Phase 1 Status

Phase 1 adds user registration, login, BCrypt password hashing, JWT authentication, protected frontend routes, and automatic creation of a virtual USD wallet with a starting balance of `$100,000.00`.

Stock search, market data, orders, portfolio calculations, watchlists, analytics, and real-time features are intentionally outside this phase.

## Tech Stack

- Backend: Java 21, Spring Boot, Spring Web, Spring Security, Spring Data JPA, Validation, PostgreSQL, Actuator
- Frontend: React, TypeScript, Vite, Tailwind CSS, React Router
- Database: Neon PostgreSQL over SSL; no local PostgreSQL installation required
- Auth: Stateless JWT security with BCrypt password hashing
- Planned: Financial market-data provider, TanStack Query, Redis, Recharts, and Docker

## Planned Features

- User registration and JWT authentication — complete
- Virtual wallet creation with starting funds — complete
- Stock search and near real-time market prices
- Simulated buy and sell orders
- Portfolio positions and performance tracking
- Watchlists and analytics dashboards

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
| `FINANCIAL_API_PROVIDER` | Reserved market-data provider name |
| `FINANCIAL_API_KEY` | Reserved provider API key |

PowerShell example for the current terminal:

```powershell
$env:DATABASE_URL="jdbc:postgresql://your-neon-hostname.neon.tech/your-db-name?sslmode=require"
$env:DATABASE_USERNAME="your_neon_username"
$env:DATABASE_PASSWORD="your_neon_password"
$env:JWT_SECRET="replace_with_long_secure_secret_at_least_32_characters"
$env:JWT_EXPIRATION_MS="86400000"
$env:FRONTEND_URL="http://localhost:5173"
```

Create `frontend/.env` from `frontend/.env.example` to configure the browser API URL:

```text
VITE_API_BASE_URL=http://localhost:8080/api
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

Flyway automatically applies `V1__create_users_and_wallets.sql` to the configured Neon database at startup.

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

## Roadmap

- Phase 0: Foundation, documentation, health endpoint, and UI shell — complete
- Phase 1: Users, JWT authentication, and virtual wallets — complete
- Phase 2: Financial API integration, search, and watchlists
- Phase 3: Portfolio positions and simulated order execution
- Phase 4: Trade history, performance analytics, and charts
- Phase 5: Redis caching, Docker support, testing, and deployment hardening

See [docs/PHASES.md](docs/PHASES.md) for scope boundaries.

## API Documentation

The API conventions and Phase 1 authentication contract are described in [docs/API_DESIGN.md](docs/API_DESIGN.md).

## License

No license has been selected yet.
