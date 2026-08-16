import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'

// Nested inside ProtectedRoute: a logged-out user hits /login first (via
// ProtectedRoute); a logged-in non-admin lands back on the dashboard rather
// than another login prompt, since they ARE authenticated, just not
// permitted here.
export function AdminRoute() {
  const { user } = useAuth()

  if (user?.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
