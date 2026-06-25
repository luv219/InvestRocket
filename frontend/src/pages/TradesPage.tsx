import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { getTrades } from '../features/trades/tradeService'
import { Alert } from '../components/ui/Alert'
import { Badge } from '../components/ui/Badge'
import { EmptyState } from '../components/ui/EmptyState'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'
import { PageHeader } from '../components/ui/PageHeader'
import { Table, TableContainer } from '../components/ui/Table'
import type { Trade } from '../types/trade'
import { getApiErrorMessage } from '../utils/apiError'
import { formatCurrency, formatDateTime, formatOrderSide } from '../utils/formatters'

export function TradesPage() {
  const [trades, setTrades] = useState<Trade[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getTrades()
      .then(setTrades)
      .catch((requestError) => setError(getApiErrorMessage(requestError, 'Unable to load trades')))
      .finally(() => setIsLoading(false))
  }, [])

  return (
    <div className="mx-auto max-w-7xl px-6 py-14">
      <PageHeader eyebrow="Executed orders" title="Trade History" description="Review completed virtual executions and realized profit or loss." />
      {isLoading && <LoadingSpinner label="Loading trades..." />}
      {error && <div className="mt-8"><Alert tone="error">{error}</Alert></div>}
      {!isLoading && !error && trades.length === 0 && <div className="mt-8"><EmptyState title="No trades yet" description="Executed virtual orders will appear here." /></div>}
      {trades.length > 0 && (
        <TableContainer className="mt-8">
          <Table>
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                {['Symbol', 'Side', 'Quantity', 'Price', 'Trade Value', 'Realized P/L', 'Executed', 'Journal'].map((heading) => (
                  <th key={heading} scope="col" className="whitespace-nowrap px-4 py-3 font-medium">{heading}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {trades.map((trade) => (
                <tr key={trade.id}>
                  <td className="px-4 py-4 font-semibold text-rocket-400">{trade.symbol}</td>
                  <td className="px-4 py-4"><Badge tone={trade.side === 'BUY' ? 'success' : 'danger'}>{formatOrderSide(trade.side)}</Badge></td>
                  <td className="px-4 py-4 text-slate-300">{trade.quantity}</td>
                  <td className="px-4 py-4 text-slate-300">{formatCurrency(trade.price)}</td>
                  <td className="px-4 py-4 text-slate-300">{formatCurrency(trade.tradeValue)}</td>
                  <td className={`px-4 py-4 font-medium ${trade.realizedProfitLoss >= 0 ? 'text-rocket-400' : 'text-red-400'}`}>
                    {formatCurrency(trade.realizedProfitLoss)}
                  </td>
                  <td className="px-4 py-4 text-slate-400">{formatDateTime(trade.executedAt)}</td>
                  <td className="px-4 py-4">
                    <Link to={`/journal?symbol=${encodeURIComponent(trade.symbol)}&tradeId=${encodeURIComponent(trade.id)}`} className="font-semibold text-rocket-400">
                      Add Note
                    </Link>
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
