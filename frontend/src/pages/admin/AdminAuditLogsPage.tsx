import { useEffect, useState, type FormEvent } from 'react'

import { getAdminAuditLogs } from '../../features/admin/adminService'
import type { AdminAuditLog } from '../../types/admin'
import { getApiErrorMessage } from '../../utils/apiError'
import { formatDateTime } from '../../utils/formatters'
import { AdminHeader } from './AdminDashboardPage'

const categories = ['', 'AUTH', 'PROFILE', 'WALLET', 'ORDER', 'TRADE', 'WATCHLIST', 'ANALYTICS', 'RISK', 'SYSTEM']

export function AdminAuditLogsPage() {
  const [logs, setLogs] = useState<AdminAuditLog[]>([])
  const [category, setCategory] = useState('')
  const [userEmail, setUserEmail] = useState('')
  const [error, setError] = useState('')

  async function loadLogs(params?: { category?: string; userEmail?: string }) {
    setError('')
    try {
      setLogs(await getAdminAuditLogs(params))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to load audit logs'))
    }
  }

  useEffect(() => {
    getAdminAuditLogs()
      .then(setLogs)
      .catch((requestError) =>
        setError(getApiErrorMessage(requestError, 'Unable to load audit logs')),
      )
  }, [])

  function handleFilter(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void loadLogs({
      ...(category ? { category } : {}),
      ...(userEmail.trim() ? { userEmail: userEmail.trim() } : {}),
    })
  }

  return (
    <div className="mx-auto max-w-7xl px-6 py-14">
      <AdminHeader
        eyebrow="Platform audit"
        title="Audit Logs"
        description="Recent non-sensitive activity across simulator accounts."
      />
      <form onSubmit={handleFilter} className="mt-7 grid gap-3 rounded-2xl border border-slate-800 bg-slate-900/60 p-5 sm:grid-cols-[1fr_2fr_auto]">
        <select value={category} onChange={(event) => setCategory(event.target.value)} className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white">
          {categories.map((item) => <option key={item || 'ALL'} value={item}>{item || 'All categories'}</option>)}
        </select>
        <input value={userEmail} onChange={(event) => setUserEmail(event.target.value)} placeholder="Filter by user email" className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white" />
        <button className="rounded-xl bg-rocket-500 px-5 py-3 font-semibold text-slate-950">Apply Filters</button>
      </form>
      {error && <p className="mt-5 text-red-300">{error}</p>}
      <div className="mt-8 overflow-x-auto rounded-2xl border border-slate-800">
        <table className="min-w-full divide-y divide-slate-800 text-left text-sm">
          <thead className="bg-slate-900 text-slate-400"><tr>{['User', 'Category', 'Action', 'Description', 'Time'].map((heading) => <th key={heading} className="px-4 py-3 font-medium">{heading}</th>)}</tr></thead>
          <tbody className="divide-y divide-slate-800 bg-slate-950/40">
            {logs.map((log) => (
              <tr key={log.id}>
                <td className="px-4 py-4 text-white">{log.userEmail}</td>
                <td className="px-4 py-4 text-rocket-400">{log.category}</td>
                <td className="px-4 py-4">{log.action.replaceAll('_', ' ')}</td>
                <td className="px-4 py-4 text-slate-300">{log.description}</td>
                <td className="px-4 py-4 text-slate-400">{formatDateTime(log.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {logs.length === 0 && <p className="px-6 py-12 text-center text-slate-500">No matching audit logs.</p>}
      </div>
    </div>
  )
}
