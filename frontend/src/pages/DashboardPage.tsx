import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { SummaryCard } from '../components/SummaryCard'
import { useAuth } from '../features/auth/useAuth'
import { getPendingOrders } from '../features/orders/orderService'
import { getPortfolioSummary } from '../features/portfolio/portfolioService'
import { getWatchlist } from '../features/watchlist/watchlistService'
import type { PortfolioSummary } from '../types/portfolio'
import { getApiErrorMessage } from '../utils/apiError'
import { formatCurrency, formatNumber } from '../utils/formatters'

const quickLinks = [
  { to: '/market', label: 'Explore Market', description: 'Search stocks and place virtual market orders.' },
  { to: '/watchlist', label: 'Watchlist', description: 'Track selected symbols with live demo price updates.' },
  { to: '/portfolio', label: 'View Portfolio', description: 'Review holdings and current valuation.' },
  { to: '/orders', label: 'Orders', description: 'Inspect your simulated order history.' },
  { to: '/orders/pending', label: 'Pending Orders', description: 'Review or cancel advanced orders awaiting triggers.' },
  { to: '/trades', label: 'Trades', description: 'Review executions and realized profit or loss.' },
]

export function DashboardPage() {
  const { user, logout } = useAuth()
  const [summary, setSummary] = useState<PortfolioSummary | null>(null)
  const [pendingCount, setPendingCount] = useState<number | null>(null)
  const [watchlistCount, setWatchlistCount] = useState<number | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([getPortfolioSummary(), getPendingOrders(), getWatchlist()])
      .then(([summaryData, pendingOrders, watchlistItems]) => {
        setSummary(summaryData)
        setPendingCount(pendingOrders.length)
        setWatchlistCount(watchlistItems.length)
      })
      .catch((requestError) => setError(getApiErrorMessage(requestError, 'Unable to load portfolio summary')))
  }, [])

  return (
    <div className="mx-auto max-w-6xl px-6 py-14">
      <section className="flex flex-col gap-6 rounded-3xl border border-slate-800 bg-slate-900/70 p-8 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">Trading simulator</p>
          <h1 className="mt-3 text-3xl font-bold text-white">Welcome, {user?.fullName}</h1>
          <p className="mt-2 text-slate-400">{user?.email}</p>
        </div>
        <button type="button" onClick={logout} className="rounded-xl border border-slate-700 px-5 py-3 font-semibold text-white hover:border-slate-500 hover:bg-slate-800">
          Logout
        </button>
      </section>

      {error && <p className="mt-6 text-sm text-red-300">{error}</p>}

      <section className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-6">
        <SummaryCard label="Available Cash" value={summary ? formatCurrency(summary.availableCash) : error ? 'Unavailable' : 'Loading...'} />
        <SummaryCard label="Holdings Value" value={summary ? formatCurrency(summary.holdingsValue) : error ? 'Unavailable' : 'Loading...'} />
        <SummaryCard label="Total Portfolio Value" value={summary ? formatCurrency(summary.totalPortfolioValue) : error ? 'Unavailable' : 'Loading...'} />
        <SummaryCard label="Number of Holdings" value={summary ? formatNumber(summary.numberOfHoldings) : error ? 'Unavailable' : 'Loading...'} />
        <SummaryCard label="Pending Orders" value={pendingCount !== null ? formatNumber(pendingCount) : error ? 'Unavailable' : 'Loading...'} />
        <SummaryCard label="Watchlist Symbols" value={watchlistCount !== null ? formatNumber(watchlistCount) : error ? 'Unavailable' : 'Loading...'} />
      </section>

      <section className="mt-8 grid gap-4 sm:grid-cols-2">
        {quickLinks.map((link) => (
          <Link key={link.to} to={link.to} className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6 hover:-translate-y-0.5 hover:border-rocket-500/50">
            <h2 className="text-xl font-bold text-white">{link.label}</h2>
            <p className="mt-2 text-slate-400">{link.description}</p>
          </Link>
        ))}
      </section>

      <p className="mt-8 rounded-xl border border-amber-400/20 bg-amber-400/5 px-5 py-4 text-sm text-amber-200">
        Invest Rocket uses virtual funds only and does not provide financial advice.
      </p>
    </div>
  )
}
