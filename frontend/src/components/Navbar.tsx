import { useEffect, useState } from 'react'
import { Link, NavLink } from 'react-router-dom'

import { useAuth } from '../features/auth/useAuth'
import { getNotificationSummary } from '../features/notifications/notificationService'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-lg px-3 py-2 text-sm font-medium ${
    isActive
      ? 'bg-rocket-500/15 text-rocket-400'
      : 'text-slate-300 hover:bg-slate-800 hover:text-white'
  }`

export function Navbar() {
  const { isAuthenticated, isAdmin, logout } = useAuth()
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    if (!isAuthenticated) {
      return
    }
    getNotificationSummary()
      .then((summary) => setUnreadCount(summary.unreadCount))
      .catch(() => setUnreadCount(0))
  }, [isAuthenticated])

  return (
    <header className="border-b border-slate-800 bg-slate-950/90 backdrop-blur">
      <nav className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-3 px-6 py-4">
        <Link to="/" className="flex items-center gap-3 font-semibold text-white">
          <span className="grid size-9 place-items-center rounded-xl bg-rocket-500 text-lg text-slate-950">
            ↗
          </span>
          Invest Rocket
        </Link>
        <div className="flex items-center gap-1">
          {isAuthenticated ? (
            <>
              <NavLink to="/dashboard" className={navLinkClass}>
                Dashboard
              </NavLink>
              <NavLink to="/market" className={navLinkClass}>
                Market
              </NavLink>
              <NavLink to="/watchlist" className={navLinkClass}>
                Watchlist
              </NavLink>
              <NavLink to="/portfolio" className={navLinkClass}>
                Portfolio
              </NavLink>
              <NavLink to="/analytics" className={navLinkClass}>
                Analytics
              </NavLink>
              <NavLink to="/alerts" className={navLinkClass}>
                Alerts
              </NavLink>
              <NavLink to="/journal" className={navLinkClass}>
                Journal
              </NavLink>
              <NavLink to="/notifications" className={navLinkClass}>
                Notifications{isAuthenticated && unreadCount > 0 ? ` (${unreadCount})` : ''}
              </NavLink>
              <NavLink to="/orders" className={navLinkClass}>
                Orders
              </NavLink>
              <NavLink to="/orders/pending" className={navLinkClass}>
                Pending Orders
              </NavLink>
              <NavLink to="/trades" className={navLinkClass}>
                Trades
              </NavLink>
              <NavLink to="/activity" className={navLinkClass}>
                Activity
              </NavLink>
              <NavLink to="/settings" className={navLinkClass}>
                Settings
              </NavLink>
              {isAdmin && (
                <NavLink to="/admin" className={navLinkClass}>
                  Admin
                </NavLink>
              )}
              <button
                type="button"
                onClick={logout}
                className="ml-2 rounded-lg border border-slate-700 px-4 py-2 text-sm font-semibold text-white hover:border-slate-500 hover:bg-slate-800"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={navLinkClass}>
                Login
              </NavLink>
              <Link
                to="/register"
                className="ml-2 rounded-lg bg-rocket-500 px-4 py-2 text-sm font-semibold text-slate-950 hover:bg-rocket-400"
              >
                Register
              </Link>
            </>
          )}
        </div>
      </nav>
    </header>
  )
}
