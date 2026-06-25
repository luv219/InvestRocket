import type { AxiosResponse } from 'axios'

import { apiClient } from './client'
import { AUTH_TOKEN_KEY } from '../utils/authStorage'

test('attaches the stored bearer token to API requests', async () => {
  localStorage.setItem(AUTH_TOKEN_KEY, 'test-token')
  const originalAdapter = apiClient.defaults.adapter
  apiClient.defaults.adapter = async (config) =>
    ({
      data: {},
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    }) as AxiosResponse

  const response = await apiClient.get('/test')

  expect(response.config.headers.Authorization).toBe('Bearer test-token')
  apiClient.defaults.adapter = originalAdapter
})
