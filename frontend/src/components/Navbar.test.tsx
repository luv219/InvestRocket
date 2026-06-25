import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { vi } from 'vitest'

import { Navbar } from './Navbar'
import { adminUser, authValue, regularUser } from '../test/testAuth'

const useAuthMock = vi.fn()

vi.mock('../features/auth/useAuth', () => ({
  useAuth: () => useAuthMock(),
}))
vi.mock('../features/notifications/notificationService', () => ({
  getNotificationSummary: async () => ({
    unreadCount: 2,
    recentNotifications: [],
  }),
}))

test('shows login and register links when logged out', () => {
  useAuthMock.mockReturnValue(authValue())
  render(
    <MemoryRouter>
      <Navbar />
    </MemoryRouter>,
  )

  expect(screen.getByRole('link', { name: 'Login' })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: 'Register' })).toBeInTheDocument()
  expect(screen.queryByRole('link', { name: 'Admin' })).not.toBeInTheDocument()
})

test('shows protected links without admin link for regular users', () => {
  useAuthMock.mockReturnValue(
    authValue({
      user: regularUser,
      token: 'token',
      isAuthenticated: true,
    }),
  )
  render(
    <MemoryRouter>
      <Navbar />
    </MemoryRouter>,
  )

  expect(screen.getByRole('link', { name: 'Dashboard' })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: 'Alerts' })).toBeInTheDocument()
  expect(screen.queryByRole('link', { name: 'Admin' })).not.toBeInTheDocument()
})

test('shows admin link only for administrators', () => {
  useAuthMock.mockReturnValue(
    authValue({
      user: adminUser,
      token: 'token',
      isAuthenticated: true,
      isAdmin: true,
    }),
  )
  render(
    <MemoryRouter>
      <Navbar />
    </MemoryRouter>,
  )

  expect(screen.getByRole('link', { name: 'Admin' })).toBeInTheDocument()
})
