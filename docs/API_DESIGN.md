# API Design

## Conventions

- Base path: `/api`
- Payload format: JSON
- Resource names: plural nouns for future domain resources
- Errors: consistent status, message, and timestamp payloads
- Authentication: Bearer JWT beginning in Phase 1
- Versioning: introduce `/api/v1` before public compatibility commitments require it

Invest Rocket APIs support simulation only and will not expose real-money trading operations.

## Phase 0 Endpoint

### `GET /api/health`

Public service health check.

```json
{
  "status": "UP",
  "service": "Invest Rocket Backend"
}
```

## Planned Resources

| Phase | Resource area | Example path |
| --- | --- | --- |
| 1 | Authentication | `/api/auth/login` |
| 1 | Current user | `/api/users/me` |
| 1 | Virtual wallet | `/api/wallet` |
| 2 | Market search | `/api/market/stocks` |
| 2 | Quotes | `/api/market/quotes/{symbol}` |
| 2 | Watchlists | `/api/watchlists` |
| 3 | Portfolio | `/api/portfolio` |
| 3 | Simulated orders | `/api/orders` |
| 3 | Trades | `/api/trades` |
| 4 | Analytics | `/api/analytics/portfolio` |

These paths are architectural proposals, not Phase 0 implementations. Financial provider keys will always be server-side environment variables.
