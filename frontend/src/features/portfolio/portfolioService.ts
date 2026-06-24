import { apiClient } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type { Holding, PortfolioSummary } from '../../types/portfolio'

export async function getPortfolioSummary() {
  const response =
    await apiClient.get<ApiResponse<PortfolioSummary>>('/portfolio/summary')
  return response.data.data
}

export async function getHoldings() {
  const response =
    await apiClient.get<ApiResponse<Holding[]>>('/portfolio/holdings')
  return response.data.data
}
