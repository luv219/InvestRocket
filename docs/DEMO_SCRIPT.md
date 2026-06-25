# Demo Script

## 5–7 Minute Portfolio Presentation

### 1. Opening and Problem — 30 seconds

“Invest Rocket is a full-stack virtual stock trading simulator. It addresses a common learning problem: people want to understand market workflows, orders, portfolios, and risk controls without connecting a brokerage account or risking real money.”

State the boundary clearly: Invest Rocket is educational software, not financial advice, and it never executes real trades.

### 2. Solution Overview — 30 seconds

Explain that users receive a virtual `$100,000` wallet, search stocks, place simulated orders, track holdings, analyze performance, create alerts, and record decisions in a trading journal. Administrators can monitor users, activity, and platform health.

### 3. Register and Authenticate — 30 seconds

Show registration or login. Mention:

- BCrypt password hashing
- Stateless JWT authentication
- Protected user routes
- Backend-enforced ADMIN authorization

### 4. Search and Trade — 75 seconds

1. Search for `AAPL`.
2. Open the stock detail page.
3. Show live demo WebSocket price updates.
4. Add the symbol to the watchlist.
5. Place a small market buy.
6. Show a pending limit order or stop-loss example.

Explain that wallet, holdings, order, and trade changes are transactional and use fixed-precision decimal values.

### 5. Portfolio and Analytics — 60 seconds

Open the portfolio to show available cash, reserved cash, holdings, valuation, and unrealized profit or loss. Open analytics to demonstrate allocation, return metrics, performance history, and trading statistics.

### 6. Alerts, Notifications, and Journal — 60 seconds

Create an ABOVE or BELOW price alert. Open notifications to show order and alert events. Add a journal entry linked to a symbol, order, or trade and explain how it supports reflection rather than automated recommendations.

### 7. Admin and System Design — 60 seconds

Open the admin dashboard and show:

- User management
- Platform order/trade statistics
- Audit visibility
- Database and provider health

Summarize the architecture:

- React, TypeScript, Tailwind CSS, Recharts
- Java 21, Spring Boot, Security, JPA, Flyway
- Neon PostgreSQL
- Mock/Finnhub provider abstraction
- STOMP WebSockets and scheduled processors
- JUnit, MockMvc, Vitest, Docker, and GitHub Actions

### 8. Security and Compliance Boundary — 30 seconds

Mention exact-origin CORS, backend-only API keys, password secrecy, ownership checks, disabled-by-default admin bootstrap, and sanitized errors. Reiterate that no real-money trading, brokerage integration, payments, KYC, or investment recommendations are included.

### 9. Future Scope and Close — 30 seconds

Potential future work includes secure cookie-based sessions, refresh-token rotation, rate limiting, Redis-backed scaling, browser end-to-end tests, and stronger observability.

Close with: “Invest Rocket demonstrates production-minded full-stack architecture while keeping the financial workflow safe, simulated, and educational.”
