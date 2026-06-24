import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'

import { useLivePrices } from '../features/live/useLivePrices'
import {
  addToWatchlist,
  getWatchlist,
  removeFromWatchlist,
} from '../features/watchlist/watchlistService'
import type { WatchlistItem } from '../types/watchlist'
import { getApiErrorMessage } from '../utils/apiError'
import {
  formatCurrency,
  formatDateTime,
  formatNumber,
} from '../utils/formatters'

export function WatchlistPage() {
  const [items, setItems] = useState<WatchlistItem[]>([])
  const [symbol, setSymbol] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const livePrices = useLivePrices()

  useEffect(() => {
    getWatchlist()
      .then(setItems)
      .catch((requestError) =>
        setError(getApiErrorMessage(requestError, 'Unable to load watchlist')),
      )
      .finally(() => setIsLoading(false))
  }, [])

  const displayedItems = useMemo(
    () =>
      items.map((item) => {
        const livePrice = livePrices[item.symbol]
        return livePrice
          ? {
              ...item,
              currentPrice: livePrice.currentPrice,
              changeAmount: livePrice.changeAmount,
              changePercent: livePrice.changePercent,
              latestTradingTime: livePrice.latestTradingTime,
            }
          : item
      }),
    [items, livePrices],
  )

  async function handleAdd(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const normalizedSymbol = symbol.trim().toUpperCase()
    if (!normalizedSymbol) {
      setError('Enter a stock symbol')
      return
    }

    setError('')
    setMessage('')
    setIsSubmitting(true)
    try {
      const item = await addToWatchlist({ symbol: normalizedSymbol })
      setItems((current) => [
        item,
        ...current.filter((existing) => existing.symbol !== item.symbol),
      ])
      setSymbol('')
      setMessage(`${item.symbol} added to your watchlist`)
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, 'Unable to add stock to watchlist'),
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleRemove(itemSymbol: string) {
    setError('')
    setMessage('')
    try {
      await removeFromWatchlist(itemSymbol)
      setItems((current) =>
        current.filter((item) => item.symbol !== itemSymbol),
      )
      setMessage(`${itemSymbol} removed from your watchlist`)
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          'Unable to remove stock from watchlist',
        ),
      )
    }
  }

  return (
    <div className="mx-auto max-w-7xl px-6 py-14">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
            Personal watchlist
          </p>
          <h1 className="mt-3 text-4xl font-bold text-white">
            Track demo prices
          </h1>
          <p className="mt-3 text-slate-400">
            Prices update from an in-memory development stream.
          </p>
        </div>
        <span className="w-fit rounded-full border border-rocket-500/30 bg-rocket-500/10 px-3 py-1 text-xs font-semibold text-rocket-300">
          ● Live demo stream
        </span>
      </header>

      <form
        onSubmit={handleAdd}
        className="mt-8 flex flex-col gap-3 rounded-2xl border border-slate-800 bg-slate-900/60 p-5 sm:flex-row"
      >
        <input
          value={symbol}
          onChange={(event) => setSymbol(event.target.value)}
          placeholder="Enter symbol, e.g. AAPL"
          aria-label="Stock symbol"
          className="min-w-0 flex-1 rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 uppercase text-white outline-none focus:border-rocket-500"
        />
        <button
          type="submit"
          disabled={isSubmitting}
          className="rounded-xl bg-rocket-500 px-6 py-3 font-semibold text-slate-950 hover:bg-rocket-400 disabled:opacity-60"
        >
          {isSubmitting ? 'Adding...' : 'Add to Watchlist'}
        </button>
      </form>

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

      {isLoading ? (
        <p className="mt-10 text-slate-400">Loading watchlist...</p>
      ) : displayedItems.length === 0 ? (
        <div className="mt-8 rounded-2xl border border-dashed border-slate-700 px-6 py-14 text-center text-slate-500">
          Your watchlist is empty. Add a stock symbol to track live prices.
        </div>
      ) : (
        <div className="mt-8 overflow-x-auto rounded-2xl border border-slate-800">
          <table className="min-w-full divide-y divide-slate-800 bg-slate-900/60 text-left text-sm">
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                {['Symbol', 'Company', 'Exchange', 'Price', 'Change', 'Updated', 'Actions'].map(
                  (heading) => (
                    <th key={heading} className="px-5 py-4 font-medium">
                      {heading}
                    </th>
                  ),
                )}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {displayedItems.map((item) => {
                const isPositive = (item.changeAmount ?? 0) >= 0
                return (
                  <tr
                    key={item.id}
                    className={
                      livePrices[item.symbol] ? 'bg-rocket-500/[0.03]' : ''
                    }
                  >
                    <td className="px-5 py-4 font-bold text-rocket-400">
                      {item.symbol}
                    </td>
                    <td className="px-5 py-4 text-white">
                      {item.companyName}
                    </td>
                    <td className="px-5 py-4 text-slate-400">
                      {item.exchange ?? '—'}
                    </td>
                    <td className="px-5 py-4 font-semibold text-white">
                      {formatCurrency(item.currentPrice, item.currency ?? 'USD')}
                    </td>
                    <td
                      className={`px-5 py-4 ${isPositive ? 'text-rocket-400' : 'text-red-400'}`}
                    >
                      {isPositive ? '+' : ''}
                      {formatNumber(item.changeAmount ?? 0)} (
                      {isPositive ? '+' : ''}
                      {formatNumber(item.changePercent ?? 0)}%)
                    </td>
                    <td className="px-5 py-4 text-slate-400">
                      {formatDateTime(item.latestTradingTime)}
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex gap-3">
                        <Link
                          to={`/market/${encodeURIComponent(item.symbol)}`}
                          className="font-semibold text-rocket-400"
                        >
                          Details
                        </Link>
                        <button
                          type="button"
                          onClick={() => void handleRemove(item.symbol)}
                          className="font-semibold text-red-400"
                        >
                          Remove
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
