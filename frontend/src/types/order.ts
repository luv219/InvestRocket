export type OrderSide = 'BUY' | 'SELL'
export type OrderType = 'MARKET' | 'LIMIT' | 'STOP_LOSS'
export type OrderStatus = 'PENDING' | 'EXECUTED' | 'REJECTED' | 'CANCELLED'

export type CreateOrderRequest = {
  symbol: string
  side: OrderSide
  orderType: 'MARKET'
  quantity: number
}

export type Order = {
  id: string
  symbol: string
  side: OrderSide
  orderType: OrderType
  quantity: number
  requestedPrice: number
  executedPrice: number
  status: OrderStatus
  totalAmount: number
  createdAt: string
  executedAt: string
  message: string
}
