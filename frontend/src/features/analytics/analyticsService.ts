import { apiClient } from '../../api/client'
import type {
  AllocationItem,
  HoldingPerformance,
  PortfolioAnalytics,
  PortfolioPerformancePoint,
  TradingStats,
} from '../../types/analytics'
import type { ApiResponse } from '../../types/api'

export async function getAnalyticsOverview() {
  const response =
    await apiClient.get<ApiResponse<PortfolioAnalytics>>('/analytics/overview')
  return response.data.data
}

export async function getPerformanceHistory() {
  const response = await apiClient.get<
    ApiResponse<PortfolioPerformancePoint[]>
  >('/analytics/performance')
  return response.data.data
}

export async function getAllocation() {
  const response =
    await apiClient.get<ApiResponse<AllocationItem[]>>('/analytics/allocation')
  return response.data.data
}

export async function getHoldingPerformance() {
  const response = await apiClient.get<ApiResponse<HoldingPerformance[]>>(
    '/analytics/holdings',
  )
  return response.data.data
}

export async function getTradingStats() {
  const response = await apiClient.get<ApiResponse<TradingStats>>(
    '/analytics/trading-stats',
  )
  return response.data.data
}

export async function createSnapshot() {
  const response = await apiClient.post<ApiResponse<PortfolioPerformancePoint>>(
    '/analytics/snapshot',
  )
  return response.data.data
}
