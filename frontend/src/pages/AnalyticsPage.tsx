import { useEffect, useState, type ReactNode } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

import { SummaryCard } from '../components/SummaryCard'
import {
  createSnapshot,
  getAnalyticsOverview,
  getHoldingPerformance,
} from '../features/analytics/analyticsService'
import type {
  HoldingPerformance,
  PortfolioAnalytics,
} from '../types/analytics'
import { getApiErrorMessage } from '../utils/apiError'
import {
  formatCurrency,
  formatDateTime,
  formatNumber,
} from '../utils/formatters'

const allocationColors = [
  '#10ad6d',
  '#38bdf8',
  '#a78bfa',
  '#f59e0b',
  '#f472b6',
  '#fb7185',
]

export function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<PortfolioAnalytics | null>(null)
  const [holdings, setHoldings] = useState<HoldingPerformance[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isCreating, setIsCreating] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function loadAnalytics() {
    setError('')
    try {
      const [overview, holdingPerformance] = await Promise.all([
        getAnalyticsOverview(),
        getHoldingPerformance(),
      ])
      setAnalytics(overview)
      setHoldings(holdingPerformance)
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, 'Unable to load portfolio analytics'),
      )
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    Promise.all([getAnalyticsOverview(), getHoldingPerformance()])
      .then(([overview, holdingPerformance]) => {
        setAnalytics(overview)
        setHoldings(holdingPerformance)
      })
      .catch((requestError) => {
        setError(
          getApiErrorMessage(
            requestError,
            'Unable to load portfolio analytics',
          ),
        )
      })
      .finally(() => setIsLoading(false))
  }, [])

  async function handleCreateSnapshot() {
    setIsCreating(true)
    setError('')
    setMessage('')
    try {
      await createSnapshot()
      setMessage('Portfolio snapshot created')
      await loadAnalytics()
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, 'Unable to create portfolio snapshot'),
      )
    } finally {
      setIsCreating(false)
    }
  }

  if (isLoading) {
    return (
      <div className="grid min-h-[60vh] place-items-center text-slate-400">
        Loading analytics...
      </div>
    )
  }

  if (error && !analytics) {
    return (
      <div className="mx-auto max-w-6xl px-6 py-14 text-red-300">{error}</div>
    )
  }

  if (!analytics) return null

  const hasAnalytics =
    analytics.performanceHistory.length > 0 ||
    analytics.allocation.length > 0 ||
    analytics.tradingStats.totalTrades > 0
  const returnClass =
    analytics.totalReturnPercent >= 0 ? 'text-rocket-400' : 'text-red-400'
  const profitClass =
    analytics.totalProfitLoss >= 0 ? 'text-rocket-400' : 'text-red-400'

  return (
    <div className="mx-auto max-w-7xl px-6 py-14">
      <header className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
            Portfolio intelligence
          </p>
          <h1 className="mt-3 text-4xl font-bold text-white">
            Performance Analytics
          </h1>
          <p className="mt-3 text-slate-400">
            Backend-calculated metrics for simulated holdings and trades.
          </p>
        </div>
        <button
          type="button"
          onClick={() => void handleCreateSnapshot()}
          disabled={isCreating}
          className="rounded-xl bg-rocket-500 px-5 py-3 font-semibold text-slate-950 hover:bg-rocket-400 disabled:opacity-60"
        >
          {isCreating ? 'Creating...' : 'Create Snapshot Now'}
        </button>
      </header>

      {error && (
        <p className="mt-5 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-300">
          {error}
        </p>
      )}
      {message && (
        <p className="mt-5 rounded-xl border border-rocket-500/30 bg-rocket-500/10 px-4 py-3 text-sm text-rocket-300">
          {message}
        </p>
      )}

      <section className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <SummaryCard
          label="Current Portfolio Value"
          value={formatCurrency(analytics.currentPortfolioValue)}
        />
        <SummaryCard
          label="Total P/L"
          value={formatCurrency(analytics.totalProfitLoss)}
          valueClassName={profitClass}
        />
        <SummaryCard
          label="Total Return"
          value={`${analytics.totalReturnPercent.toFixed(2)}%`}
          valueClassName={returnClass}
        />
        <SummaryCard
          label="Realized P/L"
          value={formatCurrency(analytics.realizedProfitLoss)}
        />
        <SummaryCard
          label="Unrealized P/L"
          value={formatCurrency(analytics.unrealizedProfitLoss)}
        />
        <SummaryCard
          label="Cash Balance"
          value={formatCurrency(analytics.cashBalance)}
        />
        <SummaryCard
          label="Reserved Cash"
          value={formatCurrency(analytics.reservedCash)}
        />
        <SummaryCard
          label="Holdings Value"
          value={formatCurrency(analytics.holdingsValue)}
        />
      </section>

      {!hasAnalytics && (
        <div className="mt-8 rounded-2xl border border-dashed border-slate-700 px-6 py-12 text-center text-slate-400">
          No analytics yet. Place your first virtual trade or create a snapshot.
        </div>
      )}

      <section className="mt-8 grid gap-6 lg:grid-cols-2">
        <ChartPanel title="Portfolio Value Over Time">
          {analytics.performanceHistory.length === 0 ? (
            <ChartEmpty />
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={analytics.performanceHistory}>
                <CartesianGrid stroke="#1e293b" strokeDasharray="3 3" />
                <XAxis dataKey="date" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" />
                <Tooltip />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="totalPortfolioValue"
                  name="Portfolio Value"
                  stroke="#29c985"
                  strokeWidth={2}
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          )}
        </ChartPanel>

        <ChartPanel title="Allocation by Stock">
          {holdings.length === 0 ? (
            <ChartEmpty />
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={analytics.allocation}
                  dataKey="currentValue"
                  nameKey="symbol"
                  innerRadius={60}
                  outerRadius={100}
                  paddingAngle={2}
                >
                  {analytics.allocation.map((item, index) => (
                    <Cell
                      key={item.symbol}
                      fill={allocationColors[index % allocationColors.length]}
                    />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </ChartPanel>

        <ChartPanel title="Unrealized P/L by Holding">
          {analytics.allocation.length === 0 ? (
            <ChartEmpty />
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={holdings}>
                <CartesianGrid stroke="#1e293b" strokeDasharray="3 3" />
                <XAxis dataKey="symbol" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" />
                <Tooltip />
                <Bar dataKey="unrealizedProfitLoss" fill="#38bdf8" />
              </BarChart>
            </ResponsiveContainer>
          )}
        </ChartPanel>

        <ChartPanel title="Trading Statistics">
          <dl className="grid grid-cols-2 gap-4 text-sm sm:grid-cols-3">
            {[
              ['Total Trades', analytics.tradingStats.totalTrades],
              ['Buy Trades', analytics.tradingStats.buyTrades],
              ['Sell Trades', analytics.tradingStats.sellTrades],
              ['Total Orders', analytics.tradingStats.totalOrders],
              ['Executed', analytics.tradingStats.executedOrders],
              ['Pending', analytics.tradingStats.pendingOrders],
              ['Cancelled', analytics.tradingStats.cancelledOrders],
              ['Winning Sells', analytics.tradingStats.winningSellTrades],
              ['Losing Sells', analytics.tradingStats.losingSellTrades],
              [
                'Win Rate',
                `${analytics.tradingStats.winRatePercent.toFixed(2)}%`,
              ],
            ].map(([label, value]) => (
              <div key={label} className="rounded-xl bg-slate-950/60 p-4">
                <dt className="text-slate-500">{label}</dt>
                <dd className="mt-2 text-lg font-bold text-white">{value}</dd>
              </div>
            ))}
          </dl>
        </ChartPanel>
      </section>

      <section className="mt-8 grid gap-5 md:grid-cols-2">
        <HoldingHighlight title="Best Holding" holding={analytics.bestHolding} />
        <HoldingHighlight
          title="Worst Holding"
          holding={analytics.worstHolding}
        />
      </section>

      <section className="mt-8 overflow-x-auto rounded-2xl border border-slate-800">
        <table className="min-w-full divide-y divide-slate-800 text-left text-sm">
          <thead className="bg-slate-900 text-slate-400">
            <tr>
              {[
                'Symbol',
                'Company',
                'Quantity',
                'Average Buy',
                'Current Price',
                'Current Value',
                'Invested',
                'Unrealized P/L',
              ].map((heading) => (
                <th key={heading} className="px-4 py-3 font-medium">
                  {heading}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800 bg-slate-950/40">
            {holdings.map((holding) => (
              <tr key={holding.symbol}>
                <td className="px-4 py-4 font-semibold text-rocket-400">
                  {holding.symbol}
                </td>
                <td className="px-4 py-4 text-white">
                  {holding.companyName}
                </td>
                <td className="px-4 py-4">
                  {formatNumber(holding.quantity)}
                </td>
                <td className="px-4 py-4">
                  {formatCurrency(holding.averageBuyPrice)}
                </td>
                <td className="px-4 py-4">
                  {formatCurrency(holding.currentPrice)}
                </td>
                <td className="px-4 py-4">
                  {formatCurrency(holding.currentValue)}
                </td>
                <td className="px-4 py-4">
                  {formatCurrency(holding.totalInvested)}
                </td>
                <td className="px-4 py-4">
                  {formatCurrency(holding.unrealizedProfitLoss)} (
                  {holding.unrealizedProfitLossPercent.toFixed(2)}%)
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {analytics.performanceHistory.length > 0 && (
          <p className="border-t border-slate-800 px-4 py-3 text-xs text-slate-500">
            Latest snapshot:{' '}
            {formatDateTime(
              analytics.performanceHistory.at(-1)?.snapshotTime ?? '',
            )}
          </p>
        )}
      </section>
    </div>
  )
}

function ChartPanel({
  title,
  children,
}: {
  title: string
  children: ReactNode
}) {
  return (
    <article className="rounded-2xl border border-slate-800 bg-slate-900/60 p-5">
      <h2 className="text-lg font-bold text-white">{title}</h2>
      <div className="mt-5">{children}</div>
    </article>
  )
}

function ChartEmpty() {
  return (
    <div className="grid h-[300px] place-items-center text-sm text-slate-500">
      Create a snapshot or add holdings to populate this chart.
    </div>
  )
}

function HoldingHighlight({
  title,
  holding,
}: {
  title: string
  holding: PortfolioAnalytics['bestHolding']
}) {
  return (
    <article className="rounded-2xl border border-slate-800 bg-slate-900/60 p-5">
      <p className="text-sm text-slate-500">{title}</p>
      {holding ? (
        <>
          <h2 className="mt-2 text-xl font-bold text-white">
            {holding.symbol} · {holding.companyName}
          </h2>
          <p
            className={`mt-3 font-semibold ${
              holding.unrealizedProfitLoss >= 0
                ? 'text-rocket-400'
                : 'text-red-400'
            }`}
          >
            {formatCurrency(holding.unrealizedProfitLoss)} (
            {holding.unrealizedProfitLossPercent.toFixed(2)}%)
          </p>
        </>
      ) : (
        <p className="mt-3 text-slate-500">No holding data available.</p>
      )}
    </article>
  )
}
