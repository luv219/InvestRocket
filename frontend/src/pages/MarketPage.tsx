import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'

import { getMarketErrorMessage } from '../features/market/marketErrors'
import { searchStocks } from '../features/market/marketService'
import { addToWatchlist } from '../features/watchlist/watchlistService'
import type { StockSearchResult } from '../types/market'
import { getApiErrorMessage } from '../utils/apiError'

export function MarketPage() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<StockSearchResult[]>([])
  const [hasSearched, setHasSearched] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [addingSymbol, setAddingSymbol] = useState('')

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const normalizedQuery = query.trim()
    if (!normalizedQuery) {
      setError('Enter a company name or stock symbol')
      return
    }

    setError('')
    setIsLoading(true)
    setHasSearched(true)
    try {
      setResults(await searchStocks(normalizedQuery))
    } catch (requestError) {
      setResults([])
      setError(getMarketErrorMessage(requestError))
    } finally {
      setIsLoading(false)
    }
  }

  async function handleAddToWatchlist(stock: StockSearchResult) {
    setError('')
    setMessage('')
    setAddingSymbol(stock.symbol)
    try {
      await addToWatchlist({ symbol: stock.symbol })
      setMessage(`${stock.symbol} added to your watchlist`)
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, 'Unable to add stock to watchlist'),
      )
    } finally {
      setAddingSymbol('')
    }
  }

  return (
    <div className="mx-auto max-w-6xl px-6 py-14">
      <header>
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
          Market discovery
        </p>
        <h1 className="mt-3 text-4xl font-bold text-white">Explore stocks</h1>
        <p className="mt-3 max-w-2xl leading-7 text-slate-400">
          Search companies and symbols, then open a quote for a simulated
          market-data view.
        </p>
      </header>

      <form
        onSubmit={handleSubmit}
        className="mt-9 flex flex-col gap-3 rounded-2xl border border-slate-800 bg-slate-900/70 p-5 sm:flex-row"
      >
        <input
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Search AAPL, Microsoft, Tesla..."
          aria-label="Stock search"
          className="min-w-0 flex-1 rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none placeholder:text-slate-600 focus:border-rocket-500 focus:ring-2 focus:ring-rocket-500/20"
        />
        <button
          type="submit"
          disabled={isLoading}
          className="rounded-xl bg-rocket-500 px-6 py-3 font-semibold text-slate-950 hover:bg-rocket-400 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isLoading ? 'Searching...' : 'Search'}
        </button>
      </form>

      {error && (
        <p
          role="alert"
          className="mt-5 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-300"
        >
          {error}
        </p>
      )}
      {message && (
        <p className="mt-5 rounded-xl border border-rocket-500/30 bg-rocket-500/10 px-4 py-3 text-sm text-rocket-300">
          {message}
        </p>
      )}

      {!hasSearched && !error && (
        <div className="mt-8 rounded-2xl border border-dashed border-slate-700 px-6 py-14 text-center text-slate-500">
          Search for a stock symbol to begin.
        </div>
      )}

      {hasSearched && !isLoading && !error && results.length === 0 && (
        <div className="mt-8 rounded-2xl border border-dashed border-slate-700 px-6 py-14 text-center text-slate-500">
          No matching stocks were found.
        </div>
      )}

      {results.length > 0 && (
        <section className="mt-8 grid gap-4">
          {results.map((stock) => (
            <article
              key={`${stock.symbol}-${stock.exchange ?? 'unknown'}`}
              className="grid gap-4 rounded-2xl border border-slate-800 bg-slate-900/60 p-5 sm:grid-cols-[0.7fr_2fr_1fr_0.8fr_1fr_auto] sm:items-center"
            >
              <strong className="text-lg text-rocket-400">{stock.symbol}</strong>
              <span className="font-medium text-white">{stock.name}</span>
              <span className="text-sm text-slate-400">
                {stock.exchange ?? 'Exchange unavailable'}
              </span>
              <span className="text-sm text-slate-400">
                {stock.currency ?? '—'}
              </span>
              <span className="text-sm text-slate-400">
                {stock.type ?? 'Security'}
              </span>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  disabled={addingSymbol === stock.symbol}
                  onClick={() => void handleAddToWatchlist(stock)}
                  className="rounded-lg border border-rocket-500/40 px-3 py-2 text-xs font-semibold text-rocket-300 hover:bg-rocket-500/10 disabled:opacity-60"
                >
                  {addingSymbol === stock.symbol ? 'Adding...' : 'Add'}
                </button>
                <Link
                  to={`/market/${encodeURIComponent(stock.symbol)}`}
                  className="rounded-lg bg-slate-800 px-3 py-2 text-xs font-semibold text-white hover:bg-slate-700"
                >
                  Details
                </Link>
              </div>
            </article>
          ))}
        </section>
      )}
    </div>
  )
}
