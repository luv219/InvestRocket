import { useEffect, useState } from 'react'

import type { LivePriceUpdate } from '../../types/watchlist'
import { useAuth } from '../auth/useAuth'
import { subscribeToLivePrices } from './livePriceClient'

export function useLivePrices() {
  const { isAuthenticated } = useAuth()
  const [prices, setPrices] = useState<Record<string, LivePriceUpdate>>({})

  useEffect(() => {
    if (!isAuthenticated) {
      return
    }

    return subscribeToLivePrices((update) => {
      setPrices((current) => ({
        ...current,
        [update.symbol]: update,
      }))
    })
  }, [isAuthenticated])

  return prices
}
