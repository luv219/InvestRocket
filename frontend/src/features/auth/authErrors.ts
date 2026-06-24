import axios from 'axios'

import type { ApiErrorResponse } from '../../types/auth'

export function getAuthErrorMessage(error: unknown) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const response = error.response?.data
    if (response?.errors) {
      const firstError = Object.values(response.errors)[0]
      if (firstError) {
        return firstError
      }
    }
    return response?.message ?? 'Unable to complete the request'
  }

  return 'Unable to connect to the server'
}
