import { apiClient } from '../../api/client'
import type {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from '../../types/auth'
import { clearStoredAuth } from '../../utils/authStorage'

export async function register(request: RegisterRequest) {
  const response = await apiClient.post<ApiResponse<AuthResponse>>(
    '/auth/register',
    request,
  )
  return response.data.data
}

export async function login(request: LoginRequest) {
  const response = await apiClient.post<ApiResponse<AuthResponse>>(
    '/auth/login',
    request,
  )
  return response.data.data
}

export async function getCurrentUser() {
  const response =
    await apiClient.get<ApiResponse<User>>('/auth/me')
  return response.data.data
}

export function logout() {
  clearStoredAuth()
}
