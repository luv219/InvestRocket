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
import { Alert } from '../components/ui/Alert'
import { Badge } from '../components/ui/Badge'
import { EmptyState } from '../components/ui/EmptyState'
import { PageHeader } from '../components/ui/PageHeader'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'

export function PriceAlertsPage() {
  const [params] = useSearchParams()
  const [alerts, setAlerts] = useState<PriceAlert[]>([])
  const [symbol, setSymbol] = useState(params.get('symbol') ?? '')
  const [condition, setCondition] = useState<PriceAlertCondition>('ABOVE')
  const [targetPrice, setTargetPrice] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    getAlerts()
      .then(setAlerts)
      .catch((requestError) =>
        setError(getApiErrorMessage(requestError, 'Unable to load price alerts')),
      )
      .finally(() => setIsLoading(false))
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
      <PageHeader eyebrow="Market monitoring" title="Price Alerts" description="Create simulated thresholds using the configured market-data provider." />
      <form onSubmit={submit} className="mt-8 grid gap-4 rounded-2xl border border-slate-800 bg-slate-900/60 p-6 sm:grid-cols-4">
        <input required value={symbol} onChange={(event) => setSymbol(event.target.value)} placeholder="Symbol" className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 uppercase text-white" />
        <select value={condition} onChange={(event) => setCondition(event.target.value as PriceAlertCondition)} className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white">
          <option value="ABOVE">ABOVE</option>
          <option value="BELOW">BELOW</option>
        </select>
        <input required min="0.0001" step="0.01" type="number" value={targetPrice} onChange={(event) => setTargetPrice(event.target.value)} placeholder="Target price" className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white" />
        <button disabled={isSubmitting} className="rounded-xl bg-rocket-500 px-5 py-3 font-semibold text-slate-950 disabled:opacity-60">{isSubmitting ? 'Creating...' : 'Create Alert'}</button>
      </form>
      {error && <div className="mt-5"><Alert tone="error">{error}</Alert></div>}
      <section className="mt-10">
        <h2 className="text-2xl font-bold text-white">Active alerts ({active.length})</h2>
        {isLoading ? (
          <LoadingSpinner label="Loading price alerts..." />
        ) : alerts.length === 0 ? (
          <div className="mt-5"><EmptyState title="No price alerts yet" description="Create an ABOVE or BELOW threshold to monitor a symbol." /></div>
        ) : (
          <div className="mt-5 grid gap-4 md:grid-cols-2">
            {alerts.map((alert) => (
              <article key={alert.id} className="rounded-2xl border border-slate-800 bg-slate-900/60 p-5">
                <div className="flex justify-between gap-4">
                  <div>
                    <p className="font-bold text-rocket-400">{alert.symbol}</p>
                    <h3 className="mt-1 text-lg font-semibold text-white">{alert.companyName}</h3>
                  </div>
                  <Badge tone={alert.status === 'ACTIVE' ? 'info' : alert.status === 'TRIGGERED' ? 'success' : 'neutral'}>{alert.status}</Badge>
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
