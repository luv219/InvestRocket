import {
  formatCurrency,
  formatOrderSide,
  formatOrderStatus,
  formatPercent,
} from './formatters'

test('formats financial values and readable order labels', () => {
  expect(formatCurrency(1234.5)).toBe('$1,234.50')
  expect(formatPercent(12.345)).toBe('12.35%')
  expect(formatOrderStatus('PARTIALLY_FILLED')).toBe('Partially filled')
  expect(formatOrderSide('BUY')).toBe('Buy')
})
