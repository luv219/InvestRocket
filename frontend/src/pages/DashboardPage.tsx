import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { Alert } from '../components/ui/Alert'
import { Card } from '../components/ui/Card'
import { EmptyState } from '../components/ui/EmptyState'
import { PageHeader } from '../components/ui/PageHeader'
import { StatCard } from '../components/ui/StatCard'
import { getActiveAlerts } from '../features/alerts/priceAlertService'
import { getAnalyticsOverview } from '../features/analytics/analyticsService'
import { useAuth } from '../features/auth/useAuth'
import { getNotificationSummary } from '../features/notifications/notificationService'
import { getPendingOrders } from '../features/orders/orderService'
import { getWatchlist } from '../features/watchlist/watchlistService'
import type { PortfolioAnalytics } from '../types/analytics'
import { getApiErrorMessage } from '../utils/apiError'
import {
  formatCurrency,
  formatNumber,
  formatPercent,
} from '../utils/formatters'

const quickActions = [
  { to: '/market', label: 'Search Market', description: 'Find a stock and place a virtual order.', icon: '⌕' },
  { to: '/portfolio', label: 'View Portfolio', description: 'Review positions and current valuation.', icon: '◫' },
  { to: '/analytics', label: 'View Analytics', description: 'Inspect returns, allocation, and performance.', icon: '↗' },
  { to: '/watchlist', label: 'Open Watchlist', description: 'Track live demo prices for selected stocks.', icon: '☆' },
  { to: '/journal', label: 'Create Journal Entry', description: 'Record your plan, mood, and lessons.', icon: '✎' },
]

export function DashboardPage() {
  const { user } = useAuth()
  const [analytics, setAnalytics] = useState<PortfolioAnalytics | null>(null)
  const [pendingCount, setPendingCount] = useState<number | null>(null)
  const [watchlistCount, setWatchlistCount] = useState<number | null>(null)
  const [activeAlertCount, setActiveAlertCount] = useState<number | null>(null)
  const [unreadCount, setUnreadCount] = useState<number | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([
      getAnalyticsOverview(),
      getPendingOrders(),
      getWatchlist(),
      getActiveAlerts(),
      getNotificationSummary(),
    ])
      .then(([analyticsData, pendingOrders, watchlistItems, alerts, notifications]) => {
        setAnalytics(analyticsData)
        setPendingCount(pendingOrders.length)
        setWatchlistCount(watchlistItems.length)
        setActiveAlertCount(alerts.length)
        setUnreadCount(notifications.unreadCount)
      })
      .catch((requestError) =>
        setError(getApiErrorMessage(requestError, 'Unable to load dashboard summary')),
      )
  }, [])

  const loadingValue = error ? 'Unavailable' : 'Loading...'
  const hasNoTrades = analytics?.tradingStats.totalTrades === 0

  return (
    <div className="mx-auto max-w-7xl px-5 py-10 sm:px-6 sm:py-14">
      <PageHeader
        eyebrow="Trading simulator"
        title={`Welcome back, ${user?.fullName ?? 'Investor'}`}
        description="Review your virtual portfolio, monitor activity, and choose your next learning step."
        actions={
          <span className="rounded-full border border-slate-700 bg-slate-900 px-4 py-2 text-sm text-slate-300">
            {user?.email}
          </span>
        }
      />

      {error && (
        <div className="mt-6">
          <Alert tone="error">{error}</Alert>
        </div>
      )}

      {hasNoTrades && (
        <div className="mt-8">
          <EmptyState
            title="Place your first virtual trade"
            description="Start by searching a stock and placing your first virtual trade. Your portfolio and analytics will update automatically."
            action={
              <Link to="/market" className="inline-flex rounded-xl bg-rocket-500 px-5 py-3 font-semibold text-slate-950 hover:bg-rocket-400">
                Search the Market
              </Link>
            }
          />
        </div>
      )}

      <section aria-label="Portfolio summary" className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Portfolio Value" value={analytics ? formatCurrency(analytics.currentPortfolioValue) : loadingValue} />
        <StatCard label="Cash Balance" value={analytics ? formatCurrency(analytics.cashBalance) : loadingValue} />
        <StatCard label="Holdings Value" value={analytics ? formatCurrency(analytics.holdingsValue) : loadingValue} />
        <StatCard
          label="Total P/L"
          value={analytics ? formatCurrency(analytics.totalProfitLoss) : loadingValue}
          valueClassName={analytics && analytics.totalProfitLoss < 0 ? 'text-red-300' : 'text-rocket-300'}
          helper={analytics ? `${formatPercent(analytics.totalReturnPercent)} total return` : undefined}
        />
        <StatCard label="Pending Orders" value={pendingCount === null ? loadingValue : formatNumber(pendingCount)} />
        <StatCard label="Active Alerts" value={activeAlertCount === null ? loadingValue : formatNumber(activeAlertCount)} />
        <StatCard label="Unread Notifications" value={unreadCount === null ? loadingValue : formatNumber(unreadCount)} />
        <StatCard label="Watchlist Symbols" value={watchlistCount === null ? loadingValue : formatNumber(watchlistCount)} />
      </section>

      {watchlistCount === 0 && (
        <Card className="mt-6 flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="font-semibold text-white">Build your watchlist</h2>
            <p className="mt-1 text-sm text-slate-400">
              Add stocks to your watchlist to track live demo prices.
            </p>
          </div>
          <Link to="/watchlist" className="font-semibold text-rocket-400 hover:text-rocket-300">
            Open Watchlist →
          </Link>
        </Card>
      )}

      {analytics && analytics.performanceHistory.length === 0 && (
        <Card className="mt-4 flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="font-semibold text-white">Performance history is empty</h2>
            <p className="mt-1 text-sm text-slate-400">
              Create a snapshot after placing trades to view performance history.
            </p>
          </div>
          <Link to="/analytics" className="font-semibold text-rocket-400 hover:text-rocket-300">
            View Analytics →
          </Link>
        </Card>
      )}

      <section className="mt-10">
        <h2 className="text-2xl font-bold text-white">Quick actions</h2>
        <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
          {quickActions.map((action) => (
            <Link
              key={action.to}
              to={action.to}
              className="group rounded-2xl border border-slate-800 bg-slate-900/60 p-5 hover:-translate-y-0.5 hover:border-rocket-500/50"
            >
              <span aria-hidden="true" className="grid size-10 place-items-center rounded-xl bg-rocket-500/10 text-xl text-rocket-300">
                {action.icon}
              </span>
              <h3 className="mt-4 font-bold text-white group-hover:text-rocket-300">
                {action.label}
              </h3>
              <p className="mt-2 text-sm leading-6 text-slate-400">
                {action.description}
              </p>
            </Link>
          ))}
        </div>
      </section>

      <div className="mt-8">
        <Alert tone="warning">
          Invest Rocket is a virtual trading simulator for educational purposes
          only. It does not provide financial advice and does not execute real
          trades.
        </Alert>
      </div>
    </div>
  )
}
