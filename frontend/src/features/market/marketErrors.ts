import axios from 'axios'

import type { ApiErrorResponse } from '../../types/api'

export function getMarketErrorMessage(error: unknown) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    return error.response?.data?.message ?? 'Unable to fetch market data'
  }
  return 'Unable to connect to the market data service'
}
