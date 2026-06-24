import { createContext } from 'react'

import type {
  LoginRequest,
  RegisterRequest,
  User,
} from '../../types/auth'

export type AuthContextValue = {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  isAdmin: boolean
  isLoading: boolean
  login: (request: LoginRequest) => Promise<void>
  register: (request: RegisterRequest) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
