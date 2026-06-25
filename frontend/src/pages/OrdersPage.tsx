import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { getOrders } from '../features/orders/orderService'
import { Alert } from '../components/ui/Alert'
import { Badge } from '../components/ui/Badge'
import { EmptyState } from '../components/ui/EmptyState'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'
import { PageHeader } from '../components/ui/PageHeader'
import { Table, TableContainer } from '../components/ui/Table'
import type { Order } from '../types/order'
import { getApiErrorMessage } from '../utils/apiError'
import {
  formatCurrency,
  formatDateTime,
  formatOrderSide,
  formatOrderStatus,
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
      <PageHeader
        eyebrow="Virtual trading"
        title="Order History"
        description="Review every simulated order and its final lifecycle state."
        actions={hasPending ? (
          <Link
            to="/orders/pending"
            className="rounded-xl bg-amber-400/15 px-4 py-2 font-semibold text-amber-300"
          >
            View Pending Orders
          </Link>
        ) : undefined}
      />
      {isLoading && (
        <LoadingSpinner label="Loading orders..." />
      )}
      {error && <div className="mt-8"><Alert tone="error">{error}</Alert></div>}
      {!isLoading && !error && orders.length === 0 && (
        <div className="mt-8"><EmptyState title="No orders yet" description="Search the market and place a virtual order to begin your trading history." /></div>
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
                  <td
                    className={`px-4 py-4 font-medium ${
                      order.side === 'BUY'
                        ? 'text-rocket-400'
                        : 'text-red-400'
                    }`}
                  >
                    {formatOrderSide(order.side)}
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
                  <td className="px-4 py-4">
                    <Badge tone={order.status === 'EXECUTED' ? 'success' : order.status === 'PENDING' ? 'warning' : order.status === 'CANCELLED' ? 'neutral' : 'danger'}>
                      {formatOrderStatus(order.status)}
                    </Badge>
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
          </Table>
        </TableContainer>
      )}
    </div>
  )
}
