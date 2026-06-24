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

export function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUser[]>([])
  const [query, setQuery] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    getAdminUsers()
      .then(setUsers)
      .catch((requestError) =>
        setError(getApiErrorMessage(requestError, 'Unable to load users')),
      )
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
      <input
        value={query}
        onChange={(event) => setQuery(event.target.value)}
        placeholder="Search name or email"
        className="mt-7 w-full max-w-md rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white"
      />
      {error && <p className="mt-5 text-red-300">{error}</p>}
      <div className="mt-8 overflow-x-auto rounded-2xl border border-slate-800">
        <table className="min-w-full divide-y divide-slate-800 text-left text-sm">
          <thead className="bg-slate-900 text-slate-400">
            <tr>
              {['Name', 'Email', 'Role', 'Status', 'Created', 'Orders', 'Trades', 'Cash', 'Actions'].map(
                (heading) => (
                  <th key={heading} className="px-4 py-3 font-medium">
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
                <td className="px-4 py-4 text-rocket-400">{user.role}</td>
                <td className="px-4 py-4">{user.enabled ? 'Enabled' : 'Disabled'}</td>
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
        </table>
      </div>
    </div>
  )
}
