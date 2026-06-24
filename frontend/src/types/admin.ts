import type { ActivityLog } from './activity'
import type { PortfolioSummary } from './portfolio'
import type { RiskSettings } from './risk'
import type { UserProfile } from './profile'

export type AdminUser = {
  id: string
  fullName: string
  email: string
  role: 'USER' | 'ADMIN'
  enabled: boolean
  createdAt: string
  updatedAt: string
  lastLoginAt: string | null
  walletCashBalance: number
  totalOrders: number
  totalTrades: number
  totalHoldings: number
}

export type AdminOrder = {
  id: string
  userEmail: string
  symbol: string
  side: string
  orderType: string
  quantity: number
  status: string
  totalAmount: number
  createdAt: string
}

export type AdminTrade = {
  id: string
  userEmail: string
  symbol: string
  side: string
  quantity: number
  price: number
  tradeValue: number
  realizedProfitLoss: number
  executedAt: string
}

export type AdminUserDetail = {
  profile: UserProfile
  summary: AdminUser
  riskSettings: RiskSettings
  portfolioSummary: PortfolioSummary
  recentOrders: AdminOrder[]
  recentTrades: AdminTrade[]
  recentActivity: ActivityLog[]
}

export type AdminDashboardStats = {
  totalUsers: number
  enabledUsers: number
  disabledUsers: number
  adminUsers: number
  totalOrders: number
  executedOrders: number
  pendingOrders: number
  cancelledOrders: number
  rejectedOrders: number
  totalTrades: number
  totalWatchlistItems: number
  totalPortfolioSnapshots: number
  totalVirtualCash: number
  totalHoldingsValue: number
  totalPlatformPortfolioValue: number
  totalRealizedProfitLoss: number
  totalUnrealizedProfitLoss: number
}

export type SymbolCount = { symbol: string; count: number }
export type UserMetric = {
  email: string
  tradeCount: number
  portfolioValue: number
}

export type AdminTradingStats = {
  totalBuyOrders: number
  totalSellOrders: number
  totalMarketOrders: number
  totalLimitOrders: number
  totalStopLossOrders: number
  mostTradedSymbols: SymbolCount[]
  topUsersByTradeCount: UserMetric[]
  topUsersByPortfolioValue: UserMetric[]
  recentTrades: AdminTrade[]
  recentOrders: AdminOrder[]
}

export type AdminSystemHealth = {
  backendStatus: string
  databaseStatus: string
  marketDataProvider: string
  livePriceStreamEnabled: boolean
  pendingOrderProcessorEnabled: boolean
  portfolioSnapshotEnabled: boolean
  currentTime: string
  activeProfile: string
  applicationName: string
  version: string
}

export type AdminAuditLog = {
  id: string
  userEmail: string
  category: string
  action: string
  description: string
  metadata: string | null
  createdAt: string
}

export type AdminMarketDataStatus = {
  provider: string
  status: string
  testSymbol: string
  currentPrice: number | null
  checkedAt: string
  message: string
}
