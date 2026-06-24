import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { useAuth } from '../features/auth/useAuth'

export function AdminRoute() {
  const { isAuthenticated, isAdmin, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return (
      <div className="grid min-h-[60vh] place-items-center text-slate-400">
        Verifying administrator access...
      </div>
    )
  }
  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  if (!isAdmin) {
    return <Navigate to="/access-denied" replace />
  }
  return <Outlet />
}
