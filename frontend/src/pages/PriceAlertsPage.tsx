import { useEffect, useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router-dom'

import {
  cancelAlert,
  createAlert,
  getAlerts,
} from '../features/alerts/priceAlertService'
import type { PriceAlert, PriceAlertCondition } from '../types/alert'
import { getApiErrorMessage } from '../utils/apiError'
import { formatCurrency, formatDateTime } from '../utils/formatters'

export function PriceAlertsPage() {
  const [params] = useSearchParams()
  const [alerts, setAlerts] = useState<PriceAlert[]>([])
  const [symbol, setSymbol] = useState(params.get('symbol') ?? '')
  const [condition, setCondition] = useState<PriceAlertCondition>('ABOVE')
  const [targetPrice, setTargetPrice] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    getAlerts().then(setAlerts).catch((requestError) =>
      setError(getApiErrorMessage(requestError, 'Unable to load price alerts')),
    )
  }, [])

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)
    try {
      const created = await createAlert({
        symbol: symbol.trim().toUpperCase(),
        condition,
        targetPrice: Number(targetPrice),
      })
      setAlerts((current) => [created, ...current])
      setTargetPrice('')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to create price alert'))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function cancel(id: string) {
    const updated = await cancelAlert(id)
    setAlerts((current) =>
      current.map((alert) => (alert.id === id ? updated : alert)),
    )
  }

  const active = alerts.filter((alert) => alert.status === 'ACTIVE')

  return (
    <div className="mx-auto max-w-6xl px-6 py-14">
      <h1 className="text-4xl font-bold text-white">Price Alerts</h1>
      <p className="mt-3 text-slate-400">Alerts use the configured simulated or financial market-data provider.</p>
      <form onSubmit={submit} className="mt-8 grid gap-4 rounded-2xl border border-slate-800 bg-slate-900/60 p-6 sm:grid-cols-4">
        <input required value={symbol} onChange={(event) => setSymbol(event.target.value)} placeholder="Symbol" className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 uppercase text-white" />
        <select value={condition} onChange={(event) => setCondition(event.target.value as PriceAlertCondition)} className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white">
          <option value="ABOVE">ABOVE</option>
          <option value="BELOW">BELOW</option>
        </select>
        <input required min="0.0001" step="0.01" type="number" value={targetPrice} onChange={(event) => setTargetPrice(event.target.value)} placeholder="Target price" className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white" />
        <button disabled={isSubmitting} className="rounded-xl bg-rocket-500 px-5 py-3 font-semibold text-slate-950 disabled:opacity-60">{isSubmitting ? 'Creating...' : 'Create Alert'}</button>
      </form>
      {error && <p className="mt-5 text-red-300">{error}</p>}
      <section className="mt-10">
        <h2 className="text-2xl font-bold text-white">Active alerts ({active.length})</h2>
        {alerts.length === 0 ? (
          <p className="mt-5 rounded-2xl border border-dashed border-slate-700 p-12 text-center text-slate-500">No price alerts yet.</p>
        ) : (
          <div className="mt-5 grid gap-4 md:grid-cols-2">
            {alerts.map((alert) => (
              <article key={alert.id} className="rounded-2xl border border-slate-800 bg-slate-900/60 p-5">
                <div className="flex justify-between gap-4">
                  <div>
                    <p className="font-bold text-rocket-400">{alert.symbol}</p>
                    <h3 className="mt-1 text-lg font-semibold text-white">{alert.companyName}</h3>
                  </div>
                  <span className={`h-fit rounded-full px-3 py-1 text-xs font-semibold ${alert.status === 'ACTIVE' ? 'bg-blue-500/15 text-blue-300' : alert.status === 'TRIGGERED' ? 'bg-rocket-500/15 text-rocket-300' : 'bg-slate-700 text-slate-300'}`}>{alert.status}</span>
                </div>
                <p className="mt-4 text-slate-300">{alert.condition} {formatCurrency(alert.targetPrice)}</p>
                {alert.triggeredPrice !== null && <p className="mt-2 text-sm text-rocket-300">Triggered at {formatCurrency(alert.triggeredPrice)}</p>}
                <p className="mt-3 text-xs text-slate-500">{formatDateTime(alert.createdAt)}</p>
                {alert.status === 'ACTIVE' && <button type="button" onClick={() => void cancel(alert.id)} className="mt-4 text-sm font-semibold text-red-400">Cancel alert</button>}
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
