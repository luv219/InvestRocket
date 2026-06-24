export type UserRole = 'USER' | 'ADMIN'

export type User = {
  id: string
  fullName: string
  email: string
  role: UserRole
}

export type RegisterRequest = {
  fullName: string
  email: string
  password: string
  confirmPassword: string
}

export type LoginRequest = {
  email: string
  password: string
}

export type AuthResponse = {
  accessToken: string
  tokenType: string
  user: User
}

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
