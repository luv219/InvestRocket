import { apiClient } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type {
  RiskSettings,
  UpdateRiskSettingsRequest,
} from '../../types/risk'

export async function getRiskSettings() {
  const response =
    await apiClient.get<ApiResponse<RiskSettings>>('/profile/risk-settings')
  return response.data.data
}

export async function updateRiskSettings(
  request: UpdateRiskSettingsRequest,
) {
  const response = await apiClient.put<ApiResponse<RiskSettings>>(
    '/profile/risk-settings',
    request,
  )
  return response.data.data
}
