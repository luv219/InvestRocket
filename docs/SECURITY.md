# Security

## Implemented Controls

- Stateless JWT authentication with an environment-provided signing secret
- BCrypt password hashing; plaintext passwords and hashes are excluded from API DTOs
- Default-authenticated API policy with explicit public auth and health endpoints
- Backend `ADMIN` authorization and current-user ownership enforcement
- Request validation for quantities, money, symbols, email, and text lengths
- Exact-origin REST CORS and WebSocket configuration through `FRONTEND_URL`
- Backend-only financial API credentials
- Flyway migrations and production schema validation
- Sanitized API errors with no stack traces in responses
- Disabled-by-default admin bootstrap with self-lockout protection
- `.env`, build, test, and dependency artifacts excluded from Git/Docker contexts

## Production Checklist

1. Generate a unique high-entropy `JWT_SECRET`.
2. Use a Neon JDBC URL containing `sslmode=require`.
3. Set `FRONTEND_URL` to the exact HTTPS browser origin.
4. Terminate HTTPS/WSS at the hosting platform or reverse proxy.
5. Disable admin bootstrap immediately after initial administrator creation.
6. Store secrets in the deployment platform's secret manager.
7. Limit Actuator exposure to health.
8. Run tests, dependency audit, and production builds before release.

## Recommended Future Improvements

- Replace development `localStorage` tokens with secure `httpOnly` cookies and a deliberate CSRF strategy.
- Add refresh-token rotation, revocation, and authentication rate limiting.
- Add centralized structured logging, metrics, request IDs, and alerts.
- Add dependency/container scanning, CI/CD gates, backup testing, live-database integration tests, and browser end-to-end tests.

Invest Rocket is a virtual trading simulator for educational purposes only. It does not provide financial advice and does not execute real trades.
