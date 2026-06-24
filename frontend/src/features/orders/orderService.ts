import { apiClient } from '../../api/client'
import type { ApiResponse } from '../../types/api'
import type { CreateOrderRequest, Order } from '../../types/order'

export async function placeOrder(request: CreateOrderRequest) {
  const response = await apiClient.post<ApiResponse<Order>>('/orders', request)
  return response.data.data
}

export async function getOrders() {
  const response = await apiClient.get<ApiResponse<Order[]>>('/orders')
  return response.data.data
}
