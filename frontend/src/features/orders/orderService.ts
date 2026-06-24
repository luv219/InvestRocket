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

export async function getPendingOrders() {
  const response = await apiClient.get<ApiResponse<Order[]>>('/orders/pending')
  return response.data.data
}

export async function cancelOrder(orderId: string) {
  const response = await apiClient.delete<ApiResponse<Order>>(
    `/orders/${orderId}/cancel`,
  )
  return response.data.data
}
