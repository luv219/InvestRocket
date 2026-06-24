export type RiskSettings = {
  maxOrderValue: number
  maxDailyTrades: number
  allowStopLossOrders: boolean
  allowLimitOrders: boolean
}

export type UpdateRiskSettingsRequest = RiskSettings
