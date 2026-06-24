import {
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'

import type {
  LoginRequest,
  RegisterRequest,
  User,
} from '../../types/auth'
import {
  clearStoredAuth,
  getStoredToken,
  getStoredUser,
  storeAuth,
} from '../../utils/authStorage'
import * as authService from './authService'
import { AuthContext } from './authContextValue'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => getStoredUser())
  const [token, setToken] = useState<string | null>(() => getStoredToken())
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    async function restoreSession() {
      if (!token) {
        setIsLoading(false)
        return
      }

      try {
        const currentUser = await authService.getCurrentUser()
        setUser(currentUser)
        storeAuth(token, currentUser)
      } catch {
        clearStoredAuth()
        setToken(null)
        setUser(null)
      } finally {
        setIsLoading(false)
      }
    }

    void restoreSession()
  }, [token])

  async function login(request: LoginRequest) {
    const response = await authService.login(request)
    storeAuth(response.accessToken, response.user)
    setToken(response.accessToken)
    setUser(response.user)
  }

  async function register(request: RegisterRequest) {
    const response = await authService.register(request)
    storeAuth(response.accessToken, response.user)
    setToken(response.accessToken)
    setUser(response.user)
  }

  function logout() {
    authService.logout()
    setToken(null)
    setUser(null)
  }

  const value = useMemo(
    () => ({
      user,
      token,
      isAuthenticated: Boolean(token && user),
      isAdmin: user?.role === 'ADMIN',
      isLoading,
      login,
      register,
      logout,
    }),
    [isLoading, token, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
