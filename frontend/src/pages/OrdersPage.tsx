import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { getOrders } from '../features/orders/orderService'
import type { Order } from '../types/order'
import { getApiErrorMessage } from '../utils/apiError'
import {
  formatCurrency,
  formatDateTime,
  formatOptionalCurrency,
  formatOptionalDateTime,
} from '../utils/formatters'

export function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getOrders()
      .then(setOrders)
      .catch((requestError) =>
        setError(getApiErrorMessage(requestError, 'Unable to load orders')),
      )
      .finally(() => setIsLoading(false))
  }, [])

  const hasPending = orders.some((order) => order.status === 'PENDING')

  return (
    <div className="mx-auto max-w-[90rem] px-6 py-14">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-4xl font-bold text-white">Order History</h1>
        {hasPending && (
          <Link
            to="/orders/pending"
            className="rounded-xl bg-amber-400/15 px-4 py-2 font-semibold text-amber-300"
          >
            View Pending Orders
          </Link>
        )}
      </div>
      {isLoading && (
        <p className="mt-8 text-slate-400">Loading orders...</p>
      )}
      {error && <p className="mt-8 text-red-300">{error}</p>}
      {!isLoading && !error && orders.length === 0 && (
        <p className="mt-8 text-slate-400">No orders yet.</p>
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
                  'Qty',
                  'Limit',
                  'Stop',
                  'Executed',
                  'Total',
                  'Status',
                  'Reason',
                  'Created',
                  'Executed At',
                  'Cancelled At',
                  'Journal',
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
                  <td
                    className={`px-4 py-4 font-medium ${
                      order.side === 'BUY'
                        ? 'text-rocket-400'
                        : 'text-red-400'
                    }`}
                  >
                    {order.side}
                  </td>
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
                  <td className="px-4 py-4 text-slate-300">
                    {formatOptionalCurrency(order.executedPrice)}
                  </td>
                  <td className="px-4 py-4 text-slate-300">
                    {formatCurrency(order.totalAmount)}
                  </td>
                  <td className="px-4 py-4 text-slate-300">
                    {order.status}
                  </td>
                  <td className="max-w-48 px-4 py-4 text-slate-400">
                    {order.statusReason ?? '—'}
                  </td>
                  <td className="px-4 py-4 text-slate-400">
                    {formatDateTime(order.createdAt)}
                  </td>
                  <td className="px-4 py-4 text-slate-400">
                    {formatOptionalDateTime(order.executedAt)}
                  </td>
                  <td className="px-4 py-4 text-slate-400">
                    {formatOptionalDateTime(order.cancelledAt)}
                  </td>
                  <td className="px-4 py-4">
                    <Link
                      to={`/journal?symbol=${encodeURIComponent(order.symbol)}&orderId=${encodeURIComponent(order.id)}`}
                      className="font-semibold text-rocket-400"
                    >
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
