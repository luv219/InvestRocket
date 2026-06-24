import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { getTrades } from '../features/trades/tradeService'
import type { Trade } from '../types/trade'
import { getApiErrorMessage } from '../utils/apiError'
import { formatCurrency, formatDateTime } from '../utils/formatters'

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
      <h1 className="text-4xl font-bold text-white">Trade History</h1>
      {isLoading && <p className="mt-8 text-slate-400">Loading trades...</p>}
      {error && <p className="mt-8 text-red-300">{error}</p>}
      {!isLoading && !error && trades.length === 0 && <p className="mt-8 text-slate-400">No trades yet.</p>}
      {trades.length > 0 && (
        <div className="mt-8 overflow-x-auto rounded-2xl border border-slate-800">
          <table className="min-w-full divide-y divide-slate-800 text-left text-sm">
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                {['Symbol', 'Side', 'Quantity', 'Price', 'Trade Value', 'Realized P/L', 'Executed', 'Journal'].map((heading) => (
                  <th key={heading} className="px-4 py-3 font-medium">{heading}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {trades.map((trade) => (
                <tr key={trade.id}>
                  <td className="px-4 py-4 font-semibold text-rocket-400">{trade.symbol}</td>
                  <td className={`px-4 py-4 font-medium ${trade.side === 'BUY' ? 'text-rocket-400' : 'text-red-400'}`}>{trade.side}</td>
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
          </table>
        </div>
      )}
    </div>
  )
}
