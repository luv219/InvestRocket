import { useEffect, useState } from 'react'

import {
  getActivity,
  getActivityByCategory,
} from '../features/activity/activityService'
import type {
  ActivityCategory,
  ActivityLog,
} from '../types/activity'
import { getApiErrorMessage } from '../utils/apiError'
import { formatDateTime } from '../utils/formatters'

const categories: Array<'ALL' | ActivityCategory> = [
  'ALL',
  'AUTH',
  'PROFILE',
  'WALLET',
  'ORDER',
  'TRADE',
  'WATCHLIST',
  'ANALYTICS',
  'RISK',
  'SYSTEM',
]

export function ActivityPage() {
  const [activity, setActivity] = useState<ActivityLog[]>([])
  const [category, setCategory] = useState<'ALL' | ActivityCategory>('ALL')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const request =
      category === 'ALL' ? getActivity() : getActivityByCategory(category)
    request
      .then(setActivity)
      .catch((requestError) =>
        setError(
          getApiErrorMessage(requestError, 'Unable to load account activity'),
        ),
      )
      .finally(() => setIsLoading(false))
  }, [category])

  function handleCategoryChange(value: 'ALL' | ActivityCategory) {
    setIsLoading(true)
    setError('')
    setCategory(value)
  }

  return (
    <div className="mx-auto max-w-6xl px-6 py-14">
      <header className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
            Account audit trail
          </p>
          <h1 className="mt-3 text-4xl font-bold text-white">Activity</h1>
          <p className="mt-3 text-slate-400">
            Security and simulator actions recorded for your account.
          </p>
        </div>
        <select
          value={category}
          onChange={(event) =>
            handleCategoryChange(
              event.target.value as 'ALL' | ActivityCategory,
            )
          }
          className="rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white"
        >
          {categories.map((item) => (
            <option key={item} value={item}>
              {item === 'ALL' ? 'All categories' : item}
            </option>
          ))}
        </select>
      </header>

      {error && (
        <p className="mt-6 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-red-300">
          {error}
        </p>
      )}

      {isLoading ? (
        <p className="mt-10 text-slate-400">Loading activity...</p>
      ) : activity.length === 0 ? (
        <div className="mt-8 rounded-2xl border border-dashed border-slate-700 px-6 py-14 text-center text-slate-500">
          No activity recorded yet.
        </div>
      ) : (
        <div className="mt-8 overflow-x-auto rounded-2xl border border-slate-800">
          <table className="min-w-full divide-y divide-slate-800 text-left text-sm">
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                {['Category', 'Action', 'Description', 'Time'].map((heading) => (
                  <th key={heading} className="px-5 py-4 font-medium">
                    {heading}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800 bg-slate-950/40">
              {activity.map((item) => (
                <tr key={item.id}>
                  <td className="px-5 py-4 text-rocket-400">{item.category}</td>
                  <td className="px-5 py-4 font-medium text-white">
                    {item.action.replaceAll('_', ' ')}
                  </td>
                  <td className="px-5 py-4 text-slate-300">
                    {item.description}
                  </td>
                  <td className="px-5 py-4 text-slate-400">
                    {formatDateTime(item.createdAt)}
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
