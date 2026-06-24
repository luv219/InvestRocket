import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { SummaryCard } from '../../components/SummaryCard'
import {
  getAdminAuditLogs,
  getAdminDashboard,
  getMarketDataStatus,
  getSystemHealth,
} from '../../features/admin/adminService'
import type {
  AdminAuditLog,
  AdminDashboardStats,
  AdminMarketDataStatus,
  AdminSystemHealth,
} from '../../types/admin'
import { getApiErrorMessage } from '../../utils/apiError'
import {
  formatCurrency,
  formatDateTime,
  formatNumber,
} from '../../utils/formatters'

export function AdminDashboardPage() {
  const [stats, setStats] = useState<AdminDashboardStats | null>(null)
  const [health, setHealth] = useState<AdminSystemHealth | null>(null)
  const [market, setMarket] = useState<AdminMarketDataStatus | null>(null)
  const [logs, setLogs] = useState<AdminAuditLog[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([
      getAdminDashboard(),
      getSystemHealth(),
      getMarketDataStatus(),
      getAdminAuditLogs(),
    ])
      .then(([dashboard, systemHealth, marketStatus, auditLogs]) => {
        setStats(dashboard)
        setHealth(systemHealth)
        setMarket(marketStatus)
        setLogs(auditLogs.slice(0, 8))
      })
      .catch((requestError) =>
        setError(
          getApiErrorMessage(requestError, 'Unable to load admin dashboard'),
        ),
      )
  }, [])

  if (error) {
    return <div className="mx-auto max-w-7xl px-6 py-14 text-red-300">{error}</div>
  }
  if (!stats || !health || !market) {
    return (
      <div className="grid min-h-[60vh] place-items-center text-slate-400">
        Loading admin dashboard...
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-7xl px-6 py-14">
      <AdminHeader
        eyebrow="Platform administration"
        title="Admin Dashboard"
        description="Simulator-wide users, trading activity, and service health."
      />
      <section className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <SummaryCard label="Total Users" value={formatNumber(stats.totalUsers)} />
        <SummaryCard label="Enabled Users" value={formatNumber(stats.enabledUsers)} />
        <SummaryCard label="Disabled Users" value={formatNumber(stats.disabledUsers)} />
        <SummaryCard label="Total Orders" value={formatNumber(stats.totalOrders)} />
        <SummaryCard label="Pending Orders" value={formatNumber(stats.pendingOrders)} />
        <SummaryCard label="Executed Orders" value={formatNumber(stats.executedOrders)} />
        <SummaryCard label="Total Trades" value={formatNumber(stats.totalTrades)} />
        <SummaryCard
          label="Platform Portfolio Value"
          value={formatCurrency(stats.totalPlatformPortfolioValue)}
        />
      </section>

      <section className="mt-8 grid gap-6 lg:grid-cols-2">
        <Panel title="System Health">
          <StatusRow label="Backend" value={health.backendStatus} />
          <StatusRow label="Database" value={health.databaseStatus} />
          <StatusRow label="Active Profile" value={health.activeProfile} />
          <StatusRow label="Application" value={health.applicationName} />
        </Panel>
        <Panel title="Market Data Provider">
          <StatusRow label="Provider" value={market.provider} />
          <StatusRow label="Status" value={market.status} />
          <StatusRow label="Test Symbol" value={market.testSymbol} />
          <StatusRow
            label="Test Price"
            value={
              market.currentPrice === null
                ? 'Unavailable'
                : formatCurrency(market.currentPrice)
            }
          />
        </Panel>
      </section>

      <section className="mt-8 grid gap-4 sm:grid-cols-3">
        {[
          ['/admin/users', 'Users'],
          ['/admin/trading-stats', 'Trading Stats'],
          ['/admin/audit-logs', 'Audit Logs'],
        ].map(([to, label]) => (
          <Link
            key={to}
            to={to}
            className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6 text-xl font-bold text-white hover:border-rocket-500/50"
          >
            {label} →
          </Link>
        ))}
      </section>

      <Panel title="Recent Audit Logs" className="mt-8">
        {logs.length === 0 ? (
          <p className="text-slate-500">No audit activity available.</p>
        ) : (
          <div className="space-y-3">
            {logs.map((log) => (
              <div
                key={log.id}
                className="flex flex-col justify-between gap-2 rounded-xl bg-slate-950/60 p-4 sm:flex-row"
              >
                <div>
                  <p className="font-semibold text-white">
                    {log.action.replaceAll('_', ' ')}
                  </p>
                  <p className="text-sm text-slate-400">
                    {log.userEmail} · {log.description}
                  </p>
                </div>
                <time className="text-xs text-slate-500">
                  {formatDateTime(log.createdAt)}
                </time>
              </div>
            ))}
          </div>
        )}
      </Panel>
    </div>
  )
}

export function AdminHeader({
  eyebrow,
  title,
  description,
}: {
  eyebrow: string
  title: string
  description: string
}) {
  return (
    <header>
      <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
        {eyebrow}
      </p>
      <h1 className="mt-3 text-4xl font-bold text-white">{title}</h1>
      <p className="mt-3 text-slate-400">{description}</p>
    </header>
  )
}

export function Panel({
  title,
  children,
  className = '',
}: {
  title: string
  children: React.ReactNode
  className?: string
}) {
  return (
    <article
      className={`rounded-2xl border border-slate-800 bg-slate-900/60 p-6 ${className}`}
    >
      <h2 className="text-xl font-bold text-white">{title}</h2>
      <div className="mt-5">{children}</div>
    </article>
  )
}

function StatusRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between border-b border-slate-800 py-3 last:border-0">
      <span className="text-slate-400">{label}</span>
      <span className="font-semibold text-white">{value}</span>
    </div>
  )
}
