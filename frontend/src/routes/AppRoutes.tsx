import { lazy, Suspense } from 'react'
import { Route, Routes } from 'react-router-dom'

import { AppLayout } from '../layouts/AppLayout'
import { AccessDeniedPage } from '../pages/AccessDeniedPage'
import { AdminAuditLogsPage } from '../pages/admin/AdminAuditLogsPage'
import { AdminDashboardPage } from '../pages/admin/AdminDashboardPage'
import { AdminSystemHealthPage } from '../pages/admin/AdminSystemHealthPage'
import { AdminTradingStatsPage } from '../pages/admin/AdminTradingStatsPage'
import { AdminUserDetailPage } from '../pages/admin/AdminUserDetailPage'
import { AdminUsersPage } from '../pages/admin/AdminUsersPage'
import { ActivityPage } from '../pages/ActivityPage'
import { DashboardPage } from '../pages/DashboardPage'
import { LandingPage } from '../pages/LandingPage'
import { LoginPage } from '../pages/LoginPage'
import { MarketPage } from '../pages/MarketPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { OrdersPage } from '../pages/OrdersPage'
import { PendingOrdersPage } from '../pages/PendingOrdersPage'
import { PortfolioPage } from '../pages/PortfolioPage'
import { RegisterPage } from '../pages/RegisterPage'
import { SettingsPage } from '../pages/SettingsPage'
import { StockDetailPage } from '../pages/StockDetailPage'
import { TradesPage } from '../pages/TradesPage'
import { WatchlistPage } from '../pages/WatchlistPage'
import { AdminRoute } from './AdminRoute'
import { ProtectedRoute } from './ProtectedRoute'

const AnalyticsPage = lazy(() =>
  import('../pages/AnalyticsPage').then((module) => ({
    default: module.AnalyticsPage,
  })),
)

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<LandingPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="access-denied" element={<AccessDeniedPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="market" element={<MarketPage />} />
          <Route path="market/:symbol" element={<StockDetailPage />} />
          <Route path="watchlist" element={<WatchlistPage />} />
          <Route path="portfolio" element={<PortfolioPage />} />
          <Route
            path="analytics"
            element={
              <Suspense
                fallback={
                  <div className="grid min-h-[60vh] place-items-center text-slate-400">
                    Loading analytics...
                  </div>
                }
              >
                <AnalyticsPage />
              </Suspense>
            }
          />
          <Route path="orders" element={<OrdersPage />} />
          <Route path="orders/pending" element={<PendingOrdersPage />} />
          <Route path="trades" element={<TradesPage />} />
          <Route path="activity" element={<ActivityPage />} />
          <Route path="settings" element={<SettingsPage />} />
        </Route>
        <Route element={<AdminRoute />}>
          <Route path="admin" element={<AdminDashboardPage />} />
          <Route path="admin/users" element={<AdminUsersPage />} />
          <Route path="admin/users/:userId" element={<AdminUserDetailPage />} />
          <Route path="admin/trading-stats" element={<AdminTradingStatsPage />} />
          <Route path="admin/audit-logs" element={<AdminAuditLogsPage />} />
          <Route path="admin/system-health" element={<AdminSystemHealthPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
