import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/model/use-auth'
import type { ReactNode } from 'react'
import { getStoredAuth } from '@/shared/lib/storage'

type Role = 'FIRMA' | 'ADMIN' | 'TEKNOPARK'

export function RequireAuth({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  const location = useLocation()
  const stored = getStoredAuth()

  if (!isAuthenticated && !stored?.token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return <>{children}</>
}

export function RequireRole({ roles, children }: { roles: Role[]; children: ReactNode }) {
  const { role, isAuthenticated } = useAuth()
  const stored = getStoredAuth()
  const effectiveRole = (isAuthenticated ? role : stored?.role) as Role | undefined

  if (!isAuthenticated && !stored?.token) {
    return <Navigate to="/login" replace />
  }

  if (!effectiveRole || !roles.includes(effectiveRole)) {
    return <Navigate to="/" replace />
  }

  return <>{children}</>
}
