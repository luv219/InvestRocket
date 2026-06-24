import { useEffect, useState } from 'react'

import { cancelOrder, getPendingOrders } from '../features/orders/orderService'
import type { Order } from '../types/order'
import { getApiErrorMessage } from '../utils/apiError'
import {
  formatDateTime,
  formatOptionalCurrency,
} from '../utils/formatters'

export function PendingOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [cancellingId, setCancellingId] = useState<string | null>(null)

  async function loadOrders() {
    setIsLoading(true)
    try {
      setOrders(await getPendingOrders())
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, 'Unable to load pending orders'),
      )
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    getPendingOrders()
      .then(setOrders)
      .catch((requestError) =>
        setError(
          getApiErrorMessage(requestError, 'Unable to load pending orders'),
        ),
      )
      .finally(() => setIsLoading(false))
  }, [])

  async function handleCancel(order: Order) {
    if (
      !window.confirm(
        `Cancel pending ${order.orderType} order for ${order.symbol}?`,
      )
    ) {
      return
    }
    setCancellingId(order.id)
    setMessage('')
    setError('')
    try {
      await cancelOrder(order.id)
      setMessage('Pending order cancelled successfully.')
      await loadOrders()
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, 'Unable to cancel pending order'),
      )
    } finally {
      setCancellingId(null)
    }
  }

  return (
    <div className="mx-auto max-w-7xl px-6 py-14">
      <h1 className="text-4xl font-bold text-white">Pending Orders</h1>
      <p className="mt-3 text-slate-400">
        Reserved cash and locked shares are released when an order is
        cancelled.
      </p>

      {message && (
        <p className="mt-6 rounded-xl bg-rocket-500/10 px-4 py-3 text-rocket-300">
          {message}
        </p>
      )}
      {error && (
        <p className="mt-6 rounded-xl bg-red-500/10 px-4 py-3 text-red-300">
          {error}
        </p>
      )}
      {isLoading && (
        <p className="mt-8 text-slate-400">Loading pending orders...</p>
      )}
      {!isLoading && orders.length === 0 && (
        <p className="mt-8 text-slate-400">No pending orders.</p>
      )}

      {orders.length > 0 && (
        <div className="mt-8 overflow-x-auto rounded-2xl border border-slate-800">
          <table className="min-w-full divide-y divide-slate-800 text-left text-sm">
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                {[
                  'Symbol',
                  'Side',
                  'Type',
                  'Quantity',
                  'Limit Price',
                  'Stop Price',
                  'Status',
                  'Created',
                  'Action',
                ].map((heading) => (
                  <th key={heading} className="px-4 py-3 font-medium">
                    {heading}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {orders.map((order) => (
                <tr key={order.id}>
                  <td className="px-4 py-4 font-semibold text-rocket-400">
                    {order.symbol}
                  </td>
                  <td className="px-4 py-4 text-slate-300">{order.side}</td>
                  <td className="px-4 py-4 text-slate-300">
                    {order.orderType}
                  </td>
                  <td className="px-4 py-4 text-slate-300">
                    {order.quantity}
                  </td>
                  <td className="px-4 py-4 text-slate-300">
                    {formatOptionalCurrency(order.limitPrice)}
                  </td>
                  <td className="px-4 py-4 text-slate-300">
                    {formatOptionalCurrency(order.stopPrice)}
                  </td>
                  <td className="px-4 py-4 text-amber-300">{order.status}</td>
                  <td className="px-4 py-4 text-slate-400">
                    {formatDateTime(order.createdAt)}
                  </td>
                  <td className="px-4 py-4">
                    <button
                      type="button"
                      disabled={cancellingId === order.id}
                      onClick={() => void handleCancel(order)}
                      className="rounded-lg border border-red-500/40 px-3 py-2 font-semibold text-red-300 hover:bg-red-500/10 disabled:opacity-50"
                    >
                      {cancellingId === order.id
                        ? 'Cancelling...'
                        : 'Cancel'}
                    </button>
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
