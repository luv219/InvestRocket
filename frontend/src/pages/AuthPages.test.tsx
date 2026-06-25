import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { vi } from 'vitest'

import { LoginPage } from './LoginPage'
import { RegisterPage } from './RegisterPage'
import { authValue } from '../test/testAuth'

vi.mock('../features/auth/useAuth', () => ({
  useAuth: () => authValue(),
}))

test('renders login form fields', () => {
  render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>,
  )

  expect(screen.getByLabelText('Email')).toBeInTheDocument()
  expect(screen.getByLabelText('Password')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Login' })).toBeInTheDocument()
})

test('renders registration form fields', () => {
  render(
    <MemoryRouter>
      <RegisterPage />
    </MemoryRouter>,
  )

  expect(screen.getByLabelText('Full name')).toBeInTheDocument()
  expect(screen.getByLabelText('Confirm password')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Register' })).toBeInTheDocument()
})
