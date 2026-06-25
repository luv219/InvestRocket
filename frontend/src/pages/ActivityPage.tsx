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
import { Alert } from '../components/ui/Alert'
import { EmptyState } from '../components/ui/EmptyState'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'
import { PageHeader } from '../components/ui/PageHeader'
import { Table, TableContainer } from '../components/ui/Table'

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
      <PageHeader
        eyebrow="Account audit trail"
        title="Activity"
        description="Security and simulator actions recorded for your account."
        actions={<label className="grid gap-2 text-sm text-slate-300"><span>Filter category</span><select
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
        </select></label>}
      />

      {error && (
        <div className="mt-6"><Alert tone="error">{error}</Alert></div>
      )}

      {isLoading ? (
        <LoadingSpinner label="Loading activity..." />
      ) : activity.length === 0 ? (
        <div className="mt-8"><EmptyState title="No activity recorded yet" description="Security, profile, order, trade, watchlist, and system activity will appear here." /></div>
      ) : (
        <TableContainer className="mt-8">
          <Table>
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                {['Category', 'Action', 'Description', 'Time'].map((heading) => (
                  <th key={heading} scope="col" className="whitespace-nowrap px-5 py-4 font-medium">
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
          </Table>
        </TableContainer>
      )}
    </div>
  )
}
