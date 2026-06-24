export type NotificationType = 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR'
export type NotificationCategory =
  | 'ORDER'
  | 'TRADE'
  | 'ALERT'
  | 'WATCHLIST'
  | 'PORTFOLIO'
  | 'SYSTEM'

export type Notification = {
  id: string
  title: string
  message: string
  type: NotificationType
  category: NotificationCategory
  isRead: boolean
  relatedEntityType: string | null
  relatedEntityId: string | null
  createdAt: string
  readAt: string | null
}

export type NotificationSummary = {
  unreadCount: number
  recentNotifications: Notification[]
}
