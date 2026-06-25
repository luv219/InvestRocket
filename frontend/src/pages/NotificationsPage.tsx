import { useEffect, useState } from 'react'

import {
  deleteNotification,
  getNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from '../features/notifications/notificationService'
import type { Notification } from '../types/notification'
import { getApiErrorMessage } from '../utils/apiError'
import { formatDateTime } from '../utils/formatters'
import { Alert } from '../components/ui/Alert'
import { EmptyState } from '../components/ui/EmptyState'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'
import { PageHeader } from '../components/ui/PageHeader'

export function NotificationsPage() {
  const [items, setItems] = useState<Notification[]>([])
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    getNotifications()
      .then(setItems)
      .catch((requestError) =>
        setError(getApiErrorMessage(requestError, 'Unable to load notifications')),
      )
      .finally(() => setIsLoading(false))
  }, [])

  async function markRead(id: string) {
    const updated = await markNotificationAsRead(id)
    setItems((current) =>
      current.map((item) => (item.id === id ? updated : item)),
    )
  }

  async function markAll() {
    await markAllNotificationsAsRead()
    setItems((current) =>
      current.map((item) => ({ ...item, isRead: true })),
    )
  }

  async function remove(id: string) {
    await deleteNotification(id)
    setItems((current) => current.filter((item) => item.id !== id))
  }

  return (
    <div className="mx-auto max-w-5xl px-6 py-14">
      <PageHeader eyebrow="In-app updates" title="Notifications" description="Review order, alert, and simulator lifecycle messages." actions={
        <button type="button" onClick={() => void markAll()} className="rounded-xl border border-slate-700 px-4 py-2 font-semibold text-white hover:bg-slate-800">
          Mark all as read
        </button>
      } />
      {error && <div className="mt-6"><Alert tone="error">{error}</Alert></div>}
      {isLoading ? (
        <LoadingSpinner label="Loading notifications..." />
      ) : items.length === 0 ? (
        <div className="mt-8"><EmptyState title="No notifications yet" description="Order, alert, and simulator updates will appear here." /></div>
      ) : (
        <div className="mt-8 space-y-4">
          {items.map((item) => (
            <article key={item.id} className={`rounded-2xl border p-5 ${item.isRead ? 'border-slate-800 bg-slate-900/50' : 'border-rocket-500/40 bg-rocket-500/5'}`}>
              <div className="flex flex-wrap justify-between gap-3">
                <div>
                  <div className="flex gap-2 text-xs font-semibold uppercase tracking-wide text-slate-400">
                    <span>{item.category}</span><span>•</span><span>{item.type}</span>
                  </div>
                  <h2 className="mt-2 text-lg font-bold text-white">{item.title}</h2>
                  <p className="mt-2 text-slate-300">{item.message}</p>
                  <p className="mt-3 text-xs text-slate-500">{formatDateTime(item.createdAt)}</p>
                </div>
                <div className="flex items-start gap-3">
                  {!item.isRead && <button type="button" onClick={() => void markRead(item.id)} className="text-sm font-semibold text-rocket-400">Mark read</button>}
                  <button type="button" onClick={() => void remove(item.id)} className="text-sm font-semibold text-red-400">Delete</button>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  )
}
