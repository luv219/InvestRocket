import { vi } from 'vitest'

import { apiClient } from '../api/client'
import { login } from './auth/authService'
import { getQuote, searchStocks } from './market/marketService'

vi.mock('../api/client', () => ({
  apiClient: {
    post: vi.fn(),
    get: vi.fn(),
  },
}))

test('auth service posts login credentials to the login endpoint', async () => {
  vi.mocked(apiClient.post).mockResolvedValue({
    data: { data: { accessToken: 'token', tokenType: 'Bearer', user: {} } },
  })

  await login({ email: 'demo@example.com', password: 'Password123' })

  expect(apiClient.post).toHaveBeenCalledWith('/auth/login', {
    email: 'demo@example.com',
    password: 'Password123',
  })
})

test('market service calls search and encoded quote endpoints', async () => {
  vi.mocked(apiClient.get).mockResolvedValue({ data: { data: [] } })
  await searchStocks('AAPL')
  await getQuote('BRK.B')

  expect(apiClient.get).toHaveBeenNthCalledWith(1, '/market/search', {
    params: { query: 'AAPL' },
  })
  expect(apiClient.get).toHaveBeenNthCalledWith(2, '/market/quote/BRK.B')
})
