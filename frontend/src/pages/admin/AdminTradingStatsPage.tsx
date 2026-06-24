import { useEffect, useState } from 'react'

import { SummaryCard } from '../../components/SummaryCard'
import { getAdminTradingStats } from '../../features/admin/adminService'
import type { AdminTradingStats } from '../../types/admin'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatCurrency, formatNumber } from '../../utils/formatters'
import { AdminHeader, Panel } from './AdminDashboardPage'

export function AdminTradingStatsPage() {
  const [stats, setStats] = useState<AdminTradingStats | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getAdminTradingStats()
      .then(setStats)
      .catch((requestError) =>
        setError(
          getApiErrorMessage(requestError, 'Unable to load trading statistics'),
        ),
      )
  }, [])

  if (error) return <div className="px-6 py-14 text-red-300">{error}</div>
  if (!stats) {
    return <div className="grid min-h-[60vh] place-items-center text-slate-400">Loading trading statistics...</div>
  }

  return (
    <div className="mx-auto max-w-7xl px-6 py-14">
      <AdminHeader
        eyebrow="Platform trading"
        title="Trading Statistics"
        description="Simulator-wide order composition and top activity."
      />
      <section className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <SummaryCard label="Buy Orders" value={formatNumber(stats.totalBuyOrders)} />
        <SummaryCard label="Sell Orders" value={formatNumber(stats.totalSellOrders)} />
        <SummaryCard label="Market Orders" value={formatNumber(stats.totalMarketOrders)} />
        <SummaryCard label="Limit Orders" value={formatNumber(stats.totalLimitOrders)} />
        <SummaryCard label="Stop-Loss Orders" value={formatNumber(stats.totalStopLossOrders)} />
      </section>
      <section className="mt-8 grid gap-6 lg:grid-cols-3">
        <Panel title="Most Traded Symbols">
          {stats.mostTradedSymbols.map((item) => (
            <Metric key={item.symbol} label={item.symbol} value={formatNumber(item.count)} />
          ))}
        </Panel>
        <Panel title="Top Users by Trades">
          {stats.topUsersByTradeCount.map((item) => (
            <Metric key={item.email} label={item.email} value={formatNumber(item.tradeCount)} />
          ))}
        </Panel>
        <Panel title="Top Users by Portfolio">
          {stats.topUsersByPortfolioValue.map((item) => (
            <Metric key={item.email} label={item.email} value={formatCurrency(item.portfolioValue)} />
          ))}
        </Panel>
      </section>
      <section className="mt-8 grid gap-6 lg:grid-cols-2">
        <Panel title="Recent Orders">
          {stats.recentOrders.map((order) => (
            <Metric
              key={order.id}
              label={`${order.userEmail} · ${order.symbol}`}
              value={`${order.side} ${order.status}`}
            />
          ))}
        </Panel>
        <Panel title="Recent Trades">
          {stats.recentTrades.map((trade) => (
            <Metric
              key={trade.id}
              label={`${trade.userEmail} · ${trade.symbol}`}
              value={formatCurrency(trade.tradeValue)}
            />
          ))}
        </Panel>
      </section>
    </div>
  )
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4 border-b border-slate-800 py-3 last:border-0">
      <span className="truncate text-slate-400">{label}</span>
      <span className="font-semibold text-white">{value}</span>
    </div>
  )
}
