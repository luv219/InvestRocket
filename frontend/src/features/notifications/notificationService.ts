import { apiClient } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type {
  Notification,
  NotificationSummary,
} from '../../types/notification'

export async function getNotifications() {
  const response =
    await apiClient.get<ApiResponse<Notification[]>>('/notifications')
  return response.data.data
}

export async function getUnreadNotifications() {
  const response = await apiClient.get<ApiResponse<Notification[]>>(
    '/notifications/unread',
  )
  return response.data.data
}

export async function getNotificationSummary() {
  const response = await apiClient.get<ApiResponse<NotificationSummary>>(
    '/notifications/summary',
  )
  return response.data.data
}

export async function markNotificationAsRead(notificationId: string) {
  const response = await apiClient.put<ApiResponse<Notification>>(
    `/notifications/${notificationId}/read`,
  )
  return response.data.data
}

export async function markAllNotificationsAsRead() {
  await apiClient.put('/notifications/read-all')
}

export async function deleteNotification(notificationId: string) {
  await apiClient.delete(`/notifications/${notificationId}`)
}
