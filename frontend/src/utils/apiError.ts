import axios from 'axios'

import type { ApiErrorResponse } from '../types/api'

export function getApiErrorMessage(error: unknown, fallback: string) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const response = error.response?.data
    if (response?.errors) {
      const firstError = Object.values(response.errors)[0]
      if (firstError) {
        return firstError
      }
    }
    return response?.message ?? fallback
  }
  return fallback
}
