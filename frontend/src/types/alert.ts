export type PriceAlertCondition = 'ABOVE' | 'BELOW'
export type PriceAlertStatus = 'ACTIVE' | 'TRIGGERED' | 'CANCELLED'

export type CreatePriceAlertRequest = {
  symbol: string
  targetPrice: number
  condition: PriceAlertCondition
}

export type PriceAlert = CreatePriceAlertRequest & {
  id: string
  companyName: string
  status: PriceAlertStatus
  triggeredPrice: number | null
  triggeredAt: string | null
  createdAt: string
  updatedAt: string
}
