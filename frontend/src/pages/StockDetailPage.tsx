import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'

import { MarketStatCard } from '../components/MarketStatCard'
import { getMarketErrorMessage } from '../features/market/marketErrors'
import { getQuote } from '../features/market/marketService'
import { placeOrder } from '../features/orders/orderService'
import type { StockQuote } from '../types/market'
import type { Order, OrderSide } from '../types/order'
import { getApiErrorMessage } from '../utils/apiError'

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
  const [side, setSide] = useState<OrderSide>('BUY')
  const [quantity, setQuantity] = useState(1)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [orderError, setOrderError] = useState('')
  const [executedOrder, setExecutedOrder] = useState<Order | null>(null)

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

  async function handleOrderSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!quote || quantity < 1) {
      setOrderError('Quantity must be greater than 0')
      return
    }

    setOrderError('')
    setExecutedOrder(null)
    setIsSubmitting(true)
    try {
      const order = await placeOrder({
        symbol: quote.symbol,
        side,
        orderType: 'MARKET',
        quantity,
      })
      setExecutedOrder(order)
    } catch (requestError) {
      setOrderError(getApiErrorMessage(requestError, 'Unable to place market order'))
    } finally {
      setIsSubmitting(false)
    }
  }

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

      <section className="mt-8 rounded-2xl border border-slate-800 bg-slate-900/60 p-6">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-rocket-400">
            Market order
          </p>
          <h2 className="mt-2 text-2xl font-bold text-white">Place a virtual trade</h2>
          <p className="mt-2 text-sm text-amber-200">
            This is a virtual market order using simulated funds.
          </p>
        </div>

        <form onSubmit={handleOrderSubmit} className="mt-6 grid gap-4 md:grid-cols-[1fr_1fr_1fr_auto] md:items-end">
          <label>
            <span className="text-sm font-medium text-slate-300">Side</span>
            <select
              value={side}
              onChange={(event) => setSide(event.target.value as OrderSide)}
              className="mt-2 w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none focus:border-rocket-500"
            >
              <option value="BUY">BUY</option>
              <option value="SELL">SELL</option>
            </select>
          </label>
          <label>
            <span className="text-sm font-medium text-slate-300">Quantity</span>
            <input
              type="number"
              min={1}
              step={1}
              required
              value={quantity}
              onChange={(event) => setQuantity(Number(event.target.value))}
              className="mt-2 w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none focus:border-rocket-500"
            />
          </label>
          <div>
            <p className="text-sm font-medium text-slate-300">Estimated total</p>
            <p className="mt-2 rounded-xl border border-slate-800 bg-slate-950 px-4 py-3 font-semibold text-white">
              {formatMoney(quote.currentPrice * Math.max(quantity, 0), quote.currency)}
            </p>
          </div>
          <button
            type="submit"
            disabled={isSubmitting}
            className={`rounded-xl px-6 py-3 font-semibold disabled:cursor-not-allowed disabled:opacity-60 ${
              side === 'BUY'
                ? 'bg-rocket-500 text-slate-950 hover:bg-rocket-400'
                : 'bg-red-500 text-white hover:bg-red-400'
            }`}
          >
            {isSubmitting ? 'Executing...' : `${side} MARKET`}
          </button>
        </form>

        {orderError && (
          <p role="alert" className="mt-5 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-300">
            {orderError}
          </p>
        )}

        {executedOrder && (
          <div className="mt-5 rounded-xl border border-rocket-500/30 bg-rocket-500/10 px-5 py-4">
            <p className="font-semibold text-rocket-300">{executedOrder.message}</p>
            <p className="mt-1 text-sm text-slate-300">
              {executedOrder.quantity} {executedOrder.symbol} at{' '}
              {formatMoney(executedOrder.executedPrice, quote.currency)} for{' '}
              {formatMoney(executedOrder.totalAmount, quote.currency)}.
            </p>
            <div className="mt-4 flex flex-wrap gap-3 text-sm font-semibold">
              <Link to="/portfolio" className="text-rocket-400 hover:text-rocket-300">View Portfolio</Link>
              <Link to="/orders" className="text-rocket-400 hover:text-rocket-300">View Orders</Link>
              <Link to="/trades" className="text-rocket-400 hover:text-rocket-300">View Trades</Link>
            </div>
          </div>
        )}
      </section>
    </div>
  )
}
