import { apiClient } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type {
  ChangePasswordRequest,
  ResetAccountRequest,
  UpdateProfileRequest,
  UserProfile,
} from '../../types/profile'

export async function getProfile() {
  const response = await apiClient.get<ApiResponse<UserProfile>>('/profile')
  return response.data.data
}

export async function updateProfile(request: UpdateProfileRequest) {
  const response = await apiClient.put<ApiResponse<UserProfile>>(
    '/profile',
    request,
  )
  return response.data.data
}

export async function changePassword(request: ChangePasswordRequest) {
  await apiClient.put('/profile/password', request)
}

export async function resetSimulator(request: ResetAccountRequest) {
  await apiClient.post('/profile/reset-simulator', request)
}
