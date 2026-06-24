export function formatCurrency(value: number, currency = 'USD') {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(value)
}

export function formatNumber(value: number) {
  return new Intl.NumberFormat('en-US').format(value)
}

export function formatDateTime(value: string) {
  return new Date(value).toLocaleString()
}

export function formatOptionalCurrency(value: number | null) {
  return value === null ? '—' : formatCurrency(value)
}

export function formatOptionalDateTime(value: string | null) {
  return value === null ? '—' : formatDateTime(value)
}
