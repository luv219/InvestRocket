import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'

import { MarketStatCard } from '../components/MarketStatCard'
import { getMarketErrorMessage } from '../features/market/marketErrors'
import { getQuote } from '../features/market/marketService'
import type { StockQuote } from '../types/market'

function formatMoney(value: number | null, currency: string | null) {
  if (value === null) {
    return 'Unavailable'
  }
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency ?? 'USD',
  }).format(value)
}

function formatNumber(value: number | null) {
  return value === null ? 'Unavailable' : new Intl.NumberFormat('en-US').format(value)
}

function formatChange(value: number | null, suffix = '') {
  if (value === null) {
    return 'Unavailable'
  }
  const prefix = value >= 0 ? '+' : ''
  return `${prefix}${formatNumber(value)}${suffix}`
}

export function StockDetailPage() {
  const { symbol = '' } = useParams()
  const [quote, setQuote] = useState<StockQuote | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function loadQuote() {
      setIsLoading(true)
      setError('')
      try {
        setQuote(await getQuote(symbol.toUpperCase()))
      } catch (requestError) {
        setError(getMarketErrorMessage(requestError))
      } finally {
        setIsLoading(false)
      }
    }

    void loadQuote()
  }, [symbol])

  if (isLoading) {
    return (
      <div className="grid min-h-[60vh] place-items-center text-slate-400">
        Loading quote...
      </div>
    )
  }

  if (error || !quote) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-16">
        <div className="rounded-2xl border border-red-500/30 bg-red-500/10 p-6 text-red-200">
          <h1 className="text-xl font-semibold">Quote unavailable</h1>
          <p className="mt-2">{error || 'Unable to load this stock quote'}</p>
          <Link
            to="/market"
            className="mt-5 inline-flex rounded-lg bg-slate-800 px-4 py-2 text-sm font-semibold text-white"
          >
            Back to market
          </Link>
        </div>
      </div>
    )
  }

  const isPositive = (quote.changeAmount ?? 0) >= 0
  const changeClass = isPositive ? 'text-rocket-400' : 'text-red-400'

  return (
    <div className="mx-auto max-w-6xl px-6 py-14">
      <Link to="/market" className="text-sm font-medium text-slate-400 hover:text-white">
        ← Back to market search
      </Link>

      <section className="mt-6 grid gap-6 rounded-3xl border border-slate-800 bg-slate-900/70 p-8 md:grid-cols-[1fr_auto] md:items-center">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
            {quote.symbol}
          </p>
          <h1 className="mt-3 text-4xl font-bold text-white">
            {quote.companyName}
          </h1>
          <p className="mt-3 text-sm text-slate-500">
            Provider: {quote.provider}
          </p>
        </div>
        <div className="md:text-right">
          <p className="text-4xl font-bold text-white">
            {formatMoney(quote.currentPrice, quote.currency)}
          </p>
          <p className={`mt-2 font-semibold ${changeClass}`}>
            {formatChange(quote.changeAmount)} ({formatChange(quote.changePercent, '%')})
          </p>
        </div>
      </section>

      <section className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <MarketStatCard
          label="Open"
          value={formatMoney(quote.openPrice, quote.currency)}
        />
        <MarketStatCard
          label="High"
          value={formatMoney(quote.highPrice, quote.currency)}
        />
        <MarketStatCard
          label="Low"
          value={formatMoney(quote.lowPrice, quote.currency)}
        />
        <MarketStatCard
          label="Previous close"
          value={formatMoney(quote.previousClose, quote.currency)}
        />
        <MarketStatCard label="Volume" value={formatNumber(quote.volume)} />
        <MarketStatCard
          label="Latest trading time"
          value={new Date(quote.latestTradingTime).toLocaleString()}
        />
        <MarketStatCard label="Currency" value={quote.currency ?? 'Unavailable'} />
        <MarketStatCard label="Provider" value={quote.provider} />
      </section>

      <section className="mt-8 flex flex-col gap-3 rounded-2xl border border-slate-800 bg-slate-900/60 p-6 sm:flex-row">
        <button
          type="button"
          disabled
          className="flex-1 cursor-not-allowed rounded-xl bg-rocket-500/30 px-5 py-3 font-semibold text-rocket-100 opacity-60"
        >
          Buy Coming Soon
        </button>
        <button
          type="button"
          disabled
          className="flex-1 cursor-not-allowed rounded-xl bg-red-500/20 px-5 py-3 font-semibold text-red-200 opacity-60"
        >
          Sell Coming Soon
        </button>
      </section>
    </div>
  )
}
