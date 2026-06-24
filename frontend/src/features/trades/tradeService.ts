import { apiClient } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type { Trade } from '../../types/trade'

export async function getTrades() {
  const response = await apiClient.get<ApiResponse<Trade[]>>('/trades')
  return response.data.data
}
