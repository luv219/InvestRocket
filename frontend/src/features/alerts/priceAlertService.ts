import { apiClient } from '../../api/client'
import type { PriceAlert, CreatePriceAlertRequest } from '../../types/alert'
import type { ApiResponse } from '../../types/api'

export async function createAlert(request: CreatePriceAlertRequest) {
  const response = await apiClient.post<ApiResponse<PriceAlert>>(
    '/alerts',
    request,
  )
  return response.data.data
}

export async function getAlerts() {
  const response = await apiClient.get<ApiResponse<PriceAlert[]>>('/alerts')
  return response.data.data
}

export async function getActiveAlerts() {
  const response =
    await apiClient.get<ApiResponse<PriceAlert[]>>('/alerts/active')
  return response.data.data
}

export async function cancelAlert(alertId: string) {
  const response = await apiClient.delete<ApiResponse<PriceAlert>>(
    `/alerts/${alertId}/cancel`,
  )
  return response.data.data
}
