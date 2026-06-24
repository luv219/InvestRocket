export type Holding = {
  symbol: string
  companyName: string
  quantity: number
  averageBuyPrice: number
  currentPrice: number
  totalInvested: number
  currentValue: number
  unrealizedProfitLoss: number
  unrealizedProfitLossPercent: number
}

export type PortfolioSummary = {
  cashBalance: number
  holdingsValue: number
  totalPortfolioValue: number
  totalInvested: number
  unrealizedProfitLoss: number
  unrealizedProfitLossPercent: number
  numberOfHoldings: number
}
