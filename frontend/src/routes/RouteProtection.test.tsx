import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { vi } from 'vitest'

import { AdminRoute } from './AdminRoute'
import { ProtectedRoute } from './ProtectedRoute'
import { authValue, regularUser } from '../test/testAuth'

const useAuthMock = vi.fn()

vi.mock('../features/auth/useAuth', () => ({
  useAuth: () => useAuthMock(),
}))

test('redirects unauthenticated users from protected routes', () => {
  useAuthMock.mockReturnValue(authValue())
  render(
    <MemoryRouter initialEntries={['/dashboard']}>
      <Routes>
        <Route path="/login" element={<p>Login destination</p>} />
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<p>Protected dashboard</p>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  )

  expect(screen.getByText('Login destination')).toBeInTheDocument()
})

test('redirects non-admin users from admin routes', () => {
  useAuthMock.mockReturnValue(
    authValue({
      user: regularUser,
      token: 'token',
      isAuthenticated: true,
    }),
  )
  render(
    <MemoryRouter initialEntries={['/admin']}>
      <Routes>
        <Route path="/access-denied" element={<p>Access denied destination</p>} />
        <Route element={<AdminRoute />}>
          <Route path="/admin" element={<p>Admin dashboard</p>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  )

  expect(screen.getByText('Access denied destination')).toBeInTheDocument()
})
