import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { SummaryCard } from '../components/SummaryCard'
import { Alert } from '../components/ui/Alert'
import { EmptyState } from '../components/ui/EmptyState'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'
import { PageHeader } from '../components/ui/PageHeader'
import { Table, TableContainer } from '../components/ui/Table'
import { getHoldings, getPortfolioSummary } from '../features/portfolio/portfolioService'
import type { Holding, PortfolioSummary } from '../types/portfolio'
import { getApiErrorMessage } from '../utils/apiError'
import { formatCurrency, formatNumber, formatPercent } from '../utils/formatters'

export function PortfolioPage() {
  const [summary, setSummary] = useState<PortfolioSummary | null>(null)
  const [holdings, setHoldings] = useState<Holding[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function loadPortfolio() {
      try {
        const [summaryData, holdingsData] = await Promise.all([
          getPortfolioSummary(),
          getHoldings(),
        ])
        setSummary(summaryData)
        setHoldings(holdingsData)
      } catch (requestError) {
        setError(getApiErrorMessage(requestError, 'Unable to load portfolio'))
      } finally {
        setIsLoading(false)
      }
    }

    void loadPortfolio()
  }, [])

  if (isLoading) {
    return <LoadingSpinner label="Loading portfolio..." />
  }

  if (error || !summary) {
    return <div className="mx-auto max-w-6xl px-6 py-14"><Alert tone="error">{error || 'Portfolio unavailable'}</Alert></div>
  }

  const profitClass = summary.unrealizedProfitLoss >= 0 ? 'text-rocket-400' : 'text-red-400'

  return (
    <div className="mx-auto max-w-7xl px-6 py-14">
      <PageHeader
        eyebrow="Virtual portfolio"
        title="Portfolio"
        description="Review available cash, reserved funds, holdings, and unrealized performance."
        actions={<Link to="/analytics" className="inline-flex rounded-xl bg-rocket-500 px-5 py-3 font-semibold text-slate-950 hover:bg-rocket-400">View Analytics</Link>}
      />

      <section className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <SummaryCard label="Available Cash" value={formatCurrency(summary.availableCash)} />
        <SummaryCard label="Reserved Cash" value={formatCurrency(summary.reservedCash)} />
        <SummaryCard label="Total Cash" value={formatCurrency(summary.totalCash)} />
        <SummaryCard label="Holdings Value" value={formatCurrency(summary.holdingsValue)} />
        <SummaryCard label="Total Portfolio Value" value={formatCurrency(summary.totalPortfolioValue)} />
        <SummaryCard label="Total Invested" value={formatCurrency(summary.totalInvested)} />
        <SummaryCard
          label="Unrealized P/L"
          value={`${formatCurrency(summary.unrealizedProfitLoss)} (${formatPercent(summary.unrealizedProfitLossPercent)})`}
          valueClassName={profitClass}
        />
        <SummaryCard label="Number of Holdings" value={formatNumber(summary.numberOfHoldings)} />
      </section>

      {holdings.length === 0 ? (
        <div className="mt-8">
          <EmptyState
            title="No holdings yet"
            description="Search the market and place your first virtual trade to build a portfolio."
            action={<Link to="/market" className="inline-flex rounded-xl bg-rocket-500 px-5 py-3 font-semibold text-slate-950">Explore Market</Link>}
          />
        </div>
      ) : (
        <TableContainer className="mt-8">
          <Table>
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                {['Symbol', 'Company', 'Total Qty', 'Locked', 'Available', 'Avg Buy', 'Current', 'Invested', 'Value', 'Unrealized P/L'].map((heading) => (
                  <th key={heading} scope="col" className="whitespace-nowrap px-4 py-3 font-medium">{heading}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800 bg-slate-950/40">
              {holdings.map((holding) => (
                <tr key={holding.symbol}>
                  <td className="px-4 py-4 font-semibold text-rocket-400">{holding.symbol}</td>
                  <td className="px-4 py-4 text-white">{holding.companyName}</td>
                  <td className="px-4 py-4 text-slate-300">{holding.quantity}</td>
                  <td className="px-4 py-4 text-amber-300">{holding.lockedQuantity}</td>
                  <td className="px-4 py-4 text-slate-300">{holding.availableQuantity}</td>
                  <td className="px-4 py-4 text-slate-300">{formatCurrency(holding.averageBuyPrice)}</td>
                  <td className="px-4 py-4 text-slate-300">{formatCurrency(holding.currentPrice)}</td>
                  <td className="px-4 py-4 text-slate-300">{formatCurrency(holding.totalInvested)}</td>
                  <td className="px-4 py-4 text-slate-300">{formatCurrency(holding.currentValue)}</td>
                  <td className={`px-4 py-4 font-medium ${holding.unrealizedProfitLoss >= 0 ? 'text-rocket-400' : 'text-red-400'}`}>
                    {formatCurrency(holding.unrealizedProfitLoss)} ({formatPercent(holding.unrealizedProfitLossPercent)})
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </TableContainer>
      )}
    </div>
  )
}
