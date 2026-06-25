import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'

import { LandingPage } from './LandingPage'

test('renders the portfolio-ready landing page and disclaimer', () => {
  render(
    <MemoryRouter>
      <LandingPage />
    </MemoryRouter>,
  )

  expect(screen.getByRole('heading', { name: /practice investing/i })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: /create an account/i })).toHaveAttribute(
    'href',
    '/register',
  )
  expect(screen.getByText(/virtual trading simulator for educational purposes only/i)).toBeInTheDocument()
})
