export type ApiResponse<T> = {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export type ApiErrorResponse = {
  success: false
  message: string
  errors?: Record<string, string>
  timestamp?: string
}
