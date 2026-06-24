import { apiClient } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type { StockQuote, StockSearchResult } from '../../types/market'

export async function searchStocks(query: string) {
  const response = await apiClient.get<ApiResponse<StockSearchResult[]>>(
    '/market/search',
    { params: { query } },
  )
  return response.data.data
}

export async function getQuote(symbol: string) {
  const response = await apiClient.get<ApiResponse<StockQuote>>(
    `/market/quote/${encodeURIComponent(symbol)}`,
  )
  return response.data.data
}
