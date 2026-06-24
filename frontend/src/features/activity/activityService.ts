import { apiClient } from '../../api/client'
import type { ActivityCategory, ActivityLog } from '../../types/activity'
import type { ApiResponse } from '../../types/api'

export async function getActivity() {
  const response =
    await apiClient.get<ApiResponse<ActivityLog[]>>('/activity')
  return response.data.data
}

export async function getActivityByCategory(category: ActivityCategory) {
  const response = await apiClient.get<ApiResponse<ActivityLog[]>>(
    '/activity',
    { params: { category } },
  )
  return response.data.data
}
