# Invest Rocket

Invest Rocket is a full-stack virtual stock trading simulator for learning, experimentation, and portfolio demonstration. Users will eventually be able to register, receive virtual funds, explore market data, place simulated orders, and monitor portfolio performance.

> **Disclaimer:** Invest Rocket supports simulated trading only. It does not execute real-money trades, provide financial advice, or recommend investments.

## Phase 0 Status

Phase 0 establishes the monorepo, Spring Boot API, React interface, environment configuration, and architecture documentation. Authentication and trading features are intentionally not implemented yet.

## Tech Stack

- Backend: Java 21, Spring Boot, Spring Web, Spring Security, Spring Data JPA, Validation, PostgreSQL, Actuator
- Frontend: React, TypeScript, Vite, Tailwind CSS, React Router
- Database: Neon PostgreSQL over SSL; no local PostgreSQL installation required
- Planned: JWT authentication, financial market-data provider, TanStack Query or Axios, Redis, Recharts, and Docker

## Planned Features

- User registration and JWT authentication
- Virtual wallet with starting funds
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
| `JWT_SECRET` | Reserved for a later authentication phase |
| `FINANCIAL_API_PROVIDER` | Reserved market-data provider name |
| `FINANCIAL_API_KEY` | Reserved provider API key |

PowerShell example for the current terminal:

```powershell
$env:DATABASE_URL="jdbc:postgresql://your-neon-hostname.neon.tech/your-db-name?sslmode=require"
$env:DATABASE_USERNAME="your_neon_username"
$env:DATABASE_PASSWORD="your_neon_password"
$env:FRONTEND_URL="http://localhost:5173"
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

## Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite application starts at `http://localhost:5173`.

## Roadmap

- Phase 0: Foundation, documentation, health endpoint, and UI shell
- Phase 1: Users, JWT authentication, and virtual wallets
- Phase 2: Financial API integration, search, and watchlists
- Phase 3: Portfolio positions and simulated order execution
- Phase 4: Trade history, performance analytics, and charts
- Phase 5: Redis caching, Docker support, testing, and deployment hardening

See [docs/PHASES.md](docs/PHASES.md) for scope boundaries.

## API Documentation

The initial API conventions and planned resources are described in [docs/API_DESIGN.md](docs/API_DESIGN.md). Phase 0 exposes only `GET /api/health`.

## License

No license has been selected yet.
