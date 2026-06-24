export type OrderSide = 'BUY' | 'SELL'
export type OrderType = 'MARKET' | 'LIMIT' | 'STOP_LOSS'
export type OrderStatus =
  | 'PENDING'
  | 'EXECUTED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'EXPIRED'

export type CreateOrderRequest = {
  symbol: string
  side: OrderSide
  orderType: OrderType
  quantity: number
  limitPrice?: number
  stopPrice?: number
}

export type Order = {
  id: string
  symbol: string
  side: OrderSide
  orderType: OrderType
  quantity: number
  limitPrice: number | null
  stopPrice: number | null
  requestedPrice: number
  executedPrice: number | null
  status: OrderStatus
  statusReason: string | null
  totalAmount: number
  createdAt: string
  executedAt: string | null
  cancelledAt: string | null
  message: string
}
