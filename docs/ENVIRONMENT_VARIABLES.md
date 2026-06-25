# Environment Variables

## Local Backend

| Variable | Secret | Example |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | No | `dev` |
| `DATABASE_URL` | No | `jdbc:postgresql://host.neon.tech/db?sslmode=require` |
| `DATABASE_USERNAME` | Yes | `your_neon_username` |
| `DATABASE_PASSWORD` | Yes | `your_neon_password` |
| `JWT_SECRET` | Yes | `replace_with_long_secure_secret_at_least_32_characters` |
| `JWT_EXPIRATION_MS` | No | `86400000` |
| `FRONTEND_URL` | No | `http://localhost:5173` |
| `BACKEND_URL` | No | `http://localhost:8080` |
| `FINANCIAL_API_PROVIDER` | No | `mock` |
| `FINANCIAL_API_KEY` | Yes | `replace_with_your_api_key` |

## Production Backend

Use `SPRING_PROFILES_ACTIVE=prod`, the production Neon connection values, a unique JWT secret, and the exact HTTPS frontend origin. Render and Railway provide `PORT` automatically.

Scheduled jobs:

- `PENDING_ORDER_PROCESSOR_ENABLED`
- `PENDING_ORDER_PROCESSOR_INTERVAL_MS`
- `LIVE_PRICE_STREAM_ENABLED`
- `LIVE_PRICE_STREAM_INTERVAL_MS`
- `PORTFOLIO_SNAPSHOT_ENABLED`
- `PORTFOLIO_SNAPSHOT_INTERVAL_MS`
- `PRICE_ALERT_PROCESSOR_ENABLED`
- `PRICE_ALERT_PROCESSOR_INTERVAL_MS`

Admin bootstrap:

- `ADMIN_BOOTSTRAP_ENABLED`
- `ADMIN_EMAIL`
- `ADMIN_PASSWORD`
- `ADMIN_FULL_NAME`

`ADMIN_BOOTSTRAP_ENABLED` must return to `false` immediately after initial setup.

## Local Frontend

```text
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_BASE_URL=ws://localhost:8080/ws
```

## Production Frontend

```text
VITE_API_BASE_URL=https://your-backend-domain.com/api
VITE_WS_BASE_URL=wss://your-backend-domain.com/ws
```

All `VITE_*` values are public and compiled into browser assets. Never place credentials, JWT secrets, database passwords, or financial API keys in frontend variables.

Use `.env.example` and `frontend/.env.production.example` as safe templates. Real `.env` files are ignored and must never be committed.
