import { apiClient } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type {
  CreateJournalEntryRequest,
  JournalEntry,
  UpdateJournalEntryRequest,
} from '../../types/journal'

export async function createEntry(request: CreateJournalEntryRequest) {
  const response = await apiClient.post<ApiResponse<JournalEntry>>(
    '/journal',
    request,
  )
  return response.data.data
}

export async function getEntries(symbol?: string) {
  const response = await apiClient.get<ApiResponse<JournalEntry[]>>('/journal', {
    params: symbol ? { symbol } : undefined,
  })
  return response.data.data
}

export const getEntriesBySymbol = (symbol: string) => getEntries(symbol)

export async function updateEntry(
  entryId: string,
  request: UpdateJournalEntryRequest,
) {
  const response = await apiClient.put<ApiResponse<JournalEntry>>(
    `/journal/${entryId}`,
    request,
  )
  return response.data.data
}

export async function deleteEntry(entryId: string) {
  await apiClient.delete(`/journal/${entryId}`)
}
