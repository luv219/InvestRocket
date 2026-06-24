import { apiClient } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type {
  AddWatchlistRequest,
  WatchlistItem,
} from '../../types/watchlist'

export async function getWatchlist() {
  const response =
    await apiClient.get<ApiResponse<WatchlistItem[]>>('/watchlist')
  return response.data.data
}

export async function addToWatchlist(request: AddWatchlistRequest) {
  const response = await apiClient.post<ApiResponse<WatchlistItem>>(
    '/watchlist',
    request,
  )
  return response.data.data
}

export async function removeFromWatchlist(symbol: string) {
  await apiClient.delete(
    `/watchlist/${encodeURIComponent(symbol.toUpperCase())}`,
  )
}
