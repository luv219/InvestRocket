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

### `POST /api/orders`

Requires a valid Bearer JWT. The backend ignores any client-side execution estimate and fetches the current quote before deciding between immediate execution and pending creation.

```json
{
  "symbol": "AAPL",
  "side": "BUY",
  "orderType": "MARKET",
  "quantity": 1
}
```

```json
{
  "success": true,
  "message": "Market buy order executed successfully",
  "data": {
    "id": "dc2279b2-4a40-4bd7-a746-b34c213a855a",
    "symbol": "AAPL",
    "side": "BUY",
    "orderType": "MARKET",
    "quantity": 1,
    "requestedPrice": 195.2500,
    "executedPrice": 195.2500,
    "status": "EXECUTED",
    "totalAmount": 195.25,
    "createdAt": "2026-06-24T16:00:00Z",
    "executedAt": "2026-06-24T16:00:00Z",
    "message": "Market buy order executed successfully"
  },
  "timestamp": "2026-06-24T16:00:00Z"
}
```

`MARKET`, `LIMIT`, and sell-side `STOP_LOSS` requests are accepted in Phase 4. Insufficient available virtual cash or holdings returns `400 Bad Request`.

Phase 4 request examples:

Limit buy:

```json
{
  "symbol": "AAPL",
  "side": "BUY",
  "orderType": "LIMIT",
  "quantity": 1,
  "limitPrice": 180.00
}
```

Limit sell:

```json
{
  "symbol": "AAPL",
  "side": "SELL",
  "orderType": "LIMIT",
  "quantity": 1,
  "limitPrice": 220.00
}
```

Stop-loss sell:

```json
{
  "symbol": "AAPL",
  "side": "SELL",
  "orderType": "STOP_LOSS",
  "quantity": 1,
  "stopPrice": 170.00
}
```

Stop-loss buys are rejected. A pending response contains `status: "PENDING"`, nullable execution fields, the trigger price, and a waiting status reason.

### `GET /api/orders`

Returns only the authenticated user’s orders in newest-first order.

### `GET /api/orders/pending`

Returns only the authenticated user’s pending orders.

### `DELETE /api/orders/{orderId}/cancel`

Cancels an owned pending order. Reserved cash or locked shares are released in the same transaction. Executed, cancelled, expired, or another user’s orders cannot be cancelled.

### `GET /api/trades`

Returns only the authenticated user’s executed trades. Sell trades include realized profit or loss based on the holding’s average buy price.

### `GET /api/portfolio/holdings`

Returns the authenticated user’s holdings with current quote, cost basis, current value, and unrealized profit or loss.

### `GET /api/portfolio/summary`

```json
{
  "success": true,
  "message": "Portfolio summary retrieved successfully",
  "data": {
    "availableCash": 99624.75,
    "reservedCash": 180.00,
    "totalCash": 99804.75,
    "holdingsValue": 195.25,
    "totalPortfolioValue": 100000.00,
    "totalInvested": 195.25,
    "unrealizedProfitLoss": 0.00,
    "unrealizedProfitLossPercent": 0.00,
    "numberOfHoldings": 1
  },
  "timestamp": "2026-06-24T16:00:00Z"
}
```

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

## Watchlist

All watchlist endpoints require a valid bearer token. The authenticated user is derived from the JWT principal.

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/watchlist` | Return the current user’s watchlist with latest quotes |
| `POST` | `/api/watchlist` | Add a normalized symbol |
| `DELETE` | `/api/watchlist/{symbol}` | Remove an owned symbol |

Request:

```json
{
  "symbol": "AAPL"
}
```

Response:

```json
{
  "success": true,
  "message": "Stock added to watchlist",
  "data": {
    "id": "00000000-0000-0000-0000-000000000000",
    "symbol": "AAPL",
    "companyName": "Apple Inc.",
    "exchange": "NASDAQ",
    "currency": "USD",
    "currentPrice": 195.25,
    "changeAmount": 1.45,
    "changePercent": 0.75,
    "latestTradingTime": "2026-06-24T16:00:00Z",
    "createdAt": "2026-06-24T10:00:00Z"
  },
  "timestamp": "2026-06-24T16:00:00Z"
}
```

## Live Price WebSocket

- Endpoint: `/ws`
- General STOMP topic: `/topic/prices`
- Symbol STOMP topic: `/topic/prices/{symbol}`

```json
{
  "symbol": "AAPL",
  "currentPrice": 195.31,
  "changeAmount": 1.51,
  "changePercent": 0.78,
  "latestTradingTime": "2026-06-24T16:00:05Z",
  "provider": "mock-live"
}
```

## Planned Resources

| Phase | Resource area | Example path |
| --- | --- | --- |
| Future | Analytics | `/api/analytics/portfolio` |

These future paths are architectural proposals and are not part of Phase 5.
