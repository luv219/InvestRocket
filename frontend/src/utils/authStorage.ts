import type { User } from '../types/auth'

export const AUTH_TOKEN_KEY = 'invest_rocket_token'
const AUTH_USER_KEY = 'invest_rocket_user'

export function getStoredToken() {
  return localStorage.getItem(AUTH_TOKEN_KEY)
}

export function getStoredUser(): User | null {
  const storedUser = localStorage.getItem(AUTH_USER_KEY)
  if (!storedUser) {
    return null
  }

  try {
    return JSON.parse(storedUser) as User
  } catch {
    localStorage.removeItem(AUTH_USER_KEY)
    return null
  }
}

export function storeAuth(token: string, user: User) {
  localStorage.setItem(AUTH_TOKEN_KEY, token)
  localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user))
}

export function clearStoredAuth() {
  localStorage.removeItem(AUTH_TOKEN_KEY)
  localStorage.removeItem(AUTH_USER_KEY)
}
