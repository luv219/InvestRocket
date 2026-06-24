export type ActivityCategory =
  | 'AUTH'
  | 'PROFILE'
  | 'WALLET'
  | 'ORDER'
  | 'TRADE'
  | 'WATCHLIST'
  | 'ANALYTICS'
  | 'RISK'
  | 'SYSTEM'

export type ActivityLog = {
  id: string
  category: ActivityCategory
  action: string
  description: string
  metadata: string | null
  createdAt: string
}
