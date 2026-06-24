export type PortfolioPerformancePoint = {
  date: string
  snapshotTime: string
  totalPortfolioValue: number
  cashBalance: number
  holdingsValue: number
  dailyProfitLoss: number
  dailyProfitLossPercent: number
}

export type AllocationItem = {
  symbol: string
  companyName: string
  currentValue: number
  allocationPercent: number
}

export type HoldingPerformance = {
  symbol: string
  companyName: string
  quantity: number
  averageBuyPrice: number
  currentPrice: number
  currentValue: number
  totalInvested: number
  unrealizedProfitLoss: number
  unrealizedProfitLossPercent: number
}

export type TradingStats = {
  totalTrades: number
  buyTrades: number
  sellTrades: number
  totalOrders: number
  executedOrders: number
  pendingOrders: number
  cancelledOrders: number
  realizedProfitLoss: number
  winningSellTrades: number
  losingSellTrades: number
  winRatePercent: number
}

export type PortfolioAnalytics = {
  currentPortfolioValue: number
  initialBalance: number
  cashBalance: number
  reservedCash: number
  holdingsValue: number
  totalInvested: number
  realizedProfitLoss: number
  unrealizedProfitLoss: number
  totalProfitLoss: number
  totalReturnPercent: number
  bestHolding: HoldingPerformance | null
  worstHolding: HoldingPerformance | null
  allocation: AllocationItem[]
  performanceHistory: PortfolioPerformancePoint[]
  tradingStats: TradingStats
}

export type DashboardAnalytics = Pick<
  PortfolioAnalytics,
  | 'currentPortfolioValue'
  | 'totalProfitLoss'
  | 'totalReturnPercent'
  | 'cashBalance'
  | 'holdingsValue'
>
