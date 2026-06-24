export type StockSearchResult = {
  symbol: string
  name: string
  exchange: string | null
  currency: string | null
  type: string | null
}

export type StockQuote = {
  symbol: string
  companyName: string
  currentPrice: number
  changeAmount: number | null
  changePercent: number | null
  openPrice: number | null
  highPrice: number | null
  lowPrice: number | null
  previousClose: number | null
  volume: number | null
  latestTradingTime: string
  currency: string | null
  provider: string
}
