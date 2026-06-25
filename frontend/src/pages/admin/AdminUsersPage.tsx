import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'

import {
  disableUser,
  enableUser,
  getAdminUsers,
} from '../../features/admin/adminService'
import type { AdminUser } from '../../types/admin'
import { getApiErrorMessage } from '../../utils/apiError'
import {
  formatCurrency,
  formatDateTime,
  formatNumber,
} from '../../utils/formatters'
import { AdminHeader } from './AdminDashboardPage'
import { Alert } from '../../components/ui/Alert'
import { Badge } from '../../components/ui/Badge'
import { EmptyState } from '../../components/ui/EmptyState'
import { Input } from '../../components/ui/Input'
import { Table, TableContainer } from '../../components/ui/Table'
import { LoadingSpinner } from '../../components/ui/LoadingSpinner'

export function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUser[]>([])
  const [query, setQuery] = useState('')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    getAdminUsers()
      .then(setUsers)
      .catch((requestError) =>
        setError(getApiErrorMessage(requestError, 'Unable to load users')),
      )
      .finally(() => setIsLoading(false))
  }, [])

  const filteredUsers = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    return normalized
      ? users.filter(
          (user) =>
            user.email.toLowerCase().includes(normalized) ||
            user.fullName.toLowerCase().includes(normalized),
        )
      : users
  }, [query, users])

  async function toggleUser(user: AdminUser) {
    setError('')
    try {
      const updated = user.enabled
        ? await disableUser(user.id)
        : await enableUser(user.id)
      setUsers((current) =>
        current.map((item) => (item.id === updated.id ? updated : item)),
      )
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update user status'))
    }
  }

  return (
    <div className="mx-auto max-w-7xl px-6 py-14">
      <AdminHeader
        eyebrow="User administration"
        title="Users"
        description="Manage simulator access and inspect account activity."
      />
      <label className="mt-7 grid max-w-md gap-2 text-sm text-slate-300">
        <span>Search users</span>
        <Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search name or email" />
      </label>
      {error && <div className="mt-5"><Alert tone="error">{error}</Alert></div>}
      {isLoading ? (
        <LoadingSpinner label="Loading users..." />
      ) : users.length > 0 && filteredUsers.length === 0 ? (
        <div className="mt-8"><EmptyState title="No matching users" description="Try a different name or email address." /></div>
      ) : (
      <TableContainer className="mt-8">
        <Table>
          <thead className="bg-slate-900 text-slate-400">
            <tr>
              {['Name', 'Email', 'Role', 'Status', 'Created', 'Orders', 'Trades', 'Cash', 'Actions'].map(
                (heading) => (
                  <th key={heading} scope="col" className="whitespace-nowrap px-4 py-3 font-medium">
                    {heading}
                  </th>
                ),
              )}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800 bg-slate-950/40">
            {filteredUsers.map((user) => (
              <tr key={user.id}>
                <td className="px-4 py-4 font-medium text-white">{user.fullName}</td>
                <td className="px-4 py-4 text-slate-300">{user.email}</td>
                <td className="px-4 py-4"><Badge tone={user.role === 'ADMIN' ? 'info' : 'neutral'}>{user.role}</Badge></td>
                <td className="px-4 py-4"><Badge tone={user.enabled ? 'success' : 'danger'}>{user.enabled ? 'Enabled' : 'Disabled'}</Badge></td>
                <td className="px-4 py-4 text-slate-400">
                  {formatDateTime(user.createdAt)}
                </td>
                <td className="px-4 py-4">{formatNumber(user.totalOrders)}</td>
                <td className="px-4 py-4">{formatNumber(user.totalTrades)}</td>
                <td className="px-4 py-4">
                  {formatCurrency(user.walletCashBalance)}
                </td>
                <td className="px-4 py-4">
                  <div className="flex gap-3">
                    <Link
                      to={`/admin/users/${user.id}`}
                      className="font-semibold text-rocket-400"
                    >
                      Details
                    </Link>
                    <button
                      type="button"
                      onClick={() => void toggleUser(user)}
                      className="font-semibold text-amber-300"
                    >
                      {user.enabled ? 'Disable' : 'Enable'}
                    </button>
                  </div>
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
