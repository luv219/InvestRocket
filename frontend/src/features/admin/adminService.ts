import { apiClient } from '../../api/client'
import type {
  AdminAuditLog,
  AdminDashboardStats,
  AdminMarketDataStatus,
  AdminSystemHealth,
  AdminTradingStats,
  AdminUser,
  AdminUserDetail,
} from '../../types/admin'
import type { ApiResponse } from '../../types/api'

export async function getAdminDashboard() {
  const response = await apiClient.get<ApiResponse<AdminDashboardStats>>(
    '/admin/dashboard',
  )
  return response.data.data
}

export async function getAdminTradingStats() {
  const response = await apiClient.get<ApiResponse<AdminTradingStats>>(
    '/admin/trading-stats',
  )
  return response.data.data
}

export async function getSystemHealth() {
  const response = await apiClient.get<ApiResponse<AdminSystemHealth>>(
    '/admin/system-health',
  )
  return response.data.data
}

export async function getAdminUsers() {
  const response =
    await apiClient.get<ApiResponse<AdminUser[]>>('/admin/users')
  return response.data.data
}

export async function getAdminUserById(userId: string) {
  const response = await apiClient.get<ApiResponse<AdminUserDetail>>(
    `/admin/users/${encodeURIComponent(userId)}`,
  )
  return response.data.data
}

export async function updateAdminUser(
  userId: string,
  request: Pick<AdminUser, 'fullName' | 'role' | 'enabled'>,
) {
  const response = await apiClient.put<ApiResponse<AdminUser>>(
    `/admin/users/${encodeURIComponent(userId)}`,
    request,
  )
  return response.data.data
}

export async function disableUser(userId: string) {
  const response = await apiClient.post<ApiResponse<AdminUser>>(
    `/admin/users/${encodeURIComponent(userId)}/disable`,
  )
  return response.data.data
}

export async function enableUser(userId: string) {
  const response = await apiClient.post<ApiResponse<AdminUser>>(
    `/admin/users/${encodeURIComponent(userId)}/enable`,
  )
  return response.data.data
}

export async function getAdminAuditLogs(params?: {
  category?: string
  userEmail?: string
}) {
  const response = await apiClient.get<ApiResponse<AdminAuditLog[]>>(
    '/admin/audit-logs',
    { params },
  )
  return response.data.data
}

export async function getMarketDataStatus() {
  const response = await apiClient.get<ApiResponse<AdminMarketDataStatus>>(
    '/admin/market-data-status',
  )
  return response.data.data
}
