import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { homePathForRole, isPathAllowedForRole } from '@/config/nav'

/**
 * Role gate for the authenticated area. Sits inside ProtectedRoute, so a
 * user is guaranteed here. If the current path belongs to a section the
 * user's role can't use (e.g. an inspector opening /payments, or a
 * receiving clerk landing on /dashboard), we redirect them to the first
 * section they *can* use instead of rendering a page the API would 403.
 */
export function RoleRoute() {
  const { user } = useAuth()
  const location = useLocation()

  if (user && !isPathAllowedForRole(location.pathname, user.role)) {
    return <Navigate to={homePathForRole(user.role)} replace />
  }

  return <Outlet />
}
