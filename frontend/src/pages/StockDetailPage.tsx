import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'

import { MarketStatCard } from '../components/MarketStatCard'
import { getMarketErrorMessage } from '../features/market/marketErrors'
import { getQuote } from '../features/market/marketService'
import { useLivePrices } from '../features/live/useLivePrices'
import { placeOrder } from '../features/orders/orderService'
import {
  addToWatchlist,
  getWatchlist,
  removeFromWatchlist,
} from '../features/watchlist/watchlistService'
import type { StockQuote } from '../types/market'
import type { Order, OrderSide, OrderType } from '../types/order'
import { getApiErrorMessage } from '../utils/apiError'

function formatMoney(value: number | null, currency: string | null) {
  if (value === null) return 'Unavailable'
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency ?? 'USD',
  }).format(value)
}

function formatNumber(value: number | null) {
  return value === null
    ? 'Unavailable'
    : new Intl.NumberFormat('en-US').format(value)
}

function formatChange(value: number | null, suffix = '') {
  if (value === null) return 'Unavailable'
  return `${value >= 0 ? '+' : ''}${formatNumber(value)}${suffix}`
}

export function StockDetailPage() {
  const { symbol = '' } = useParams()
  const [quote, setQuote] = useState<StockQuote | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [side, setSide] = useState<OrderSide>('BUY')
  const [orderType, setOrderType] = useState<OrderType>('MARKET')
  const [quantity, setQuantity] = useState(1)
  const [limitPrice, setLimitPrice] = useState('')
  const [stopPrice, setStopPrice] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [orderError, setOrderError] = useState('')
  const [submittedOrder, setSubmittedOrder] = useState<Order | null>(null)
  const [isWatched, setIsWatched] = useState(false)
  const [isUpdatingWatchlist, setIsUpdatingWatchlist] = useState(false)
  const [watchlistMessage, setWatchlistMessage] = useState('')
  const livePrices = useLivePrices()

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

  useEffect(() => {
    getWatchlist()
      .then((items) =>
        setIsWatched(
          items.some((item) => item.symbol === symbol.toUpperCase()),
        ),
      )
      .catch(() => setIsWatched(false))
  }, [symbol])

  async function handleWatchlistToggle() {
    const normalizedSymbol = symbol.toUpperCase()
    setIsUpdatingWatchlist(true)
    setWatchlistMessage('')
    try {
      if (isWatched) {
        await removeFromWatchlist(normalizedSymbol)
        setIsWatched(false)
        setWatchlistMessage(`${normalizedSymbol} removed from watchlist`)
      } else {
        await addToWatchlist({ symbol: normalizedSymbol })
        setIsWatched(true)
        setWatchlistMessage(`${normalizedSymbol} added to watchlist`)
      }
    } catch (requestError) {
      setWatchlistMessage(
        getApiErrorMessage(requestError, 'Unable to update watchlist'),
      )
    } finally {
      setIsUpdatingWatchlist(false)
    }
  }

  function handleOrderTypeChange(type: OrderType) {
    setOrderType(type)
    setOrderError('')
    if (type === 'STOP_LOSS') setSide('SELL')
  }

  async function handleOrderSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!quote || quantity < 1) {
      setOrderError('Quantity must be greater than 0')
      return
    }
    if (orderType === 'LIMIT' && Number(limitPrice) <= 0) {
      setOrderError('Limit price must be greater than 0')
      return
    }
    if (orderType === 'STOP_LOSS' && Number(stopPrice) <= 0) {
      setOrderError('Stop price must be greater than 0')
      return
    }

    setOrderError('')
    setSubmittedOrder(null)
    setIsSubmitting(true)
    try {
      setSubmittedOrder(
        await placeOrder({
          symbol: quote.symbol,
          side,
          orderType,
          quantity,
          ...(orderType === 'LIMIT'
            ? { limitPrice: Number(limitPrice) }
            : {}),
          ...(orderType === 'STOP_LOSS'
            ? { stopPrice: Number(stopPrice) }
            : {}),
        }),
      )
    } catch (requestError) {
      setOrderError(
        getApiErrorMessage(requestError, 'Unable to place advanced order'),
      )
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

  const livePrice = livePrices[quote.symbol]
  const displayedQuote = livePrice
    ? {
        ...quote,
        currentPrice: livePrice.currentPrice,
        changeAmount: livePrice.changeAmount,
        changePercent: livePrice.changePercent,
        latestTradingTime: livePrice.latestTradingTime,
        provider: livePrice.provider,
      }
    : quote
  const referencePrice =
    orderType === 'LIMIT'
      ? Number(limitPrice) || 0
      : orderType === 'STOP_LOSS'
        ? Number(stopPrice) || 0
        : displayedQuote.currentPrice
  const buttonLabel =
    orderType === 'MARKET'
      ? 'Place Market Order'
      : orderType === 'LIMIT'
        ? 'Place Limit Order'
        : 'Place Stop-Loss Order'

  return (
    <div className="mx-auto max-w-6xl px-6 py-14">
      <Link
        to="/market"
        className="text-sm font-medium text-slate-400 hover:text-white"
      >
        ← Back to market search
      </Link>

      <section className="mt-6 grid gap-6 rounded-3xl border border-slate-800 bg-slate-900/70 p-8 md:grid-cols-[1fr_auto] md:items-center">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
            {displayedQuote.symbol}
          </p>
          <h1 className="mt-3 text-4xl font-bold text-white">
            {displayedQuote.companyName}
          </h1>
          <p className="mt-3 text-sm text-slate-500">
            Provider: {displayedQuote.provider}
          </p>
          <div className="mt-4 flex flex-wrap items-center gap-3">
            <button
              type="button"
              disabled={isUpdatingWatchlist}
              onClick={() => void handleWatchlistToggle()}
              className="rounded-lg border border-rocket-500/40 px-4 py-2 text-sm font-semibold text-rocket-300 hover:bg-rocket-500/10 disabled:opacity-60"
            >
              {isUpdatingWatchlist
                ? 'Updating...'
                : isWatched
                  ? 'Remove from Watchlist'
                  : 'Add to Watchlist'}
            </button>
            <span className="text-xs font-semibold text-rocket-300">
              ● Live demo price updates enabled
            </span>
            <Link
              to={`/alerts?symbol=${encodeURIComponent(displayedQuote.symbol)}`}
              className="rounded-lg border border-blue-500/40 px-4 py-2 text-sm font-semibold text-blue-300 hover:bg-blue-500/10"
            >
              Create Price Alert
            </Link>
          </div>
          {watchlistMessage && (
            <p className="mt-3 text-sm text-slate-300">{watchlistMessage}</p>
          )}
        </div>
        <div className="md:text-right">
          <p className="text-4xl font-bold text-white">
            {formatMoney(displayedQuote.currentPrice, displayedQuote.currency)}
          </p>
          <p
            className={`mt-2 font-semibold ${
              (displayedQuote.changeAmount ?? 0) >= 0
                ? 'text-rocket-400'
                : 'text-red-400'
            }`}
          >
            {formatChange(displayedQuote.changeAmount)} (
            {formatChange(displayedQuote.changePercent, '%')})
          </p>
        </div>
      </section>

      <section className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <MarketStatCard
          label="Open"
          value={formatMoney(displayedQuote.openPrice, displayedQuote.currency)}
        />
        <MarketStatCard
          label="High"
          value={formatMoney(displayedQuote.highPrice, displayedQuote.currency)}
        />
        <MarketStatCard
          label="Low"
          value={formatMoney(displayedQuote.lowPrice, displayedQuote.currency)}
        />
        <MarketStatCard
          label="Previous close"
          value={formatMoney(displayedQuote.previousClose, displayedQuote.currency)}
        />
        <MarketStatCard label="Volume" value={formatNumber(displayedQuote.volume)} />
        <MarketStatCard
          label="Latest trading time"
          value={new Date(displayedQuote.latestTradingTime).toLocaleString()}
        />
        <MarketStatCard
          label="Currency"
          value={displayedQuote.currency ?? 'Unavailable'}
        />
        <MarketStatCard label="Provider" value={displayedQuote.provider} />
      </section>

      <section className="mt-8 rounded-2xl border border-slate-800 bg-slate-900/60 p-6">
        <p className="text-sm font-semibold uppercase tracking-[0.18em] text-rocket-400">
          Advanced virtual order
        </p>
        <h2 className="mt-2 text-2xl font-bold text-white">Place an order</h2>
        <p className="mt-2 text-sm text-amber-200">
          All orders use simulated funds. Pending orders are checked
          periodically.
        </p>

        <form
          onSubmit={handleOrderSubmit}
          className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3"
        >
          <label>
            <span className="text-sm font-medium text-slate-300">Side</span>
            <select
              value={side}
              disabled={orderType === 'STOP_LOSS'}
              onChange={(event) =>
                setSide(event.target.value as OrderSide)
              }
              className="mt-2 w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white disabled:opacity-60"
            >
              <option value="BUY">BUY</option>
              <option value="SELL">SELL</option>
            </select>
          </label>
          <label>
            <span className="text-sm font-medium text-slate-300">
              Order type
            </span>
            <select
              value={orderType}
              onChange={(event) =>
                handleOrderTypeChange(event.target.value as OrderType)
              }
              className="mt-2 w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white"
            >
              <option value="MARKET">MARKET</option>
              <option value="LIMIT">LIMIT</option>
              <option value="STOP_LOSS">STOP LOSS (SELL)</option>
            </select>
          </label>
          <label>
            <span className="text-sm font-medium text-slate-300">
              Quantity
            </span>
            <input
              type="number"
              min={1}
              step={1}
              required
              value={quantity}
              onChange={(event) => setQuantity(Number(event.target.value))}
              className="mt-2 w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white"
            />
          </label>

          {orderType === 'LIMIT' && (
            <label>
              <span className="text-sm font-medium text-slate-300">
                Limit price
              </span>
              <input
                type="number"
                min="0.01"
                step="0.01"
                required
                value={limitPrice}
                onChange={(event) => setLimitPrice(event.target.value)}
                className="mt-2 w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white"
              />
            </label>
          )}

          {orderType === 'STOP_LOSS' && (
            <label>
              <span className="text-sm font-medium text-slate-300">
                Stop price
              </span>
              <input
                type="number"
                min="0.01"
                step="0.01"
                required
                value={stopPrice}
                onChange={(event) => setStopPrice(event.target.value)}
                className="mt-2 w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white"
              />
            </label>
          )}

          <div>
            <p className="text-sm font-medium text-slate-300">
              Estimated total
            </p>
            <p className="mt-2 rounded-xl border border-slate-800 bg-slate-950 px-4 py-3 font-semibold text-white">
              {formatMoney(
                referencePrice * Math.max(quantity, 0),
                displayedQuote.currency,
              )}
            </p>
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="self-end rounded-xl bg-rocket-500 px-6 py-3 font-semibold text-slate-950 hover:bg-rocket-400 disabled:opacity-60"
          >
            {isSubmitting ? 'Submitting...' : buttonLabel}
          </button>
        </form>

        {orderError && (
          <p
            role="alert"
            className="mt-5 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-300"
          >
            {orderError}
          </p>
        )}

        {submittedOrder && (
          <div className="mt-5 rounded-xl border border-rocket-500/30 bg-rocket-500/10 px-5 py-4">
            <p className="font-semibold text-rocket-300">
              {submittedOrder.status === 'PENDING'
                ? 'Order accepted and pending its trigger.'
                : submittedOrder.message}
            </p>
            <p className="mt-1 text-sm text-slate-300">
              Status: {submittedOrder.status}. Quantity:{' '}
              {submittedOrder.quantity} {submittedOrder.symbol}.
            </p>
            <div className="mt-4 flex flex-wrap gap-3 text-sm font-semibold">
              <Link to="/orders/pending" className="text-rocket-400">
                Pending Orders
              </Link>
              <Link to="/orders" className="text-rocket-400">
                All Orders
              </Link>
              <Link to="/portfolio" className="text-rocket-400">
                Portfolio
              </Link>
            </div>
          </div>
        )}
      </section>
    </div>
  )
}
