# Demo Data Guide

## Demo Accounts

Use placeholders in public documentation and create credentials only in the deployment platform:

- User: `demo.user@example.com`
- Administrator: `demo.admin@example.com`
- Passwords: create unique strong values; never publish or commit them

For a public portfolio demo, prefer a resettable user account and rotate its password after recordings or interviews.

## Suggested Symbols

- `AAPL` — Apple
- `MSFT` — Microsoft
- `TSLA` — Tesla
- `NVDA` — NVIDIA
- `GOOGL` — Alphabet

These symbols are supported by the default mock provider and make the demonstration reproducible without an external API key.

## Recommended Demo State

Prepare the user account with:

- Two or three executed market buys
- One pending limit buy
- One pending stop-loss sell when holdings permit it
- Three watchlist symbols
- One active ABOVE alert and one triggered or cancelled alert
- At least one portfolio snapshot
- Two journal entries with different moods and strategies
- Several notifications and activity records

Keep enough virtual cash available to place a new order during the live walkthrough.

## Suggested Demo Flow

1. Register or log in.
2. Search `AAPL`.
3. Add `AAPL`, `MSFT`, and `NVDA` to the watchlist.
4. Place a small market buy.
5. Place a limit order that remains pending.
6. Create a price alert.
7. Add a journal note for the new trade.
8. Create or review an analytics snapshot.
9. Review notifications and activity.
10. Open the administrator dashboard and user management.

## Reset Strategy

Use the simulator reset action only when a clean wallet and empty holdings are required. Historical orders, trades, notifications, and audit records may remain, so create a fresh demo account when a completely clean presentation is preferred.

Invest Rocket is a virtual trading simulator for educational purposes only. It does not provide financial advice and does not execute real trades.
