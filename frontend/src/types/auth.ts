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
