export type WatchlistItem = {
  id: string
  symbol: string
  companyName: string
  exchange: string | null
  currency: string | null
  currentPrice: number
  changeAmount: number | null
  changePercent: number | null
  latestTradingTime: string
  createdAt: string
}

export type AddWatchlistRequest = {
  symbol: string
}

export type LivePriceUpdate = {
  symbol: string
  currentPrice: number
  changeAmount: number | null
  changePercent: number | null
  latestTradingTime: string
  provider: string
}
