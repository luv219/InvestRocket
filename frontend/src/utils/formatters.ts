export function formatCurrency(value: number, currency = 'USD') {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(value)
}

export function formatNumber(value: number) {
  return new Intl.NumberFormat('en-US').format(value)
}

export function formatPercent(value: number, maximumFractionDigits = 2) {
  return new Intl.NumberFormat('en-US', {
    style: 'percent',
    maximumFractionDigits,
  }).format(value / 100)
}

export function formatDateTime(value: string) {
  return new Date(value).toLocaleString()
}

export function formatDate(value: string) {
  return new Date(value).toLocaleDateString()
}

export function formatOptionalCurrency(value: number | null, currency = 'USD') {
  return value === null ? '—' : formatCurrency(value, currency)
}

export function formatOptionalDateTime(value: string | null) {
  return value === null ? '—' : formatDateTime(value)
}

export function formatOrderStatus(status: string) {
  return status.replaceAll('_', ' ').toLowerCase().replace(/^\w/, (letter) =>
    letter.toUpperCase(),
  )
}

export function formatOrderSide(side: string) {
  return side === 'BUY' ? 'Buy' : side === 'SELL' ? 'Sell' : side
}
