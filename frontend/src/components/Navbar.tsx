import { useEffect, useState } from 'react'
import { Link, NavLink } from 'react-router-dom'

import { useAuth } from '../features/auth/useAuth'
import { getNotificationSummary } from '../features/notifications/notificationService'
import { Badge } from './ui/Badge'
import { Button } from './ui/Button'

const primaryLinks = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/market', label: 'Market' },
  { to: '/portfolio', label: 'Portfolio' },
  { to: '/analytics', label: 'Analytics' },
  { to: '/watchlist', label: 'Watchlist' },
]

const secondaryLinks = [
  { to: '/alerts', label: 'Alerts' },
  { to: '/journal', label: 'Journal' },
  { to: '/orders', label: 'Orders' },
  { to: '/orders/pending', label: 'Pending Orders' },
  { to: '/trades', label: 'Trades' },
  { to: '/activity', label: 'Activity' },
  { to: '/settings', label: 'Settings' },
]

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-lg px-3 py-2 text-sm font-medium ${
    isActive
      ? 'bg-rocket-500/15 text-rocket-300'
      : 'text-slate-300 hover:bg-slate-800 hover:text-white'
  }`

export function Navbar() {
  const { isAuthenticated, isAdmin, logout } = useAuth()
  const [unreadCount, setUnreadCount] = useState(0)
  const [isMenuOpen, setIsMenuOpen] = useState(false)

  useEffect(() => {
    if (!isAuthenticated) return
    getNotificationSummary()
      .then((summary) => setUnreadCount(summary.unreadCount))
      .catch(() => setUnreadCount(0))
  }, [isAuthenticated])

  const closeMenu = () => setIsMenuOpen(false)

  return (
    <header className="sticky top-0 z-50 border-b border-slate-800 bg-slate-950/95 backdrop-blur">
      <nav
        aria-label="Primary navigation"
        className="mx-auto max-w-7xl px-4 sm:px-6"
      >
        <div className="flex min-h-18 items-center justify-between gap-4">
          <Link
            to="/"
            onClick={closeMenu}
            className="flex shrink-0 items-center gap-3 font-semibold text-white"
          >
            <span
              aria-hidden="true"
              className="grid size-9 place-items-center rounded-xl bg-rocket-500 text-lg text-slate-950"
            >
              ↗
            </span>
            <span>Invest Rocket</span>
          </Link>

          <div className="hidden items-center gap-1 lg:flex">
            {isAuthenticated ? (
              <>
                {primaryLinks.map((link) => (
                  <NavLink key={link.to} to={link.to} className={navLinkClass}>
                    {link.label}
                  </NavLink>
                ))}
                <details className="relative">
                  <summary className="cursor-pointer list-none rounded-lg px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800 hover:text-white">
                    More
                  </summary>
                  <div className="absolute right-0 mt-2 grid w-52 gap-1 rounded-xl border border-slate-700 bg-slate-900 p-2 shadow-2xl">
                    {secondaryLinks.map((link) => (
                      <NavLink key={link.to} to={link.to} className={navLinkClass}>
                        {link.label}
                      </NavLink>
                    ))}
                  </div>
                </details>
                <NavLink to="/notifications" className={navLinkClass}>
                  <span className="flex items-center gap-2">
                    Notifications
                    {unreadCount > 0 && <Badge tone="info">{unreadCount}</Badge>}
                  </span>
                </NavLink>
                {isAdmin && (
                  <NavLink to="/admin" className={navLinkClass}>
                    Admin
                  </NavLink>
                )}
                <Button variant="secondary" onClick={logout} className="ml-2">
                  Logout
                </Button>
              </>
            ) : (
              <>
                <NavLink to="/login" className={navLinkClass}>
                  Login
                </NavLink>
                <Link
                  to="/register"
                  className="ml-2 rounded-xl bg-rocket-500 px-4 py-2.5 text-sm font-semibold text-slate-950 hover:bg-rocket-400"
                >
                  Get Started
                </Link>
              </>
            )}
          </div>

          <button
            type="button"
            className="grid size-11 place-items-center rounded-xl border border-slate-700 text-xl text-white hover:bg-slate-800 lg:hidden"
            aria-expanded={isMenuOpen}
            aria-controls="mobile-navigation"
            aria-label={isMenuOpen ? 'Close navigation menu' : 'Open navigation menu'}
            onClick={() => setIsMenuOpen((open) => !open)}
          >
            <span aria-hidden="true">{isMenuOpen ? '×' : '☰'}</span>
          </button>
        </div>

        {isMenuOpen && (
          <div
            id="mobile-navigation"
            className="grid gap-1 border-t border-slate-800 py-4 lg:hidden"
          >
            {isAuthenticated ? (
              <>
                {[...primaryLinks, ...secondaryLinks].map((link) => (
                  <NavLink
                    key={link.to}
                    to={link.to}
                    onClick={closeMenu}
                    className={navLinkClass}
                  >
                    {link.label}
                  </NavLink>
                ))}
                <NavLink
                  to="/notifications"
                  onClick={closeMenu}
                  className={navLinkClass}
                >
                  Notifications {unreadCount > 0 ? `(${unreadCount})` : ''}
                </NavLink>
                {isAdmin && (
                  <NavLink to="/admin" onClick={closeMenu} className={navLinkClass}>
                    Admin
                  </NavLink>
                )}
                <Button
                  variant="secondary"
                  onClick={() => {
                    closeMenu()
                    logout()
                  }}
                  className="mt-2 w-full"
                >
                  Logout
                </Button>
              </>
            ) : (
              <>
                <NavLink to="/login" onClick={closeMenu} className={navLinkClass}>
                  Login
                </NavLink>
                <NavLink to="/register" onClick={closeMenu} className={navLinkClass}>
                  Get Started
                </NavLink>
              </>
            )}
          </div>
        )}
      </nav>
    </header>
  )
}
