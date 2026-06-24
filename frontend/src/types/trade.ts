import type { OrderSide } from './order'

export type Trade = {
  id: string
  orderId: string
  symbol: string
  side: OrderSide
  quantity: number
  price: number
  tradeValue: number
  realizedProfitLoss: number
  executedAt: string
}
