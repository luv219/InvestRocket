import { useEffect, useState } from 'react'

import { cancelOrder, getPendingOrders } from '../features/orders/orderService'
import { Alert } from '../components/ui/Alert'
import { Badge } from '../components/ui/Badge'
import { EmptyState } from '../components/ui/EmptyState'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'
import { PageHeader } from '../components/ui/PageHeader'
import { Table, TableContainer } from '../components/ui/Table'
import type { Order } from '../types/order'
import { getApiErrorMessage } from '../utils/apiError'
import {
  formatDateTime,
  formatOrderSide,
  formatOrderStatus,
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
      <PageHeader
        eyebrow="Advanced orders"
        title="Pending Orders"
        description="Reserved cash and locked shares are released when an order is cancelled."
      />

      {message && (
        <div className="mt-6"><Alert tone="success">{message}</Alert></div>
      )}
      {error && (
        <div className="mt-6"><Alert tone="error">{error}</Alert></div>
      )}
      {isLoading && (
        <LoadingSpinner label="Loading pending orders..." />
      )}
      {!isLoading && orders.length === 0 && (
        <div className="mt-8"><EmptyState title="No pending orders" description="Limit and stop-loss orders waiting for a trigger will appear here." /></div>
      )}

      {orders.length > 0 && (
        <TableContainer className="mt-8">
          <Table>
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
                  <th key={heading} scope="col" className="whitespace-nowrap px-4 py-3 font-medium">
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
                  <td className="px-4 py-4 text-slate-300">{formatOrderSide(order.side)}</td>
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
                  <td className="px-4 py-4"><Badge tone="warning">{formatOrderStatus(order.status)}</Badge></td>
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
          </Table>
        </TableContainer>
      )}
    </div>
  )
}
