import { useEffect, useState } from 'react'

import { getMarketDataStatus, getSystemHealth } from '../../features/admin/adminService'
import type { AdminMarketDataStatus, AdminSystemHealth } from '../../types/admin'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatCurrency, formatDateTime } from '../../utils/formatters'
import { AdminHeader, Panel } from './AdminDashboardPage'

export function AdminSystemHealthPage() {
  const [health, setHealth] = useState<AdminSystemHealth | null>(null)
  const [market, setMarket] = useState<AdminMarketDataStatus | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([getSystemHealth(), getMarketDataStatus()])
      .then(([systemHealth, marketStatus]) => {
        setHealth(systemHealth)
        setMarket(marketStatus)
      })
      .catch((requestError) => setError(getApiErrorMessage(requestError, 'Unable to load system health')))
  }, [])

  if (error) return <div className="px-6 py-14 text-red-300">{error}</div>
  if (!health || !market) return <div className="grid min-h-[60vh] place-items-center text-slate-400">Loading system health...</div>

  return (
    <div className="mx-auto max-w-6xl px-6 py-14">
      <AdminHeader eyebrow="Platform monitoring" title="System Health" description="Current backend, database, scheduler, and provider status." />
      <section className="mt-8 grid gap-6 lg:grid-cols-2">
        <Panel title="Application">
          <HealthRow label="Backend" value={health.backendStatus} />
          <HealthRow label="Database" value={health.databaseStatus} />
          <HealthRow label="Application" value={health.applicationName} />
          <HealthRow label="Version" value={health.version} />
          <HealthRow label="Profile" value={health.activeProfile} />
          <HealthRow label="Current Time" value={formatDateTime(health.currentTime)} />
        </Panel>
        <Panel title="Scheduled Services">
          <HealthRow label="Live Price Stream" value={health.livePriceStreamEnabled ? 'Enabled' : 'Disabled'} />
          <HealthRow label="Pending Order Processor" value={health.pendingOrderProcessorEnabled ? 'Enabled' : 'Disabled'} />
          <HealthRow label="Portfolio Snapshots" value={health.portfolioSnapshotEnabled ? 'Enabled' : 'Disabled'} />
        </Panel>
        <Panel title="Market Data">
          <HealthRow label="Provider" value={market.provider} />
          <HealthRow label="Status" value={market.status} />
          <HealthRow label="Test Symbol" value={market.testSymbol} />
          <HealthRow label="Current Price" value={market.currentPrice === null ? 'Unavailable' : formatCurrency(market.currentPrice)} />
          <HealthRow label="Checked" value={formatDateTime(market.checkedAt)} />
        </Panel>
      </section>
    </div>
  )
}

function HealthRow({ label, value }: { label: string; value: string }) {
  return <div className="flex justify-between gap-4 border-b border-slate-800 py-3 last:border-0"><span className="text-slate-400">{label}</span><span className="font-semibold text-white">{value}</span></div>
}
