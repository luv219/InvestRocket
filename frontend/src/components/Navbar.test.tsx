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
  expect(screen.getByRole('link', { name: 'Get Started' })).toBeInTheDocument()
  expect(screen.queryByRole('link', { name: 'Admin' })).not.toBeInTheDocument()
})

test('opens the mobile navigation with an accessible toggle', async () => {
  useAuthMock.mockReturnValue(authValue())
  const user = (await import('@testing-library/user-event')).default.setup()
  render(
    <MemoryRouter>
      <Navbar />
    </MemoryRouter>,
  )

  const toggle = screen.getByRole('button', { name: 'Open navigation menu' })
  await user.click(toggle)

  expect(toggle).toHaveAttribute('aria-expanded', 'true')
  expect(screen.getAllByRole('link', { name: 'Get Started' })).toHaveLength(2)
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
