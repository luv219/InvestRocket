import { useEffect, useState, type FormEvent } from 'react'
import { useParams } from 'react-router-dom'

import {
  disableUser,
  enableUser,
  getAdminUserById,
  updateAdminUser,
} from '../../features/admin/adminService'
import type { AdminUserDetail } from '../../types/admin'
import { getApiErrorMessage } from '../../utils/apiError'
import {
  formatCurrency,
  formatDateTime,
  formatNumber,
} from '../../utils/formatters'
import { AdminHeader, Panel } from './AdminDashboardPage'

export function AdminUserDetailPage() {
  const { userId = '' } = useParams()
  const [detail, setDetail] = useState<AdminUserDetail | null>(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    getAdminUserById(userId)
      .then(setDetail)
      .catch((requestError) =>
        setError(getApiErrorMessage(requestError, 'Unable to load user detail')),
      )
  }, [userId])

  async function handleUpdate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!detail) return
    setError('')
    setMessage('')
    try {
      const updated = await updateAdminUser(userId, {
        fullName: detail.summary.fullName,
        role: detail.summary.role,
        enabled: detail.summary.enabled,
      })
      setDetail({ ...detail, summary: updated })
      setMessage('User updated successfully')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update user'))
    }
  }

  async function toggleStatus() {
    if (!detail) return
    setError('')
    try {
      const updated = detail.summary.enabled
        ? await disableUser(userId)
        : await enableUser(userId)
      setDetail({ ...detail, summary: updated })
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update user status'))
    }
  }

  if (error && !detail) {
    return <div className="mx-auto max-w-7xl px-6 py-14 text-red-300">{error}</div>
  }
  if (!detail) {
    return (
      <div className="grid min-h-[60vh] place-items-center text-slate-400">
        Loading user detail...
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-7xl px-6 py-14">
      <AdminHeader
        eyebrow="User administration"
        title={detail.summary.fullName}
        description={detail.summary.email}
      />
      {error && <p className="mt-5 text-red-300">{error}</p>}
      {message && <p className="mt-5 text-rocket-300">{message}</p>}

      <section className="mt-8 grid gap-6 lg:grid-cols-2">
        <Panel title="Account Controls">
          <form onSubmit={handleUpdate} className="grid gap-4">
            <input
              value={detail.summary.fullName}
              onChange={(event) =>
                setDetail({
                  ...detail,
                  summary: {
                    ...detail.summary,
                    fullName: event.target.value,
                  },
                })
              }
              className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white"
            />
            <select
              value={detail.summary.role}
              onChange={(event) =>
                setDetail({
                  ...detail,
                  summary: {
                    ...detail.summary,
                    role: event.target.value as 'USER' | 'ADMIN',
                  },
                })
              }
              className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white"
            >
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
            <button className="rounded-xl bg-rocket-500 px-5 py-3 font-semibold text-slate-950">
              Save User
            </button>
            <button
              type="button"
              onClick={() => void toggleStatus()}
              className="rounded-xl border border-amber-500/40 px-5 py-3 font-semibold text-amber-300"
            >
              {detail.summary.enabled ? 'Disable User' : 'Enable User'}
            </button>
          </form>
        </Panel>

        <Panel title="Wallet and Portfolio">
          <Metric label="Cash" value={formatCurrency(detail.summary.walletCashBalance)} />
          <Metric
            label="Portfolio Value"
            value={formatCurrency(detail.portfolioSummary.totalPortfolioValue)}
          />
          <Metric
            label="Holdings Value"
            value={formatCurrency(detail.portfolioSummary.holdingsValue)}
          />
          <Metric
            label="Unrealized P/L"
            value={formatCurrency(detail.portfolioSummary.unrealizedProfitLoss)}
          />
        </Panel>

        <Panel title="Risk Settings">
          <Metric
            label="Max Order Value"
            value={formatCurrency(detail.riskSettings.maxOrderValue)}
          />
          <Metric
            label="Max Daily Trades"
            value={formatNumber(detail.riskSettings.maxDailyTrades)}
          />
          <Metric
            label="Limit Orders"
            value={detail.riskSettings.allowLimitOrders ? 'Allowed' : 'Disabled'}
          />
          <Metric
            label="Stop-Loss Orders"
            value={
              detail.riskSettings.allowStopLossOrders ? 'Allowed' : 'Disabled'
            }
          />
        </Panel>

        <Panel title="Profile">
          <Metric label="Country" value={detail.profile.country ?? '—'} />
          <Metric label="Phone" value={detail.profile.phoneNumber ?? '—'} />
          <Metric label="Currency" value={detail.profile.preferredCurrency} />
          <Metric
            label="Last Login"
            value={
              detail.profile.lastLoginAt
                ? formatDateTime(detail.profile.lastLoginAt)
                : 'Never'
            }
          />
        </Panel>
      </section>

      <section className="mt-8 grid gap-6 lg:grid-cols-3">
        <ListPanel
          title="Recent Orders"
          items={detail.recentOrders.map(
            (order) => `${order.symbol} · ${order.side} · ${order.status}`,
          )}
        />
        <ListPanel
          title="Recent Trades"
          items={detail.recentTrades.map(
            (trade) => `${trade.symbol} · ${trade.side} · ${trade.quantity}`,
          )}
        />
        <ListPanel
          title="Recent Activity"
          items={detail.recentActivity.map(
            (activity) => `${activity.category} · ${activity.description}`,
          )}
        />
      </section>
    </div>
  )
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between border-b border-slate-800 py-3 last:border-0">
      <span className="text-slate-400">{label}</span>
      <span className="font-semibold text-white">{value}</span>
    </div>
  )
}

function ListPanel({ title, items }: { title: string; items: string[] }) {
  return (
    <Panel title={title}>
      {items.length === 0 ? (
        <p className="text-slate-500">No records available.</p>
      ) : (
        <ul className="space-y-3 text-sm text-slate-300">
          {items.map((item, index) => (
            <li key={`${item}-${index}`} className="rounded-xl bg-slate-950/60 p-3">
              {item}
            </li>
          ))}
        </ul>
      )}
    </Panel>
  )
}
