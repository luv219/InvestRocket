import { Client } from '@stomp/stompjs'

import type { LivePriceUpdate } from '../../types/watchlist'

const brokerURL =
  import.meta.env.VITE_WS_BASE_URL ?? 'ws://localhost:8080/ws'

export function subscribeToLivePrices(
  onUpdate: (update: LivePriceUpdate) => void,
) {
  const client = new Client({
    brokerURL,
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      client.subscribe('/topic/prices', (message) => {
        onUpdate(JSON.parse(message.body) as LivePriceUpdate)
      })
    },
  })

  client.activate()

  return () => {
    void client.deactivate()
  }
}
