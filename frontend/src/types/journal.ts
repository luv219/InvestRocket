export type JournalMood =
  | 'CONFIDENT'
  | 'NEUTRAL'
  | 'UNCERTAIN'
  | 'FRUSTRATED'
  | 'EXCITED'

export type CreateJournalEntryRequest = {
  title: string
  content: string
  mood?: JournalMood
  strategy?: string
  symbol?: string
  orderId?: string
  tradeId?: string
  tags?: string
}

export type UpdateJournalEntryRequest = Omit<
  CreateJournalEntryRequest,
  'orderId' | 'tradeId'
>

export type JournalEntry = {
  id: string
  title: string
  content: string
  mood: JournalMood | null
  strategy: string | null
  symbol: string | null
  orderId: string | null
  tradeId: string | null
  tags: string | null
  createdAt: string
  updatedAt: string
}
