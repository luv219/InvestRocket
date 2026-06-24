# API Design

## Conventions

- Base path: `/api`
- Payload format: JSON
- Authentication: `Authorization: Bearer <accessToken>`
- Errors: consistent success flag, message, field errors, and timestamp
- Future APIs are protected by default

Invest Rocket APIs support simulated activity only. They do not expose brokerage or real-money trading operations.

## Public Endpoints

### `GET /api/health`

```json
{
  "status": "UP",
  "service": "Invest Rocket Backend"
}
```

### `POST /api/auth/register`

Creates a user, a `$100,000.00` virtual USD wallet, and a JWT.

Request:

```json
{
  "fullName": "Demo User",
  "email": "demo@example.com",
  "password": "Password123",
  "confirmPassword": "Password123"
}
```

Successful response:

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "<jwt>",
    "tokenType": "Bearer",
    "user": {
      "id": "6da4078a-d7d5-40fc-9501-81c3925dc813",
      "fullName": "Demo User",
      "email": "demo@example.com",
      "role": "USER"
    }
  },
  "timestamp": "2026-06-24T16:00:00Z"
}
```

### `POST /api/auth/login`

Request:

```json
{
  "email": "demo@example.com",
  "password": "Password123"
}
```

The successful response uses the same `data` shape as registration.

## Protected Endpoints

### `GET /api/auth/me`

Requires a valid Bearer JWT and returns the current user profile.

```json
{
  "success": true,
  "message": "Current user retrieved successfully",
  "data": {
    "id": "6da4078a-d7d5-40fc-9501-81c3925dc813",
    "fullName": "Demo User",
    "email": "demo@example.com",
    "role": "USER"
  },
  "timestamp": "2026-06-24T16:00:00Z"
}
```

### `GET /api/market/search?query=AAPL`

Requires a valid Bearer JWT. Searches the active market-data provider.

```json
{
  "success": true,
  "message": "Stock search completed successfully",
  "data": [
    {
      "symbol": "AAPL",
      "name": "Apple Inc.",
      "exchange": "NASDAQ",
      "currency": "USD",
      "type": "Common Stock"
    }
  ],
  "timestamp": "2026-06-24T16:00:00Z"
}
```

### `GET /api/market/quote/AAPL`

Requires a valid Bearer JWT.

```json
{
  "success": true,
  "message": "Quote fetched successfully",
  "data": {
    "symbol": "AAPL",
    "companyName": "Apple Inc.",
    "currentPrice": 195.25,
    "changeAmount": 1.45,
    "changePercent": 0.75,
    "openPrice": 193.00,
    "highPrice": 196.10,
    "lowPrice": 192.70,
    "previousClose": 193.80,
    "volume": 58400000,
    "latestTradingTime": "2026-06-24T16:00:00Z",
    "currency": "USD",
    "provider": "mock"
  },
  "timestamp": "2026-06-24T16:00:00Z"
}
```

Provider rate limits return `429 Too Many Requests`, missing Finnhub configuration returns `503 Service Unavailable`, upstream failures return `502 Bad Gateway`, and unknown symbols return `404 Not Found`.

## Error Format

Validation example:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Email must be valid"
  },
  "timestamp": "2026-06-24T16:00:00Z"
}
```

Duplicate email returns `409 Conflict`, invalid credentials and missing authentication return `401 Unauthorized`, and malformed request data returns `400 Bad Request`.

## Planned Resources

| Phase | Resource area | Example path |
| --- | --- | --- |
| Future | Watchlists | `/api/watchlists` |
| 3 | Portfolio | `/api/portfolio` |
| 3 | Simulated orders | `/api/orders` |
| 3 | Trades | `/api/trades` |
| 4 | Analytics | `/api/analytics/portfolio` |

These future paths are architectural proposals, not Phase 2 implementations.
