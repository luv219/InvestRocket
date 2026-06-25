import type { AuthContextValue } from '../features/auth/authContextValue'
import type { User } from '../types/auth'

export const regularUser: User = {
  id: '00000000-0000-0000-0000-000000000001',
  fullName: 'Demo User',
  email: 'demo@example.com',
  role: 'USER',
}

export const adminUser: User = {
  ...regularUser,
  id: '00000000-0000-0000-0000-000000000002',
  role: 'ADMIN',
}

export function authValue(
  overrides: Partial<AuthContextValue> = {},
): AuthContextValue {
  return {
    user: null,
    token: null,
    isAuthenticated: false,
    isAdmin: false,
    isLoading: false,
    login: async () => undefined,
    register: async () => undefined,
    logout: () => undefined,
    ...overrides,
  }
}
